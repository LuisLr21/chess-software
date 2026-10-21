export class LocalGame {
    #service;
    #board;
    #status;
    #game;

    constructor(service, board, status) {
        this.#service = service;
        this.#board = board;
        this.#status = status;
    }

    async start() {
        this.#show(await this.#service.createGame());
    }

    async select(square) {
        this.#board.showHints(await this.#service.legalMoves(this.#game.id, square));
    }

    async move(from, to, promotion) {
        try {
            this.#game = await this.#service.move(this.#game.id, from, to, promotion);
        } catch (error) {
            console.warn(error.message);
        }
        this.#show(this.#game);
    }

    #show(game) {
        this.#game = game;
        this.#board.render(game);
        this.#status.render(game);
    }
}
