const THINKING_MS = 400;

export class AiGame {
    #service;
    #screen;
    #game;
    #level;
    #color;
    #name;
    #thinking = false;

    constructor(service, screen) {
        this.#service = service;
        this.#screen = screen;
    }

    async start(level, color, name) {
        this.#level = level;
        this.#color = color;
        this.#name = name;
        await this.restart();
    }

    async restart() {
        this.#show(await this.#service.createGame());
        this.#screen.showMessages([]);
        await this.#playAiTurn();
    }

    async select(square) {
        this.#screen.showHints(await this.#service.legalMoves(this.#game.id, square));
    }

    async move(from, to, promotion) {
        try {
            this.#game = await this.#service.move(this.#game.id, from, to, promotion);
            this.#reviewMove(this.#game);
        } catch (error) {
            this.#screen.showError(error);
        }
        this.#show(this.#game);
        await this.#playAiTurn();
    }

    leave() {
        this.#game = null;
    }

    async #playAiTurn() {
        const game = this.#game;
        if (game.status !== "InProgress" || game.sideToMove === this.#color) return;
        this.#thinking = true;
        this.#show(game);
        await new Promise(resolve => setTimeout(resolve, THINKING_MS));
        try {
            const next = await this.#service.aiMove(game.id, this.#level);
            if (this.#game?.id === game.id) this.#game = next;
        } catch (error) {
            this.#screen.showError(error);
        }
        this.#thinking = false;
        if (this.#game?.id === game.id) this.#show(this.#game);
    }

    async #reviewMove(game) {
        if (game.status !== "InProgress") return;
        const review = await this.#service.reviewMove(game.id, game.moves.length - 1).catch(() => null);
        if (review === null || this.#game?.id !== game.id) return;
        this.#screen.addComment({
            author: this.#color === "White" ? "Black" : "White",
            authorName: "CPU",
            text: reviewText(review)
        });
    }

    #show(game) {
        this.#game = game;
        const human = { name: this.#name || "Jugador", joined: true, online: true };
        const cpu = { name: "CPU", joined: true, online: true };
        this.#screen.showMatch({
            mode: "ai",
            code: null,
            you: this.#color,
            white: this.#color === "White" ? human : cpu,
            black: this.#color === "Black" ? human : cpu,
            game,
            clock: null,
            thinking: this.#thinking,
            drawOfferedBy: null,
            rematchOfferedBy: null
        });
    }
}

function reviewText(review) {
    if (review.best) return `¡Buena jugada! ${review.played} es lo que yo habría jugado.`;
    return `En lugar de ${review.played}, yo habría jugado ${review.suggested}.`;
}
