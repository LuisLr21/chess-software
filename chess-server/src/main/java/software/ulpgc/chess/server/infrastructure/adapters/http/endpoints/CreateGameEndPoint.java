package software.ulpgc.chess.server.infrastructure.adapters.http.endpoints;

import io.javalin.http.Context;
import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.infrastructure.adapters.http.EndPoint;
import software.ulpgc.chess.server.infrastructure.adapters.http.GameResponse;

public class CreateGameEndPoint implements EndPoint {
    private final UseCase<Void, Game> useCase;
    private final Context context;

    private CreateGameEndPoint(UseCase<Void, Game> useCase, Context context) {
        this.useCase = useCase;
        this.context = context;
    }

    public static EndPoint use(UseCase<Void, Game> useCase, Context context) {
        return new CreateGameEndPoint(useCase, context);
    }

    @Override
    public void execute() {
        context.status(201).json(GameResponse.from(useCase.execute(null)));
    }
}
