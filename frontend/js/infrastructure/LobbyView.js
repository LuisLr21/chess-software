import { pieceSvg } from "./PixelPieces.js";

const LEVEL_NAMES = {
    random: "FACIL",
    greedy: "MEDIO",
    minimax: "DIFICIL",
    expert: "EXPERTO"
};

const ERRORS = {
    404: "NO EXISTE NINGUNA SALA CON ESE CODIGO",
    409: "ESA SALA YA ESTA LLENA"
};

export class LobbyView {
    #element;
    #error;
    #nameInput;
    #preferences;

    constructor(element, preferences, { onCreate, onJoin, onLocal, onAi }) {
        this.#element = element;
        this.#preferences = preferences;
        this.#error = element.querySelector(".lobby-error");
        this.#nameInput = element.querySelector("#player-name");
        this.#nameInput.value = preferences.get("name");
        this.#nameInput.addEventListener("input", () => preferences.set("name", this.#nameInput.value.trim()));
        element.querySelector(".lobby-pieces").innerHTML = ["r", "N", "k", "Q", "b", "P"].map(s => pieceSvg(s)).join("");

        for (const button of element.querySelectorAll("#mode-choice .choice")) {
            button.addEventListener("click", () => this.#selectMode(button.dataset.mode));
        }
        for (const button of element.querySelectorAll("#time-choice .choice")) {
            button.addEventListener("click", () => this.#selectMinutes(Number(button.dataset.minutes)));
        }
        for (const button of element.querySelectorAll("#color-choice .choice")) {
            button.addEventListener("click", () => this.#selectColor(button.dataset.color));
        }
        this.#selectMode("online");
        this.#selectMinutes(preferences.get("minutes"));
        this.#selectColor(preferences.get("aiColor"));

        const codeInput = element.querySelector("#room-code");
        element.querySelector("#create-room").addEventListener("click", () => onCreate(this.#name(), this.#preferences.get("minutes")));
        element.querySelector("#local-game").addEventListener("click", () => onLocal());
        element.querySelector("#ai-game").addEventListener("click",
            () => onAi(this.#preferences.get("aiLevel"), this.#preferences.get("aiColor"), this.#name()));
        element.querySelector("#join-form").addEventListener("submit", event => {
            event.preventDefault();
            if (codeInput.value.trim() !== "") onJoin(codeInput.value, this.#name());
        });
    }

    show() {
        this.#element.hidden = false;
    }

    hide() {
        this.#element.hidden = true;
        this.#error.textContent = "";
    }

    showAiLevels(levels) {
        const row = this.#element.querySelector("#level-choice");
        row.innerHTML = "";
        for (const level of levels) {
            const button = document.createElement("button");
            button.className = "pixel-btn choice";
            button.dataset.level = level;
            button.textContent = LEVEL_NAMES[level] ?? level.toUpperCase();
            button.addEventListener("click", () => this.#selectLevel(level));
            row.append(button);
        }
        const available = levels.length > 0;
        this.#element.querySelector("#ai-unavailable").hidden = available;
        this.#element.querySelector("#ai-game").disabled = !available;
        if (available) this.#selectLevel(levels.includes(this.#preferences.get("aiLevel")) ? this.#preferences.get("aiLevel") : levels[0]);
    }

    showError(error) {
        this.#error.textContent = ERRORS[error.status] ?? "NO SE PUDO CONECTAR CON EL SERVIDOR";
    }

    #name() {
        return this.#nameInput.value.trim();
    }

    #selectMode(mode) {
        this.#markSelected("#mode-choice .choice", button => button.dataset.mode === mode);
        for (const panel of this.#element.querySelectorAll("[data-for]")) {
            panel.hidden = panel.dataset.for !== mode;
        }
    }

    #selectMinutes(minutes) {
        this.#preferences.set("minutes", minutes);
        this.#markSelected("#time-choice .choice", button => Number(button.dataset.minutes) === minutes);
    }

    #selectLevel(level) {
        this.#preferences.set("aiLevel", level);
        this.#markSelected("#level-choice .choice", button => button.dataset.level === level);
    }

    #selectColor(color) {
        this.#preferences.set("aiColor", color);
        this.#markSelected("#color-choice .choice", button => button.dataset.color === color);
    }

    #markSelected(selector, isSelected) {
        for (const button of this.#element.querySelectorAll(selector)) {
            button.classList.toggle("selected", isSelected(button));
        }
    }
}
