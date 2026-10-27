package software.ulpgc.chess.server.infrastructure.adapters.ai;

import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.core.Games;
import software.ulpgc.chess.server.application.ports.ChessEngineUnavailableException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpChessEngineTest {
    private Javalin aiService;
    private HttpChessEngine engine;
    private String receivedBody;

    @BeforeEach
    void startFakeAiService() {
        aiService = Javalin.create(config -> {
            config.routes.get("/levels", ctx -> ctx.json(List.of("random", "expert")));
            config.routes.post("/best-move", ctx -> {
                receivedBody = ctx.body();
                if (ctx.body().contains("broken")) ctx.status(500).result("boom");
                else ctx.json(Map.of("move", "e7e5"));
            });
        }).start(0);
        engine = new HttpChessEngine("http://localhost:" + aiService.port() + "/");
    }

    @AfterEach
    void stopFakeAiService() {
        aiService.stop();
    }

    @Test
    void theLevelsComeFromTheAiService() {
        assertEquals(List.of("random", "expert"), engine.levels());
    }

    @Test
    void theGameIsSentAsMovesAndTheAnswerBecomesAMove() {
        assertEquals(Games.move("e7e5"), engine.chooseMove(Games.play("e2e4"), "expert"));
        assertEquals("{\"level\":\"expert\",\"moves\":[\"e2e4\"]}", receivedBody);
    }

    @Test
    void anErrorOfTheAiServiceIsReported() {
        assertThrows(ChessEngineUnavailableException.class, () -> engine.chooseMove(Games.play("e2e4"), "broken"));
    }

    @Test
    void anUnreachableAiServiceIsReported() {
        aiService.stop();
        assertThrows(ChessEngineUnavailableException.class, () -> engine.levels());
    }
}
