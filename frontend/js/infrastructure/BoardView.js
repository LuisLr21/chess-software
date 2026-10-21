const SYMBOLS = {
    K: "♔", Q: "♕", R: "♖", B: "♗", N: "♘", P: "♙",
    k: "♚", q: "♛", r: "♜", b: "♝", n: "♞", p: "♟"
};

export class BoardView {
    #container;
    #onSelect;
    #onMove;
    #selected = null;

    constructor(container, onSelect, onMove) {
        this.#container = container;
        this.#onSelect = onSelect;
        this.#onMove = onMove;
        this.#container.addEventListener("click", event => this.#handleClick(event));
    }

    render(game) {
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

    #handleClick(event) {
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
            this.#onMove(from, square);
        }
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
