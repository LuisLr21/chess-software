import { GameService } from "../application/GameService.js";

export class MockGameService extends GameService {
    #games = new Map();
    #nextId = 1;

    async createGame() {
        const game = {
            id: String(this.#nextId++),
            sideToMove: "White",
            pieces: initialPieces(),
            status: "InProgress",
            winner: null,
            moves: []
        };
        this.#games.set(game.id, game);
        return game;
    }

    async getGame(id) {
        const game = this.#games.get(id);

        if (!game) {
            throw new Error("Game not found");
        }

        return game;
    }

    async move(id, from, to, promotion) {
        const game = await this.getGame(id);
        const piece = game.pieces[from];

        if (!piece) {
            throw new Error("No piece at the source square");
        }

        game.pieces[to] = promotion ? (isWhite(piece) ? promotion.toUpperCase() : promotion) : piece;
        delete game.pieces[from];
        game.moves.push(from + to + (promotion ?? ""));

        game.sideToMove = game.sideToMove === "White" ? "Black" : "White";

        return game;
    }

    async legalMoves(id, from) {
        const game = await this.getGame(id);
        const piece = game.pieces[from];
        if (!piece) return [];

        const destinations = [];
        for (const file of "abcdefgh") {
            for (let rank = 1; rank <= 8; rank++) {
                const target = game.pieces[file + rank];
                if (!target || isWhite(target) !== isWhite(piece)) destinations.push(file + rank);
            }
        }
        return destinations;
    }
}

function isWhite(symbol) {
    return symbol === symbol.toUpperCase();
}

function initialPieces() {
    const pieces = {};
    const backRank = "RNBQKBNR";
    const files = "abcdefgh";
    for (let i = 0; i < 8; i++) {
        pieces[files[i] + "1"] = backRank[i];
        pieces[files[i] + "2"] = "P";
        pieces[files[i] + "7"] = "p";
        pieces[files[i] + "8"] = backRank[i].toLowerCase();
    }
    return pieces;
}