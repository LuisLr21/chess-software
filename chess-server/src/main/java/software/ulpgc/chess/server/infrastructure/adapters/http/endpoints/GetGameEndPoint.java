package software.ulpgc.chess.server.infrastructure.adapters.http.endpoints;

import io.javalin.http.Context;
import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.infrastructure.adapters.http.EndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.GameResponse;

public class GetGameEndPoint implements EndPoint {
    private final UseCase<String, Game> useCase;
    private final Context context;

    private GetGameEndPoint(UseCase<String, Game> useCase, Context context) {
        this.useCase = useCase;
        this.context = context;
    }

    public static EndPoint use(UseCase<String, Game> useCase, Context context) {
        return new GetGameEndPoint(useCase, context);
    }

    @Override
    public void execute() {
        context.json(GameResponse.from(useCase.execute(context.pathParam("id"))));
    }
}
