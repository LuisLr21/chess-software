const COLORS = { White: "blancas", Black: "negras" };

const MESSAGES = {
    InProgress: () => "",
    Checkmate: game => `Jaque mate. Ganan las ${COLORS[game.winner]}.`,
    Stalemate: () => "Tablas por ahogado.",
    InsufficientMaterial: () => "Tablas por material insuficiente.",
    FiftyMoveRule: () => "Tablas por la regla de los 50 movimientos."
};

export class StatusView {
    #element;

    constructor(element) {
        this.#element = element;
    }

    render(game) {
        this.#element.textContent = MESSAGES[game.status](game);
    }
}
