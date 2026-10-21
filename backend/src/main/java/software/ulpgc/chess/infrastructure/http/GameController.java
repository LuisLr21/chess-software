package software.ulpgc.chess.infrastructure.http;

import io.javalin.config.RoutesConfig;
import software.ulpgc.chess.application.CreateGameUseCase;
import software.ulpgc.chess.application.GameNotFoundException;
import software.ulpgc.chess.application.GetGameUseCase;
import software.ulpgc.chess.application.GetLegalMovesUseCase;
import software.ulpgc.chess.application.MovePieceUseCase;
import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.GameOverException;
import software.ulpgc.chess.domain.IllegalMoveException;
import software.ulpgc.chess.domain.Square;

import java.util.List;

public class GameController {
    private final CreateGameUseCase createGame;
    private final GetGameUseCase getGame;
    private final MovePieceUseCase movePiece;
    private final GetLegalMovesUseCase getLegalMoves;

    public GameController(CreateGameUseCase createGame, GetGameUseCase getGame, MovePieceUseCase movePiece,
                          GetLegalMovesUseCase getLegalMoves) {
        this.createGame = createGame;
        this.getGame = getGame;
        this.movePiece = movePiece;
        this.getLegalMoves = getLegalMoves;
    }

    public void register(RoutesConfig routes) {
        routes.post("/games", ctx -> ctx.status(201).json(GameResponse.from(createGame.execute())));

        routes.get("/games/{id}", ctx -> ctx.json(GameResponse.from(getGame.execute(ctx.pathParam("id")))));

        routes.get("/games/{id}/moves", ctx -> {
            Square from = Square.at(ctx.queryParam("from"));
            List<String> destinations = getLegalMoves.execute(ctx.pathParam("id"), from).stream()
                    .map(Square::toString)
                    .toList();
            ctx.json(destinations);
        });

        routes.post("/games/{id}/moves", ctx -> {
            MoveRequest request = ctx.bodyAsClass(MoveRequest.class);
            Game game = movePiece.execute(ctx.pathParam("id"), Square.at(request.from()), Square.at(request.to()),
                    request.promotionType());
            ctx.json(GameResponse.from(game));
        });

        routes.exception(GameNotFoundException.class, (e, ctx) -> ctx.status(404).json(new ErrorResponse(e.getMessage())));
        routes.exception(GameOverException.class, (e, ctx) -> ctx.status(409).json(new ErrorResponse(e.getMessage())));
        routes.exception(IllegalMoveException.class, (e, ctx) -> ctx.status(422).json(new ErrorResponse(e.getMessage())));
        routes.exception(IllegalArgumentException.class, (e, ctx) -> ctx.status(400).json(new ErrorResponse(e.getMessage())));
    }
}
