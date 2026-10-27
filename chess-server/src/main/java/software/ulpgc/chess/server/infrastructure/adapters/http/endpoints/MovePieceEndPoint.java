package software.ulpgc.chess.server.infrastructure.adapters.http.endpoints;

import io.javalin.http.Context;
import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.core.Square;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.usecases.GameMove;
import software.ulpgc.chess.server.infrastructure.adapters.http.EndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.GameResponse;
import software.ulpgc.chess.server.infrastructure.adapters.http.MoveRequest;

public class MovePieceEndPoint implements EndPoint {
    private final UseCase<GameMove, Game> useCase;
    private final Context context;

    private MovePieceEndPoint(UseCase<GameMove, Game> useCase, Context context) {
        this.useCase = useCase;
        this.context = context;
    }

    public static EndPoint use(UseCase<GameMove, Game> useCase, Context context) {
        return new MovePieceEndPoint(useCase, context);
    }

    @Override
    public void execute() {
        MoveRequest request = context.bodyAsClass(MoveRequest.class);
        GameMove move = new GameMove(context.pathParam("id"), Square.at(request.from()), Square.at(request.to()), request.promotionType());
        context.json(GameResponse.from(useCase.execute(move)));
    }
}
