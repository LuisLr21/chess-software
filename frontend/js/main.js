import { MockGameService } from "./infrastructure/MockGameService.js";
import { BoardView } from "./infrastructure/BoardView.js";

const service = new MockGameService();
const view = new BoardView(
    document.getElementById("board"),
    async from => view.showHints(await service.legalMoves(game.id, from)),
    async (from, to) => {
        try {
            game = await service.move(game.id, from, to);
        } catch (error) {
            console.warn(error.message);
        }
        view.render(game);
    }
);

let game = await service.createGame();
view.render(game);
