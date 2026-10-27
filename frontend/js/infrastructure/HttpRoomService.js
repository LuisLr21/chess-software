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

    async createRoom(name, minutes) {
        return request(this.#baseUrl, "POST", "/rooms", { name, minutes });
    }

    async joinRoom(code, name) {
        return request(this.#baseUrl, "POST", `/rooms/${code}/players`, { name });
    }

    async getRoom(code, playerToken) {
        return request(this.#baseUrl, "GET", `/rooms/${code}`, undefined, tokenHeader(playerToken));
    }

    async move(code, playerToken, from, to, promotion) {
        return request(this.#baseUrl, "POST", `/rooms/${code}/moves`, { from, to, promotion }, tokenHeader(playerToken));
    }

    async legalMoves(gameId, from) {
        return request(this.#baseUrl, "GET", `/games/${gameId}/moves?from=${from}`);
    }

    async resign(code, playerToken) {
        return request(this.#baseUrl, "POST", `/rooms/${code}/resign`, undefined, tokenHeader(playerToken));
    }

    async offerDraw(code, playerToken) {
        return request(this.#baseUrl, "POST", `/rooms/${code}/draw-offer`, undefined, tokenHeader(playerToken));
    }

    async declineDraw(code, playerToken) {
        return request(this.#baseUrl, "DELETE", `/rooms/${code}/draw-offer`, undefined, tokenHeader(playerToken));
    }

    async rematch(code, playerToken) {
        return request(this.#baseUrl, "POST", `/rooms/${code}/rematch`, undefined, tokenHeader(playerToken));
    }

    async report(code, playerToken, reason) {
        return request(this.#baseUrl, "POST", `/rooms/${code}/reports`, { reason }, tokenHeader(playerToken));
    }

    async messages(code) {
        return request(this.#baseUrl, "GET", `/rooms/${code}/messages`);
    }

    async sendMessage(code, playerToken, text) {
        return request(this.#baseUrl, "POST", `/rooms/${code}/messages`, { text }, tokenHeader(playerToken));
    }

    subscribe(code, playerToken, { onRoom, onPresence, onChat, onReport }) {
        let socket;
        let active = true;
        const handlers = {
            room: event => onRoom(event.room),
            presence: event => onPresence({ white: event.white, black: event.black }),
            chat: event => onChat(event.message),
            report: event => onReport(event.report)
        };
        const connect = () => {
            socket = new WebSocket(`${this.#socketUrl}/rooms/${code}/events?token=${encodeURIComponent(playerToken)}`);
            socket.addEventListener("open", () => this.getRoom(code, playerToken).then(onRoom).catch(() => {}));
            socket.addEventListener("message", message => {
                const event = JSON.parse(message.data);
                handlers[event.type]?.(event);
            });
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

function tokenHeader(playerToken) {
    return playerToken ? { "X-Player-Token": playerToken } : {};
}
