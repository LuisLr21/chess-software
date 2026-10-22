import { pieceSvg } from "./PixelPieces.js";

export class PlayersView {
    #element;

    constructor(element) {
        this.#element = element;
        element.querySelector('[data-color="White"] .player-avatar').innerHTML = pieceSvg("K");
        element.querySelector('[data-color="Black"] .player-avatar').innerHTML = pieceSvg("k");
    }

    render(match) {
        for (const color of ["White", "Black"]) {
            const player = match[color.toLowerCase()];
            const row = this.#element.querySelector(`.player[data-color="${color}"]`);
            const you = match.you === color ? " (TU)" : "";
            row.querySelector(".player-name").textContent = (player.name ?? "???").toUpperCase() + you;
            const presence = row.querySelector(".player-presence");
            presence.textContent = presenceText(match, color, player);
            presence.className = `player-presence ${player.joined && player.online ? "online" : "offline"}`;
            row.classList.toggle("active", match.game.status === "InProgress" && match.game.sideToMove === color);
        }
    }
}

function presenceText(match, color, player) {
    if (match.mode === "ai" && color !== match.you) return match.thinking ? "● PENSANDO..." : "● CPU";
    if (match.mode !== "online") return "● JUGADOR LOCAL";
    if (!player.joined) return "○ ESPERANDO...";
    return player.online ? "● EN LINEA" : "○ DESCONECTADO";
}
