import { pieceSvg } from "./PixelPieces.js";
import { PromotionDialog } from "./PromotionDialog.js";

const FILES = "abcdefgh";

export class BoardView {
    #container;
    #onSelect;
    #onMove;
    #selected = null;
    #game = null;
    #player = null;
    #locked = false;
    #renderedKey = null;
    #promotionDialog = new PromotionDialog();

    constructor(container, onSelect, onMove) {
        this.#container = container;
        this.#onSelect = onSelect;
        this.#onMove = onMove;
        this.#container.addEventListener("click", event => this.#handleClick(event));
    }

    render(game, { player = null, locked = false } = {}) {
        const key = [game.id, game.moves.length, game.status, player, locked].join("|");
        this.#game = game;
        if (key === this.#renderedKey) return;
        this.#renderedKey = key;
        this.#player = player;
        this.#locked = locked;
        this.#selected = null;
        const flipped = player === "Black";
        const ranks = flipped ? [1, 2, 3, 4, 5, 6, 7, 8] : [8, 7, 6, 5, 4, 3, 2, 1];
        const files = flipped ? [...FILES].reverse() : [...FILES];
        const lastMove = game.moves.at(-1);
        const lastSquares = lastMove ? [lastMove.slice(0, 2), lastMove.slice(2, 4)] : [];
        const checkedKing = game.inCheck ? (game.sideToMove === "White" ? "K" : "k") : null;
        this.#container.innerHTML = "";
        for (const rank of ranks) {
            for (const file of files) {
                const square = file + rank;
                const piece = game.pieces[square];
                const cell = document.createElement("div");
                cell.classList.add("square", isLight(file, rank) ? "light" : "dark");
                if (lastSquares.includes(square)) cell.classList.add("last-move");
                if (piece && piece === checkedKing) cell.classList.add("in-check");
                cell.dataset.square = square;
                if (piece) cell.innerHTML = pieceSvg(piece);
                if (file === files[0]) cell.appendChild(label("rank-label", rank));
                if (rank === ranks[7]) cell.appendChild(label("file-label", file));
                this.#container.appendChild(cell);
            }
        }
    }

    showHints(squares) {
        for (const square of squares) {
            const cell = this.#cell(square);
            cell.classList.add("hint");
            if (this.#game.pieces[square]) cell.classList.add("capture");
        }
    }

    async #handleClick(event) {
        const cell = event.target.closest(".square");
        if (!cell) return;
        const square = cell.dataset.square;
        if (this.#selected === null) {
            if (this.#canSelect(square)) this.#select(square);
        } else if (this.#selected === square) {
            this.#selected = null;
            this.#clearHighlights();
        } else if (this.#canSelect(square)) {
            this.#clearHighlights();
            this.#select(square);
        } else if (cell.classList.contains("hint")) {
            const from = this.#selected;
            this.#selected = null;
            const promotion = this.#isPromotion(from, square) ? await this.#askPromotion(from) : undefined;
            this.#clearHighlights();
            if (promotion === null) return;
            this.#onMove(from, square, promotion);
        } else {
            this.#selected = null;
            this.#clearHighlights();
        }
    }

    #canSelect(square) {
        const piece = this.#game.pieces[square];
        if (!piece || this.#locked || this.#game.status !== "InProgress") return false;
        const color = isWhite(piece) ? "White" : "Black";
        return color === this.#game.sideToMove && (this.#player === null || this.#player === color);
    }

    #select(square) {
        this.#selected = square;
        this.#cell(square).classList.add("selected");
        this.#onSelect(square);
    }

    #isPromotion(from, to) {
        const piece = this.#game.pieces[from];
        const lastRank = piece === "P" ? "8" : "1";
        return (piece === "P" || piece === "p") && to[1] === lastRank;
    }

    #askPromotion(from) {
        return this.#promotionDialog.ask(this.#game.pieces[from] === "P");
    }

    #clearHighlights() {
        for (const cell of this.#container.querySelectorAll(".selected, .hint")) {
            cell.classList.remove("selected", "hint", "capture");
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

function isWhite(symbol) {
    return symbol === symbol.toUpperCase();
}

function isLight(file, rank) {
    return (FILES.indexOf(file) + rank) % 2 === 0;
}
