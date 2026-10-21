import { SYMBOLS } from "./PieceSymbols.js";
import { PromotionDialog } from "./PromotionDialog.js";

export class BoardView {
    #container;
    #onSelect;
    #onMove;
    #selected = null;
    #pieces = {};
    #promotionDialog = new PromotionDialog();

    constructor(container, onSelect, onMove) {
        this.#container = container;
        this.#onSelect = onSelect;
        this.#onMove = onMove;
        this.#container.addEventListener("click", event => this.#handleClick(event));
    }

    render(game) {
        this.#pieces = game.pieces;
        this.#container.innerHTML = "";
        for (let rank = 8; rank >= 1; rank--) {
            for (const file of "abcdefgh") {
                const square = file + rank;
                const cell = document.createElement("div");
                cell.classList.add("square", isLight(file, rank) ? "light" : "dark");
                cell.dataset.square = square;
                cell.textContent = SYMBOLS[game.pieces[square]] ?? "";
                this.#container.appendChild(cell);
            }
        }
    }

    showHints(squares) {
        for (const square of squares) {
            this.#cell(square).classList.add("hint");
        }
    }

    async #handleClick(event) {
        const square = event.target.dataset.square;
        if (!square) return;
        if (this.#selected === null) {
            if (event.target.textContent === "") return;
            this.#selected = square;
            event.target.classList.add("selected");
            this.#onSelect(square);
        } else if (this.#selected === square) {
            this.#selected = null;
            this.#clearHighlights();
        } else {
            const from = this.#selected;
            this.#selected = null;
            const promotion = this.#isPromotion(from, square) ? await this.#askPromotion(from) : undefined;
            if (promotion === null) {
                this.#clearHighlights();
                return;
            }
            this.#onMove(from, square, promotion);
        }
    }

    #isPromotion(from, to) {
        const piece = this.#pieces[from];
        const lastRank = piece === "P" ? "8" : "1";
        return (piece === "P" || piece === "p")
            && to[1] === lastRank
            && this.#cell(to).classList.contains("hint");
    }

    #askPromotion(from) {
        return this.#promotionDialog.ask(this.#pieces[from] === "P");
    }

    #clearHighlights() {
        for (const cell of this.#container.querySelectorAll(".selected, .hint")) {
            cell.classList.remove("selected", "hint");
        }
    }

    #cell(square) {
        return this.#container.querySelector(`[data-square="${square}"]`);
    }
}

function isLight(file, rank) {
    const fileIndex = "abcdefgh".indexOf(file);
    return (fileIndex + rank) % 2 === 0;
}
