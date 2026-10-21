import { RoomService } from "../application/RoomService.js";
import { MockGameService } from "./MockGameService.js";

export class MockRoomService extends RoomService {
    #games = new MockGameService();
    #rooms = new Map();
    #messages = new Map();
    #listeners = new Map();

    async createRoom(name, minutes) {
        const game = await this.#games.createGame();
        const code = `MOCK${this.#rooms.size + 1}`;
        const millis = minutes * 60000;
        const room = {
            code,
            white: { name: name || "Blancas", joined: true },
            black: { name: null, joined: false },
            drawOfferedBy: null,
            rematchOfferedBy: null,
            clock: minutes > 0 ? { initialMillis: millis, whiteMillis: millis, blackMillis: millis, running: null } : null,
            game
        };
        this.#rooms.set(code, room);
        this.#messages.set(code, []);
        return { color: "White", playerToken: "white", room: { ...room, you: "White" } };
    }

    async joinRoom(code, name) {
        const room = await this.#room(code);
        if (room.black.joined) throw new Error("Room is full");
        room.black = { name: name || "Negras", joined: true };
        if (room.clock) room.clock.running = "White";
        this.#notify(room);
        return { color: "Black", playerToken: "black", room: { ...room, you: "Black" } };
    }

    async getRoom(code, playerToken) {
        return this.#view(await this.#room(code), playerToken);
    }

    async move(code, playerToken, from, to, promotion) {
        const room = await this.#room(code);
        room.game = await this.#games.move(room.game.id, from, to, promotion);
        if (room.clock) room.clock.running = room.game.sideToMove;
        return this.#changed(room, playerToken);
    }

    async legalMoves(gameId, from) {
        return this.#games.legalMoves(gameId, from);
    }

    async resign(code, playerToken) {
        const room = await this.#room(code);
        room.game = { ...room.game, status: "Resignation", winner: colorOf(playerToken) === "White" ? "Black" : "White" };
        return this.#changed(room, playerToken);
    }

    async offerDraw(code, playerToken) {
        const room = await this.#room(code);
        if (room.drawOfferedBy && room.drawOfferedBy !== colorOf(playerToken)) {
            room.drawOfferedBy = null;
            room.game = { ...room.game, status: "DrawByAgreement" };
        } else {
            room.drawOfferedBy = colorOf(playerToken);
        }
        return this.#changed(room, playerToken);
    }

    async declineDraw(code, playerToken) {
        const room = await this.#room(code);
        room.drawOfferedBy = null;
        return this.#changed(room, playerToken);
    }

    async rematch(code, playerToken) {
        const room = await this.#room(code);
        room.rematchOfferedBy = colorOf(playerToken);
        return this.#changed(room, playerToken);
    }

    async messages(code) {
        return this.#messages.get(code) ?? [];
    }

    async sendMessage(code, playerToken, text) {
        const room = await this.#room(code);
        const author = colorOf(playerToken);
        const message = { author, authorName: room[author.toLowerCase()].name, text, sentAt: new Date().toISOString() };
        this.#messages.get(code).push(message);
        for (const listener of this.#listeners.get(code) ?? []) listener.onChat(message);
        return message;
    }

    subscribe(code, playerToken, handlers) {
        const listener = { ...handlers, playerToken };
        if (!this.#listeners.has(code)) this.#listeners.set(code, new Set());
        this.#listeners.get(code).add(listener);
        handlers.onPresence({ white: true, black: true });
        return () => this.#listeners.get(code).delete(listener);
    }

    async #room(code) {
        const room = this.#rooms.get(code);
        if (!room) throw new Error("Room not found");
        return room;
    }

    #changed(room, playerToken) {
        this.#notify(room);
        return this.#view(room, playerToken);
    }

    #view(room, playerToken) {
        return { ...room, you: colorOf(playerToken) };
    }

    #notify(room) {
        for (const listener of this.#listeners.get(room.code) ?? []) listener.onRoom(this.#view(room, listener.playerToken));
    }
}

function colorOf(playerToken) {
    if (playerToken === "white") return "White";
    if (playerToken === "black") return "Black";
    return null;
}
