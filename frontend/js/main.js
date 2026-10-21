import { LocalGame } from "./application/LocalGame.js";
import { OnlineGame } from "./application/OnlineGame.js";
import { GameScreen } from "./infrastructure/GameScreen.js";
import { LobbyView } from "./infrastructure/LobbyView.js";
import { BrowserSessionStore } from "./infrastructure/BrowserSessionStore.js";
import { BrowserPreferences } from "./infrastructure/BrowserPreferences.js";
import { HttpGameService } from "./infrastructure/HttpGameService.js";
import { HttpRoomService } from "./infrastructure/HttpRoomService.js";
import { MockGameService } from "./infrastructure/MockGameService.js";
import { MockRoomService } from "./infrastructure/MockRoomService.js";

const USE_MOCK = false;
const API_URL = "http://localhost:7070";

const gameService = USE_MOCK ? new MockGameService() : new HttpGameService(API_URL);
const roomService = USE_MOCK ? new MockRoomService() : new HttpRoomService(API_URL);
const preferences = new BrowserPreferences();

const gameArea = document.getElementById("game");
let controller = null;

const screen = new GameScreen(gameArea, preferences, {
    onSelect: square => controller.select(square),
    onMove: (from, to, promotion) => controller.move(from, to, promotion),
    onResign: () => online.resign(),
    onDraw: () => online.offerDraw(),
    onDeclineDraw: () => online.declineDraw(),
    onRematch: () => online.rematch(),
    onSendMessage: text => online.sendMessage(text),
    onTimeUp: () => online.refresh(),
    onNewGame: () => local.start(),
    onLeave: leave
});
const local = new LocalGame(gameService, screen);
const online = new OnlineGame(roomService, screen, new BrowserSessionStore());
const lobby = new LobbyView(document.getElementById("lobby"), preferences, {
    onCreate: (name, minutes) => enter(online, () => online.create(name, minutes)),
    onJoin: (code, name) => enter(online, () => online.join(code, name)),
    onLocal: () => enter(local, () => local.start())
});

async function enter(game, start) {
    controller = game;
    try {
        await start();
        lobby.hide();
        gameArea.hidden = false;
    } catch (error) {
        lobby.showError(error);
    }
}

function leave() {
    controller.leave();
    screen.reset();
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
