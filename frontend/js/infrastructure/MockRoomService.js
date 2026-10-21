import { RoomService } from "../application/RoomService.js";
import { MockGameService } from "./MockGameService.js";

export class MockRoomService extends RoomService {
    #games = new MockGameService();
    #rooms = new Map();
    #listeners = new Map();

    async createRoom() {
        const game = await this.#games.createGame();
        const code = `MOCK${this.#rooms.size + 1}`;
        const room = { code, whiteJoined: true, blackJoined: false, game };
        this.#rooms.set(code, room);
        return { color: "White", playerToken: "white", room };
    }

    async joinRoom(code) {
        const room = await this.getRoom(code);
        if (room.blackJoined) throw new Error("Room is full");
        room.blackJoined = true;
        this.#notify(room);
        return { color: "Black", playerToken: "black", room };
    }

    async getRoom(code) {
        const room = this.#rooms.get(code);
        if (!room) throw new Error("Room not found");
        return room;
    }

    async move(code, playerToken, from, to, promotion) {
        const room = await this.getRoom(code);
        room.game = await this.#games.move(room.game.id, from, to, promotion);
        this.#notify(room);
        return room;
    }

    async legalMoves(gameId, from) {
        return this.#games.legalMoves(gameId, from);
    }

    subscribe(code, listener) {
        if (!this.#listeners.has(code)) this.#listeners.set(code, new Set());
        this.#listeners.get(code).add(listener);
        return () => this.#listeners.get(code).delete(listener);
    }

    #notify(room) {
        for (const listener of this.#listeners.get(room.code) ?? []) listener(room);
    }
}
