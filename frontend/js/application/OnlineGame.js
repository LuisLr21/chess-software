export class OnlineGame {
    #service;
    #board;
    #status;
    #roomInfo;
    #store;
    #session = null;
    #room = null;
    #unsubscribe = null;

    constructor(service, board, status, roomInfo, store) {
        this.#service = service;
        this.#board = board;
        this.#status = status;
        this.#roomInfo = roomInfo;
        this.#store = store;
    }

    async create() {
        this.#start(await this.#service.createRoom());
    }

    async join(code) {
        this.#start(await this.#service.joinRoom(code.trim().toUpperCase()));
    }

    async resume() {
        const session = this.#store.load();
        if (!session) return false;
        try {
            const room = await this.#service.getRoom(session.code);
            this.#start({ color: session.color, playerToken: session.playerToken, room });
            return true;
        } catch {
            this.#store.clear();
            return false;
        }
    }

    leave() {
        this.#unsubscribe?.();
        this.#store.clear();
        this.#session = null;
        this.#room = null;
    }

    async select(square) {
        this.#board.showHints(await this.#service.legalMoves(this.#room.game.id, square));
    }

    async move(from, to, promotion) {
        try {
            this.#update(await this.#service.move(this.#session.code, this.#session.playerToken, from, to, promotion));
        } catch (error) {
            console.warn(error.message);
            this.#update(this.#room);
        }
    }

    #start({ color, playerToken, room }) {
        this.#session = { code: room.code, color, playerToken };
        this.#store.save(this.#session);
        this.#update(room);
        this.#unsubscribe = this.#service.subscribe(room.code, updated => this.#update(updated));
    }

    #update(room) {
        this.#room = room;
        this.#board.render(room.game, { player: this.#session.color, locked: !room.blackJoined });
        this.#status.render(room.game);
        this.#roomInfo.render(room, this.#session.color);
    }
}
