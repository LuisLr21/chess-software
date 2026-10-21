import { GameService } from "../application/GameService.js";
import { request } from "./http.js";

export class HttpGameService extends GameService {
    #baseUrl;

    constructor(baseUrl) {
        super();
        this.#baseUrl = baseUrl;
    }

    async createGame() {
        return request(this.#baseUrl, "POST", "/games");
    }

    async getGame(id) {
        return request(this.#baseUrl, "GET", `/games/${id}`);
    }

    async move(id, from, to, promotion) {
        return request(this.#baseUrl, "POST", `/games/${id}/moves`, { from, to, promotion });
    }

    async legalMoves(id, from) {
        return request(this.#baseUrl, "GET", `/games/${id}/moves?from=${from}`);
    }
}
