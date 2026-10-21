export class OnlineGame {
    #service;
    #screen;
    #store;
    #session = null;
    #room = null;
    #presence = { white: false, black: false };
    #unsubscribe = null;

    constructor(service, screen, store) {
        this.#service = service;
        this.#screen = screen;
        this.#store = store;
    }

    async create(name, minutes) {
        await this.#start(await this.#service.createRoom(name, minutes));
    }

    async join(code, name) {
        await this.#start(await this.#service.joinRoom(code.trim().toUpperCase(), name));
    }

    async resume() {
        const session = this.#store.load();
        if (!session) return false;
        try {
            const room = await this.#service.getRoom(session.code, session.playerToken);
            await this.#start({ playerToken: session.playerToken, room });
            return true;
        } catch {
            this.#store.clear();
            return false;
        }
    }

    leave() {
        this.#unsubscribe?.();
        this.#unsubscribe = null;
        this.#store.clear();
        this.#session = null;
        this.#room = null;
        this.#presence = { white: false, black: false };
    }

    async select(square) {
        this.#screen.showHints(await this.#service.legalMoves(this.#room.game.id, square));
    }

    move(from, to, promotion) {
        return this.#act(() => this.#service.move(this.#session.code, this.#session.playerToken, from, to, promotion));
    }

    resign() {
        return this.#act(() => this.#service.resign(this.#session.code, this.#session.playerToken));
    }

    offerDraw() {
        return this.#act(() => this.#service.offerDraw(this.#session.code, this.#session.playerToken));
    }

    declineDraw() {
        return this.#act(() => this.#service.declineDraw(this.#session.code, this.#session.playerToken));
    }

    rematch() {
        return this.#act(() => this.#service.rematch(this.#session.code, this.#session.playerToken));
    }

    async sendMessage(text) {
        try {
            await this.#service.sendMessage(this.#session.code, this.#session.playerToken, text);
        } catch (error) {
            this.#screen.showError(error);
        }
    }

    async refresh() {
        if (!this.#session) return;
        this.#update(await this.#service.getRoom(this.#session.code, this.#session.playerToken));
    }

    async #act(action) {
        try {
            this.#update(await action());
        } catch (error) {
            this.#screen.showError(error);
            this.#update(this.#room);
        }
    }

    async #start({ playerToken, room }) {
        this.#session = { code: room.code, playerToken };
        this.#store.save(this.#session);
        this.#update(room);
        this.#screen.showMessages(await this.#service.messages(room.code));
        this.#unsubscribe = this.#service.subscribe(room.code, playerToken, {
            onRoom: updated => this.#update(updated),
            onPresence: presence => {
                this.#presence = presence;
                this.#update(this.#room);
            },
            onChat: message => this.#screen.addMessage(message)
        });
    }

    #update(room) {
        if (!this.#session || !room) return;
        this.#room = room;
        this.#screen.showMatch({
            mode: "online",
            code: room.code,
            you: room.you,
            white: { ...room.white, online: this.#presence.white },
            black: { ...room.black, online: this.#presence.black },
            game: room.game,
            clock: room.clock,
            drawOfferedBy: room.drawOfferedBy,
            rematchOfferedBy: room.rematchOfferedBy
        });
    }
}
