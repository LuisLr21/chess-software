export class LobbyView {
    #element;
    #error;

    constructor(element, { onCreate, onJoin, onLocal }) {
        this.#element = element;
        this.#error = element.querySelector(".lobby-error");
        const codeInput = element.querySelector("#room-code");
        element.querySelector("#create-room").addEventListener("click", () => onCreate());
        element.querySelector("#local-game").addEventListener("click", () => onLocal());
        element.querySelector("#join-form").addEventListener("submit", event => {
            event.preventDefault();
            if (codeInput.value.trim() !== "") onJoin(codeInput.value);
        });
    }

    show() {
        this.#element.hidden = false;
    }

    hide() {
        this.#element.hidden = true;
        this.#error.textContent = "";
    }

    showError(message) {
        this.#error.textContent = message;
    }
}
