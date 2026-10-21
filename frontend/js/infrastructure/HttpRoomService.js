import { RoomService } from "../application/RoomService.js";
import { request } from "./http.js";

const RECONNECT_DELAY_MS = 2000;

export class HttpRoomService extends RoomService {
    #baseUrl;
    #socketUrl;

    constructor(baseUrl) {
        super();
        this.#baseUrl = baseUrl;
        this.#socketUrl = baseUrl.replace(/^http/, "ws");
    }

    async createRoom() {
        return request(this.#baseUrl, "POST", "/rooms");
    }

    async joinRoom(code) {
        return request(this.#baseUrl, "POST", `/rooms/${code}/players`);
    }

    async getRoom(code) {
        return request(this.#baseUrl, "GET", `/rooms/${code}`);
    }

    async move(code, playerToken, from, to, promotion) {
        return request(this.#baseUrl, "POST", `/rooms/${code}/moves`, { from, to, promotion },
            { "X-Player-Token": playerToken });
    }

    async legalMoves(gameId, from) {
        return request(this.#baseUrl, "GET", `/games/${gameId}/moves?from=${from}`);
    }

    subscribe(code, listener) {
        let socket;
        let active = true;
        const connect = () => {
            socket = new WebSocket(`${this.#socketUrl}/rooms/${code}/events`);
            socket.addEventListener("open", () => this.getRoom(code).then(listener).catch(() => {}));
            socket.addEventListener("message", event => listener(JSON.parse(event.data)));
            socket.addEventListener("close", () => {
                if (active) setTimeout(connect, RECONNECT_DELAY_MS);
            });
        };
        connect();
        return () => {
            active = false;
            socket.close();
        };
    }
}
