package software.ulpgc.chess.ai.application.usecases;

import org.junit.jupiter.api.Test;
import software.ulpgc.chess.ai.application.domain.Engine;
import software.ulpgc.chess.core.GameOverException;
import software.ulpgc.chess.core.Games;
import software.ulpgc.chess.core.IllegalMoveException;
import software.ulpgc.chess.core.Move;
import software.ulpgc.chess.core.Position;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiUseCasesTest {
    private final Map<String, Engine> engines = new LinkedHashMap<>();
    private final List<Position> seen = new ArrayList<>();
    private final ChooseMoveUseCase chooseMove = new ChooseMoveUseCase(engines);

    private List<Move> moves(String... uci) {
        return List.of(uci).stream().map(Move::fromUci).toList();
    }

    @Test
    void theEngineOfTheLevelAnswersForThePositionAfterTheMoves() {
        engines.put("spy", position -> {
            seen.add(position);
            return Games.move("e7e5");
        });
        assertEquals(Games.move("e7e5"), chooseMove.execute(new BestMoveQuery("spy", moves("e2e4"))));
        assertEquals(List.of(Games.play("e2e4").position()), seen);
    }

    @Test
    void theLevelMustExistAndTheGameMustBeLegalAndInProgress() {
        engines.put("first", _ -> Games.move("e2e4"));
        assertThrows(IllegalArgumentException.class, () -> chooseMove.execute(new BestMoveQuery("unknown", List.of())));
        assertThrows(IllegalMoveException.class, () -> chooseMove.execute(new BestMoveQuery("first", moves("e2e5"))));
        assertThrows(GameOverException.class, () -> chooseMove.execute(new BestMoveQuery("first", moves("f2f3", "e7e5", "g2g4", "d8h4"))));
    }

    @Test
    void theLevelsKeepTheirOrder() {
        engines.put("random", _ -> null);
        engines.put("expert", _ -> null);
        assertEquals(List.of("random", "expert"), new GetLevelsUseCase(engines).execute(null));
    }
}
