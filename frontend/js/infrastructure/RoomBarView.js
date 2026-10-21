const COPIED_MS = 1500;

export class RoomBarView {
    #element;
    #copyButton;
    #code = null;

    constructor(element) {
        this.#element = element;
        this.#copyButton = element.querySelector('[data-action="copy"]');
        this.#copyButton.addEventListener("click", () => this.#copy());
    }

    render(match) {
        const online = match.mode === "online";
        this.#code = match.code;
        this.#element.querySelector(".room-bar-title").textContent = online ? "SALA MULTIJUGADOR:" : "PARTIDA LOCAL:";
        this.#element.querySelector(".room-code").textContent = online ? `#${match.code}` : "1 VS 1";
        this.#element.querySelector(".ping").hidden = !online;
        this.#copyButton.hidden = !online;
    }

    async #copy() {
        try {
            await navigator.clipboard.writeText(this.#code);
            this.#copyButton.textContent = "COPIADO!";
        } catch {
            this.#copyButton.textContent = this.#code;
        }
        setTimeout(() => this.#copyButton.textContent = "COPIAR", COPIED_MS);
    }
}
