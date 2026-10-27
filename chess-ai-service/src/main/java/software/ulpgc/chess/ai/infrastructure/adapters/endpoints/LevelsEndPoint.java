package software.ulpgc.chess.ai.infrastructure.adapters.endpoints;

import io.javalin.http.Context;
import software.ulpgc.chess.ai.application.UseCase;
import software.ulpgc.chess.ai.infrastructure.adapters.EndPoint;

import java.util.List;

public class LevelsEndPoint implements EndPoint {
    private final UseCase<Void, List<String>> useCase;
    private final Context context;

    private LevelsEndPoint(UseCase<Void, List<String>> useCase, Context context) {
        this.useCase = useCase;
        this.context = context;
    }

    public static EndPoint use(UseCase<Void, List<String>> useCase, Context context) {
        return new LevelsEndPoint(useCase, context);
    }

    @Override
    public void execute() {
        context.json(useCase.execute(null));
    }
}
