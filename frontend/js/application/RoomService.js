export class RoomService {
    async createRoom(name, minutes) {
        throw new Error("Not implemented");
    }

    async joinRoom(code, name) {
        throw new Error("Not implemented");
    }

    async getRoom(code, playerToken) {
        throw new Error("Not implemented");
    }

    async move(code, playerToken, from, to, promotion) {
        throw new Error("Not implemented");
    }

    async legalMoves(gameId, from) {
        throw new Error("Not implemented");
    }

    async resign(code, playerToken) {
        throw new Error("Not implemented");
    }

    async offerDraw(code, playerToken) {
        throw new Error("Not implemented");
    }

    async declineDraw(code, playerToken) {
        throw new Error("Not implemented");
    }

    async rematch(code, playerToken) {
        throw new Error("Not implemented");
    }

    async messages(code) {
        throw new Error("Not implemented");
    }

    async sendMessage(code, playerToken, text) {
        throw new Error("Not implemented");
    }

    subscribe(code, playerToken, { onRoom, onPresence, onChat }) {
        throw new Error("Not implemented");
    }
}
