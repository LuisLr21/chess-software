import { toPgn } from "../application/Pgn.js";

export class HistoryView {
    #element;
    #list;
    #match = null;

    constructor(element) {
        this.#element = element;
        this.#list = element.querySelector(".history-moves");
        element.querySelector('[data-action="pgn"]').addEventListener("click", () => this.#download());
    }

    render(match) {
        this.#match = match;
        const san = match.game.san;
        this.#list.innerHTML = "";
        for (let i = 0; i < san.length; i += 2) {
            const row = document.createElement("li");
            row.append(cell("history-number", `${i / 2 + 1}.`), cell("history-white", san[i]), cell("history-black", san[i + 1] ?? ""));
            this.#list.appendChild(row);
        }
        this.#list.querySelectorAll("span:not(.history-number)")[san.length - 1]?.classList.add("last");
        this.#list.parentElement.scrollTop = this.#list.parentElement.scrollHeight;
    }

    #download() {
        if (!this.#match) return;
        const blob = new Blob([toPgn(this.#match)], { type: "application/x-chess-pgn" });
        const link = document.createElement("a");
        link.href = URL.createObjectURL(blob);
        link.download = `partida-${this.#match.code ?? "local"}.pgn`;
        link.click();
        URL.revokeObjectURL(link.href);
    }
}

function cell(className, text) {
    const span = document.createElement("span");
    span.className = className;
    span.textContent = text;
    return span;
}
