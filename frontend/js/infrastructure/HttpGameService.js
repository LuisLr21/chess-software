import { GameService } from "../application/GameService.js";

export class HttpGameService extends GameService {
    #baseUrl;

    constructor(baseUrl) {
        super();
        this.#baseUrl = baseUrl;
    }

    async createGame() {
        return this.#request("POST", "/games");
    }

    async getGame(id) {
        return this.#request("GET", `/games/${id}`);
    }

    async move(id, from, to, promotion) {
        return this.#request("POST", `/games/${id}/moves`, { from, to, promotion });
    }

    async legalMoves(id, from) {
        return this.#request("GET", `/games/${id}/moves?from=${from}`);
    }

    async #request(method, path, body) {
        const response = await fetch(this.#baseUrl + path, {
            method,
            headers: body ? { "Content-Type": "application/json" } : {},
            body: body ? JSON.stringify(body) : undefined
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.error);
        return data;
    }
}