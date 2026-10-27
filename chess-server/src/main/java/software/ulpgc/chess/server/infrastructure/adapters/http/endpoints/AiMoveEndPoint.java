package software.ulpgc.chess.server.infrastructure.adapters.http.endpoints;

import io.javalin.http.Context;
import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.usecases.AiTurn;
import software.ulpgc.chess.server.infrastructure.adapters.http.EndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.GameResponse;

public class AiMoveEndPoint implements EndPoint {
    private final UseCase<AiTurn, Game> useCase;
    private final Context context;

    private AiMoveEndPoint(UseCase<AiTurn, Game> useCase, Context context) {
        this.useCase = useCase;
        this.context = context;
    }

    public static EndPoint use(UseCase<AiTurn, Game> useCase, Context context) {
        return new AiMoveEndPoint(useCase, context);
    }

    @Override
    public void execute() {
        AiTurn turn = new AiTurn(context.pathParam("id"), context.queryParam("level"));
        context.json(GameResponse.from(useCase.execute(turn)));
    }
}
