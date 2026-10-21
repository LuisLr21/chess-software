import { BoardView } from "./infrastructure/BoardView.js";
import { StatusView } from "./infrastructure/StatusView.js";
import { HttpGameService } from "./infrastructure/HttpGameService.js";

const service = new HttpGameService("http://localhost:7070");
const statusView = new StatusView(document.getElementById("status"));
const view = new BoardView(
    document.getElementById("board"),
    async from => view.showHints(await service.legalMoves(game.id, from)),
    async (from, to, promotion) => {
        try {
            game = await service.move(game.id, from, to, promotion);
        } catch (error) {
            console.warn(error.message);
        }
        show(game);
    }
);

function show(game) {
    view.render(game);
    statusView.render(game);
}

let game = await service.createGame();
show(game);
