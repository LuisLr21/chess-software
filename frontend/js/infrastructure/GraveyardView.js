import { pieceSvg } from "./PixelPieces.js";

const VALUES = { q: 9, r: 5, b: 3, n: 3, p: 1 };

export class GraveyardView {
    #element;

    constructor(element) {
        this.#element = element;
    }

    render(game) {
        for (const color of ["White", "Black"]) {
            const pieces = [...game.captured[color]].sort((a, b) => value(b) - value(a));
            this.#element.querySelector(`.captures[data-color="${color}"]`).innerHTML =
                pieces.map(piece => pieceSvg(piece, "piece-small")).join("");
        }
        const advantage = total(game.captured.White) - total(game.captured.Black);
        const material = this.#element.querySelector(".material");
        material.textContent = advantage === 0 ? "=" : `${advantage > 0 ? "BLANCAS" : "NEGRAS"} +${Math.abs(advantage)}`;
    }
}

function value(piece) {
    return VALUES[piece.toLowerCase()] ?? 0;
}

function total(pieces) {
    return pieces.reduce((sum, piece) => sum + value(piece), 0);
}
