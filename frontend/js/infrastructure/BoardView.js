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
                if (file === "a") cell.appendChild(label("rank-label", rank));
                if (rank === 1) cell.appendChild(label("file-label", file));
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
        const cell = event.target.closest(".square");
        if (!cell) return;
        const square = cell.dataset.square;
        if (this.#selected === null) {
            if (!this.#pieces[square]) return;
            this.#selected = square;
            cell.classList.add("selected");
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

function label(className, text) {
    const span = document.createElement("span");
    span.classList.add(className);
    span.textContent = text;
    return span;
}

function isLight(file, rank) {
    const fileIndex = "abcdefgh".indexOf(file);
    return (fileIndex + rank) % 2 === 0;
}
