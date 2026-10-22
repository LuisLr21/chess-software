export class LocalGame {
    #service;
    #screen;
    #game;

    constructor(service, screen) {
        this.#service = service;
        this.#screen = screen;
    }

    async start() {
        this.#show(await this.#service.createGame());
        this.#screen.showMessages([]);
    }

    async restart() {
        await this.start();
    }

    async select(square) {
        this.#screen.showHints(await this.#service.legalMoves(this.#game.id, square));
    }

    async move(from, to, promotion) {
        try {
            this.#game = await this.#service.move(this.#game.id, from, to, promotion);
        } catch (error) {
            this.#screen.showError(error);
        }
        this.#show(this.#game);
    }

    leave() {
        this.#game = null;
    }

    #show(game) {
        this.#game = game;
        this.#screen.showMatch({
            mode: "local",
            code: null,
            you: null,
            white: { name: "Blancas", joined: true, online: true },
            black: { name: "Negras", joined: true, online: true },
            game,
            clock: null,
            drawOfferedBy: null,
            rematchOfferedBy: null
        });
    }
}
