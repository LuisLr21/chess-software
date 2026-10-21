import { SYMBOLS } from "./PieceSymbols.js";

const CHOICES = ["q", "r", "b", "n"];

export class PromotionDialog {
    #dialog;

    constructor() {
        this.#dialog = document.createElement("dialog");
        this.#dialog.classList.add("promotion");
        document.body.appendChild(this.#dialog);
    }

    ask(white) {
        return new Promise(resolve => {
            this.#dialog.innerHTML = "";
            for (const choice of CHOICES) {
                const button = document.createElement("button");
                button.textContent = SYMBOLS[white ? choice.toUpperCase() : choice];
                button.addEventListener("click", () => {
                    this.#dialog.close();
                    resolve(choice);
                });
                this.#dialog.appendChild(button);
            }
            this.#dialog.addEventListener("cancel", () => resolve(null), { once: true });
            this.#dialog.showModal();
        });
    }
}
