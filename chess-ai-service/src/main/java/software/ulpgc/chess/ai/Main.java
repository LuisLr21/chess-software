package software.ulpgc.chess.ai;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import software.ulpgc.chess.ai.application.domain.Engine;
import software.ulpgc.chess.ai.application.domain.ExpertEngine;
import software.ulpgc.chess.ai.application.domain.GreedyEngine;
import software.ulpgc.chess.ai.application.domain.MinimaxEngine;
import software.ulpgc.chess.ai.application.domain.RandomEngine;
import software.ulpgc.chess.ai.application.usecases.ChooseMoveUseCase;
import software.ulpgc.chess.ai.application.usecases.GetLevelsUseCase;
import software.ulpgc.chess.ai.infrastructure.adapters.endpoints.BestMoveEndPoint;
import software.ulpgc.chess.ai.infrastructure.adapters.endpoints.LevelsEndPoint;
import software.ulpgc.chess.core.GameOverException;
import software.ulpgc.chess.core.IllegalMoveException;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<String, Engine> engines = new LinkedHashMap<>();
        engines.put("random", new RandomEngine());
        engines.put("greedy", new GreedyEngine());
        engines.put("minimax", new MinimaxEngine(3));
        engines.put("expert", new ExpertEngine(Duration.ofSeconds(3), 8));

        GetLevelsUseCase getLevels = new GetLevelsUseCase(engines);
        ChooseMoveUseCase chooseMove = new ChooseMoveUseCase(engines);

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "7071"));
        Javalin.create(config -> configure(config, getLevels, chooseMove)).start(port);
    }

    private static void configure(JavalinConfig config, GetLevelsUseCase getLevels, ChooseMoveUseCase chooseMove) {
        config.routes.get("/levels", context -> LevelsEndPoint.use(getLevels, context).execute());
        config.routes.post("/best-move", context -> BestMoveEndPoint.use(chooseMove, context).execute());

        config.routes.exception(IllegalArgumentException.class, (e, context) -> error(context, 400, e));
        config.routes.exception(IllegalMoveException.class, (e, context) -> error(context, 422, e));
        config.routes.exception(GameOverException.class, (e, context) -> error(context, 409, e));
    }

    private static void error(Context context, int status, Exception e) {
        context.status(status).json(Map.of("error", e.getMessage()));
    }
}
