package software.ulpgc.chess.infrastructure.http;

import io.javalin.Javalin;
import software.ulpgc.chess.application.CreateGameUseCase;
import software.ulpgc.chess.application.GameNotFoundException;
import software.ulpgc.chess.application.GetGameUseCase;
import software.ulpgc.chess.application.MovePieceUseCase;
import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.IllegalMoveException;
import software.ulpgc.chess.domain.Square;

public class GameController {
    private final CreateGameUseCase createGame;
    private final GetGameUseCase getGame;
    private final MovePieceUseCase movePiece;

    public GameController(CreateGameUseCase createGame, GetGameUseCase getGame, MovePieceUseCase movePiece) {
        this.createGame = createGame;
        this.getGame = getGame;
        this.movePiece = movePiece;
    }

    public void register(Javalin app) {
        app.post("/games", ctx -> ctx.status(201).json(GameResponse.from(createGame.execute())));

        app.get("/games/{id}", ctx -> ctx.json(GameResponse.from(getGame.execute(ctx.pathParam("id")))));

        app.post("/games/{id}/moves", ctx -> {
            MoveRequest request = ctx.bodyAsClass(MoveRequest.class);
            Game game = movePiece.execute(ctx.pathParam("id"), Square.at(request.from()), Square.at(request.to()));
            ctx.json(GameResponse.from(game));
        });

        app.exception(GameNotFoundException.class, (e, ctx) -> ctx.status(404).json(new ErrorResponse(e.getMessage())));
        app.exception(IllegalMoveException.class, (e, ctx) -> ctx.status(422).json(new ErrorResponse(e.getMessage())));
        app.exception(IllegalArgumentException.class, (e, ctx) -> ctx.status(400).json(new ErrorResponse(e.getMessage())));
    }
}
