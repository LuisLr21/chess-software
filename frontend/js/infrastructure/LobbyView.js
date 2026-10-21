import { pieceSvg } from "./PixelPieces.js";

const ERRORS = {
    404: "NO EXISTE NINGUNA SALA CON ESE CODIGO",
    409: "ESA SALA YA ESTA LLENA"
};

export class LobbyView {
    #element;
    #error;
    #nameInput;
    #preferences;

    constructor(element, preferences, { onCreate, onJoin, onLocal }) {
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
        this.#selectMode("online");
        this.#selectMinutes(preferences.get("minutes"));

        const codeInput = element.querySelector("#room-code");
        element.querySelector("#create-room").addEventListener("click", () => onCreate(this.#name(), this.#preferences.get("minutes")));
        element.querySelector("#local-game").addEventListener("click", () => onLocal());
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

    #markSelected(selector, isSelected) {
        for (const button of this.#element.querySelectorAll(selector)) {
            button.classList.toggle("selected", isSelected(button));
        }
    }
}
