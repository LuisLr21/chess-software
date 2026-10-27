import { BoardView } from "./BoardView.js";
import { PlayersView } from "./PlayersView.js";
import { OptionsView } from "./OptionsView.js";
import { GraveyardView } from "./GraveyardView.js";
import { RoomBarView } from "./RoomBarView.js";
import { TurnBannerView } from "./TurnBannerView.js";
import { ClockView } from "./ClockView.js";
import { HistoryView } from "./HistoryView.js";
import { ChatView } from "./ChatView.js";
import { SoundEffects } from "./SoundEffects.js";

const ERRORS = {
    400: "DATOS NO VALIDOS",
    403: "NO PUEDES HACER ESO AHORA",
    409: "LA PARTIDA NO LO PERMITE AHORA",
    422: "MOVIMIENTO ILEGAL"
};

export class GameScreen {
    #board;
    #players;
    #options;
    #graveyard;
    #roomBar;
    #banner;
    #clocks;
    #history;
    #chat;
    #sounds;
    #preferences;
    #match = null;

    constructor(root, preferences, actions) {
        this.#preferences = preferences;
        this.#sounds = new SoundEffects(preferences.get("muted"));
        this.#board = new BoardView(root.querySelector("#board"), actions.onSelect, actions.onMove);
        this.#players = new PlayersView(root.querySelector("#players"));
        this.#options = new OptionsView(root.querySelector("#options"), {
            onRematch: actions.onRematch,
            onNewGame: actions.onNewGame,
            onDraw: actions.onDraw,
            onResign: actions.onResign,
            onReport: actions.onReport,
            onMute: () => this.#toggleSound(),
            onLeave: actions.onLeave
        });
        this.#graveyard = new GraveyardView(root.querySelector("#graveyard"));
        this.#roomBar = new RoomBarView(root.querySelector("#room-bar"));
        this.#banner = new TurnBannerView(root.querySelector("#turn-banner"), {
            onAcceptDraw: actions.onDraw,
            onDeclineDraw: actions.onDeclineDraw,
            onAcceptRematch: actions.onRematch
        });
        this.#clocks = new ClockView(root.querySelector("#clocks"), actions.onTimeUp);
        this.#history = new HistoryView(root.querySelector("#history"));
        this.#chat = new ChatView(root.querySelector("#chat"), actions.onSendMessage);
    }

    showMatch(match) {
        this.#playSounds(this.#match, match);
        this.#match = match;
        const online = match.mode === "online";
        this.#board.render(match.game, {
            player: match.mode === "local" ? null : match.you,
            locked: online && (match.you === null || !match.black.joined)
        });
        this.#players.render(match);
        this.#options.render(match, this.#sounds.muted);
        this.#graveyard.render(match.game);
        this.#roomBar.render(match);
        this.#banner.render(match);
        this.#clocks.render(match.clock);
        this.#history.render(match);
        this.#chat.render(match);
    }

    showHints(squares) {
        this.#board.showHints(squares);
    }

    showMessages(messages) {
        this.#chat.show(messages);
    }

    addMessage(message) {
        this.#chat.add(message);
        if (message.author !== this.#match?.you) this.#sounds.notify();
    }

    addReport(report) {
        this.#chat.addSystem(reportText(report, this.#match));
        if (report.reporter !== this.#match?.you) this.#sounds.notify();
    }

    showError(error) {
        this.#banner.flash(ERRORS[error.status] ?? "ERROR DE CONEXION");
    }

    reset() {
        this.#match = null;
        this.#clocks.stop();
    }

    #toggleSound() {
        const muted = this.#sounds.toggle();
        this.#preferences.set("muted", muted);
        this.#options.renderSound(muted);
        this.#sounds.move();
    }

    #playSounds(previous, next) {
        if (!previous || previous.game.id !== next.game.id) {
            if (previous) this.#sounds.start();
            return;
        }
        const before = previous.game;
        const after = next.game;
        const justEnded = before.status === "InProgress" && after.status !== "InProgress";
        if (justEnded) this.#sounds.end();
        else if (after.moves.length > before.moves.length) this.#playMoveSound(before, after);
        else if (!previous.black.joined && next.black.joined) this.#sounds.start();
        else if (isNewOffer(previous.drawOfferedBy, next.drawOfferedBy, next.you)
            || isNewOffer(previous.rematchOfferedBy, next.rematchOfferedBy, next.you)) this.#sounds.notify();
    }

    #playMoveSound(before, after) {
        const captures = game => game.captured.White.length + game.captured.Black.length;
        if (after.inCheck) this.#sounds.check();
        else if (captures(after) > captures(before)) this.#sounds.capture();
        else this.#sounds.move();
    }
}

function reportText(report, match) {
    const name = color => (match?.[color.toLowerCase()].name ?? color).toUpperCase();
    if (report.reporter !== match?.you) return `${name(report.reporter)} te ha reportado por mala deportividad.`;
    const time = report.reportedTimeUsedMillis === null ? "" : ` Ha gastado ${minutesText(report.reportedTimeUsedMillis)} en ${report.reportedMoves} ${report.reportedMoves === 1 ? "jugada" : "jugadas"}.`;
    return `Has reportado a ${name(report.reported)}: ${report.reason}.${time}`;
}

function minutesText(millis) {
    const seconds = Math.floor(millis / 1000);
    return `${Math.floor(seconds / 60)}:${String(seconds % 60).padStart(2, "0")}`;
}

function isNewOffer(before, after, you) {
    return after !== null && after !== before && after !== you;
}
