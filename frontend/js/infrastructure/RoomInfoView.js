const COLORS = { White: "blancas", Black: "negras" };

export class RoomInfoView {
    #element;

    constructor(element, onLeave) {
        this.#element = element;
        element.querySelector("#leave-room").addEventListener("click", () => onLeave());
    }

    render(room, color) {
        this.#element.hidden = false;
        this.#element.querySelector(".room-code").textContent = room.code;
        this.#element.querySelector(".room-color").textContent = COLORS[color];
        this.#element.querySelector(".room-waiting").hidden = room.blackJoined;
    }

    hide() {
        this.#element.hidden = true;
    }
}
