package software.ulpgc.chess.infrastructure;

import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import software.ulpgc.chess.application.CreateGameUseCase;
import software.ulpgc.chess.application.GameRepository;
import software.ulpgc.chess.application.GetGameUseCase;
import software.ulpgc.chess.application.GetLegalMovesUseCase;
import software.ulpgc.chess.application.MovePieceUseCase;
import software.ulpgc.chess.infrastructure.http.GameController;
import software.ulpgc.chess.infrastructure.persistence.InMemoryGameRepository;

public class Main {
    public static void main(String[] args) {
        GameRepository repository = new InMemoryGameRepository();
        GameController controller = new GameController(
                new CreateGameUseCase(repository),
                new GetGameUseCase(repository),
                new MovePieceUseCase(repository),
                new GetLegalMovesUseCase(repository)
        );

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(CorsPluginConfig.CorsRule::anyHost));
            controller.register(config.routes);
        });

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "7070"));
        app.start(port);
    }
}
