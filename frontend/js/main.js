import { LocalGame } from "./application/LocalGame.js";
import { OnlineGame } from "./application/OnlineGame.js";
import { BoardView } from "./infrastructure/BoardView.js";
import { StatusView } from "./infrastructure/StatusView.js";
import { LobbyView } from "./infrastructure/LobbyView.js";
import { RoomInfoView } from "./infrastructure/RoomInfoView.js";
import { BrowserSessionStore } from "./infrastructure/BrowserSessionStore.js";
import { HttpGameService } from "./infrastructure/HttpGameService.js";
import { HttpRoomService } from "./infrastructure/HttpRoomService.js";
import { MockGameService } from "./infrastructure/MockGameService.js";
import { MockRoomService } from "./infrastructure/MockRoomService.js";

const USE_MOCK = false;
const API_URL = "http://localhost:7070";

const gameService = USE_MOCK ? new MockGameService() : new HttpGameService(API_URL);
const roomService = USE_MOCK ? new MockRoomService() : new HttpRoomService(API_URL);

const gameArea = document.getElementById("game");
let controller = null;
const board = new BoardView(
    document.getElementById("board"),
    square => controller.select(square),
    (from, to, promotion) => controller.move(from, to, promotion)
);
const status = new StatusView(document.getElementById("status"));
const roomInfo = new RoomInfoView(document.getElementById("room-info"), leaveRoom);
const local = new LocalGame(gameService, board, status);
const online = new OnlineGame(roomService, board, status, roomInfo, new BrowserSessionStore());
const lobby = new LobbyView(document.getElementById("lobby"), {
    onCreate: () => enter(online, () => online.create()),
    onJoin: code => enter(online, () => online.join(code)),
    onLocal: () => enter(local, () => local.start())
});

async function enter(game, start) {
    controller = game;
    try {
        await start();
        lobby.hide();
        gameArea.hidden = false;
    } catch (error) {
        lobby.showError(error.message);
    }
}

function leaveRoom() {
    online.leave();
    roomInfo.hide();
    gameArea.hidden = true;
    lobby.show();
}

controller = online;
if (await online.resume()) {
    lobby.hide();
    gameArea.hidden = false;
} else {
    lobby.show();
}
