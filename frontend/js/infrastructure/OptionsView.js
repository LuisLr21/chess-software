const CONFIRM_MS = 3000;
const MODES = { online: "MULTIJUGADOR", ai: "VS IA", local: "LOCAL" };

export class OptionsView {
    #element;
    #buttons = {};
    #labels = {};
    #pending = null;
    #match = null;

    constructor(element, { onRematch, onNewGame, onDraw, onResign, onReport, onMute, onLeave }) {
        this.#element = element;
        for (const button of element.querySelectorAll("[data-action]")) {
            this.#buttons[button.dataset.action] = button;
            this.#labels[button.dataset.action] = button.textContent;
        }
        this.#on("rematch", onRematch);
        this.#on("new-game", onNewGame);
        this.#on("draw", onDraw);
        this.#on("resign", () => this.#confirm("resign", onResign));
        this.#on("report", () => this.#confirm("report", onReport));
        this.#on("mute", onMute);
        this.#on("leave", () => this.#isPlaying() ? this.#confirm("leave", onLeave) : onLeave());
    }

    render(match, muted) {
        this.#match = match;
        const online = match.mode === "online";
        const game = match.game;
        const over = game.status !== "InProgress";
        const isPlayer = match.you !== null;
        const opponentOffered = offer => offer !== null && offer !== match.you;

        this.#element.querySelector(".option-mode").textContent = MODES[match.mode];
        this.#element.querySelector(".option-time").textContent =
            match.clock ? `${match.clock.initialMillis / 60000} MIN` : "SIN RELOJ";
        this.renderSound(muted);

        this.#show("rematch", online);
        this.#show("draw", online);
        this.#show("resign", online);
        this.#show("report", online);
        this.#show("new-game", !online);

        this.#buttons.rematch.disabled = !isPlayer || !over || match.rematchOfferedBy === match.you;
        this.#setLabel("rematch", opponentOffered(match.rematchOfferedBy) ? "ACEPTAR REVANCHA" : null);
        this.#buttons.draw.disabled = !isPlayer || over || !match.black.joined || match.drawOfferedBy === match.you;
        this.#setLabel("draw", opponentOffered(match.drawOfferedBy) ? "ACEPTAR TABLAS" : null);
        this.#buttons.resign.disabled = !isPlayer || over || !match.black.joined;
        if (this.#pending !== "resign") this.#setLabel("resign", null);
        this.#buttons.report.disabled = !isPlayer || !match.black.joined || match.reported;
        if (this.#pending !== "report") this.#setLabel("report", match.reported ? "REPORTADO" : null);
    }

    renderSound(muted) {
        const sound = this.#element.querySelector(".option-sound");
        sound.textContent = muted ? "SILENCIO" : "8-BIT SYNTH";
        sound.className = `option-sound ${muted ? "muted" : "accent-green"}`;
    }

    #isPlaying() {
        const match = this.#match;
        return match !== null && match.mode === "online" && match.you !== null
            && match.black.joined && match.game.status === "InProgress";
    }

    #confirm(action, callback) {
        if (this.#pending === action) {
            this.#pending = null;
            this.#setLabel(action, null);
            callback();
            return;
        }
        this.#pending = action;
        this.#setLabel(action, "SEGURO? PULSA OTRA VEZ");
        setTimeout(() => {
            if (this.#pending !== action) return;
            this.#pending = null;
            this.#setLabel(action, null);
        }, CONFIRM_MS);
    }

    #on(action, callback) {
        this.#buttons[action].addEventListener("click", () => callback());
    }

    #show(action, visible) {
        this.#buttons[action].hidden = !visible;
    }

    #setLabel(action, text) {
        this.#buttons[action].textContent = text ?? this.#labels[action];
    }
}
