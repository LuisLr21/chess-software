import { pieceSvg } from "./PixelPieces.js";

const FLASH_MS = 3000;
const COLORS = { White: "BLANCAS", Black: "NEGRAS" };
const KINGS = { White: "K", Black: "k" };

export class TurnBannerView {
    #element;
    #icon;
    #text;
    #actions;
    #onAcceptDraw;
    #onDeclineDraw;
    #onAcceptRematch;
    #flashTimer = null;
    #match = null;

    constructor(element, { onAcceptDraw, onDeclineDraw, onAcceptRematch }) {
        this.#element = element;
        this.#icon = element.querySelector(".turn-icon");
        this.#text = element.querySelector(".turn-text");
        this.#actions = element.querySelector(".turn-actions");
        this.#onAcceptDraw = onAcceptDraw;
        this.#onDeclineDraw = onDeclineDraw;
        this.#onAcceptRematch = onAcceptRematch;
    }

    render(match) {
        this.#match = match;
        if (this.#flashTimer) return;
        const { text, tone, actions, icon = null } = describe(match);
        this.#icon.innerHTML = icon ? pieceSvg(icon, "piece-small") : "";
        this.#text.textContent = text;
        this.#element.className = `pixel-card turn-banner ${tone}`;
        this.#actions.innerHTML = "";
        for (const [label, callback] of actions.map(action => this.#action(action))) {
            const button = document.createElement("button");
            button.className = "pixel-btn pixel-btn-small";
            button.textContent = label;
            button.addEventListener("click", callback);
            this.#actions.appendChild(button);
        }
    }

    flash(message) {
        clearTimeout(this.#flashTimer);
        this.#icon.innerHTML = "";
        this.#text.textContent = message;
        this.#element.className = "pixel-card turn-banner tone-error";
        this.#actions.innerHTML = "";
        this.#flashTimer = setTimeout(() => {
            this.#flashTimer = null;
            if (this.#match) this.render(this.#match);
        }, FLASH_MS);
    }

    #action(action) {
        return {
            "accept-draw": ["ACEPTAR", () => this.#onAcceptDraw()],
            "decline-draw": ["RECHAZAR", () => this.#onDeclineDraw()],
            "accept-rematch": ["ACEPTAR", () => this.#onAcceptRematch()]
        }[action];
    }
}

function describe(match) {
    const game = match.game;
    const online = match.mode === "online";
    const isPlayer = match.you !== null;
    const opponentName = color => (match[color.toLowerCase()].name ?? COLORS[color]).toUpperCase();

    if (online && !match.black.joined) {
        return { text: "ESPERANDO RIVAL... COMPARTE EL CODIGO DE SALA", tone: "tone-wait", actions: [] };
    }
    if (game.status !== "InProgress") {
        const result = endingText(game, match);
        if (online && match.rematchOfferedBy && isPlayer) {
            return match.rematchOfferedBy === match.you
                ? { text: `${result} · REVANCHA PEDIDA...`, tone: "tone-end", actions: [] }
                : { text: `${opponentName(match.rematchOfferedBy)} PIDE REVANCHA`, tone: "tone-offer", actions: ["accept-rematch"] };
        }
        return { text: result, tone: "tone-end", actions: [] };
    }
    if (online && match.drawOfferedBy && isPlayer) {
        return match.drawOfferedBy === match.you
            ? { text: "TABLAS OFRECIDAS · ESPERANDO RESPUESTA", tone: "tone-wait", actions: [] }
            : { text: `${opponentName(match.drawOfferedBy)} OFRECE TABLAS`, tone: "tone-offer", actions: ["accept-draw", "decline-draw"] };
    }
    if (match.thinking) {
        return { text: "LA CPU ESTA PENSANDO...", tone: "tone-wait", actions: [] };
    }
    const side = game.sideToMove;
    const yours = match.you === side ? " (TU)" : "";
    const check = game.inCheck ? " · JAQUE!" : "";
    return { text: `TURNO: ${COLORS[side]}${yours}${check}`, tone: game.inCheck ? "tone-check" : "", actions: [], icon: KINGS[side] };
}

function endingText(game, match) {
    const winner = game.winner ? (match[game.winner.toLowerCase()].name ?? COLORS[game.winner]).toUpperCase() : "";
    const loser = game.winner === "White" ? "Black" : "White";
    switch (game.status) {
        case "Checkmate": return `JAQUE MATE! GANA ${winner}`;
        case "Stalemate": return "TABLAS POR AHOGADO";
        case "InsufficientMaterial": return "TABLAS: MATERIAL INSUFICIENTE";
        case "FiftyMoveRule": return "TABLAS: REGLA DE LOS 50 MOVIMIENTOS";
        case "DrawByAgreement": return "TABLAS DE MUTUO ACUERDO";
        case "Resignation": return `${COLORS[loser]} ABANDONAN · GANA ${winner}`;
        case "Timeout": return `SIN TIEMPO · GANA ${winner}`;
        case "Abandonment": return `${COLORS[loser]} SE DESCONECTAN · GANA ${winner}`;
        default: return game.status.toUpperCase();
    }
}
