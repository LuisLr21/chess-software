package software.ulpgc.chess.server.infrastructure.adapters.http.endpoints;

import io.javalin.http.Context;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.infrastructure.adapters.http.EndPoint;

import java.util.List;

public class AiLevelsEndPoint implements EndPoint {
    private final UseCase<Void, List<String>> useCase;
    private final Context context;

    private AiLevelsEndPoint(UseCase<Void, List<String>> useCase, Context context) {
        this.useCase = useCase;
        this.context = context;
    }

    public static EndPoint use(UseCase<Void, List<String>> useCase, Context context) {
        return new AiLevelsEndPoint(useCase, context);
    }

    @Override
    public void execute() {
        context.json(useCase.execute(null));
    }
}
