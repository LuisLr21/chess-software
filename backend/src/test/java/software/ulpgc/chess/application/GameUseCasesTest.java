package software.ulpgc.chess.application;

import org.junit.jupiter.api.Test;
import software.ulpgc.chess.domain.Color;
import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.GameOverException;
import software.ulpgc.chess.domain.Games;
import software.ulpgc.chess.domain.PieceType;
import software.ulpgc.chess.domain.Square;
import software.ulpgc.chess.domain.ai.Engine;
import software.ulpgc.chess.infrastructure.persistence.InMemoryGameRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameUseCasesTest {
    private final GameRepository games = new InMemoryGameRepository();
    private final CreateGameUseCase createGame = new CreateGameUseCase(games);
    private final GetGameUseCase getGame = new GetGameUseCase(games);
    private final MovePieceUseCase movePiece = new MovePieceUseCase(games);
    private final GetLegalMovesUseCase legalMoves = new GetLegalMovesUseCase(games);
    private final Map<String, Engine> engines = new LinkedHashMap<>();
    private final PlayAiMoveUseCase playAiMove = new PlayAiMoveUseCase(games, engines);

    @Test
    void aCreatedGameCanBeRead() {
        Game game = createGame.execute();
        assertEquals(game, getGame.execute(game.id()));
    }

    @Test
    void aMoveIsSaved() {
        Game game = createGame.execute();
        Game moved = movePiece.execute(game.id(), Square.at("e2"), Square.at("e4"), PieceType.Queen);
        assertEquals(Color.Black, moved.position().sideToMove());
        assertEquals(moved, getGame.execute(game.id()));
    }

    @Test
    void listsTheLegalMovesOfAPiece() {
        Game game = createGame.execute();
        assertEquals(List.of(Square.at("e3"), Square.at("e4")), legalMoves.execute(game.id(), Square.at("e2")));
    }

    @Test
    void aMissingGameFails() {
        assertThrows(GameNotFoundException.class, () -> getGame.execute("missing"));
        assertThrows(GameNotFoundException.class, () -> movePiece.execute("missing", Square.at("e2"), Square.at("e4"), PieceType.Queen));
        assertThrows(GameNotFoundException.class, () -> legalMoves.execute("missing", Square.at("e2")));
    }

    @Test
    void theAiPlaysTheMoveOfTheChosenEngine() {
        engines.put("first", _ -> Games.move("e2e4"));
        engines.put("second", _ -> Games.move("d2d4"));
        Game game = createGame.execute();

        Game moved = playAiMove.execute(game.id(), "second");
        assertEquals(List.of(Games.move("d2d4")), moved.moves());
        assertEquals(moved, getGame.execute(game.id()));
    }

    @Test
    void theAiNeedsAKnownLevelAndAGameInProgress() {
        engines.put("first", _ -> Games.move("e2e4"));
        Game game = createGame.execute();
        assertThrows(IllegalArgumentException.class, () -> playAiMove.execute(game.id(), "unknown"));
        assertThrows(IllegalArgumentException.class, () -> playAiMove.execute(game.id(), null));
        assertThrows(GameNotFoundException.class, () -> playAiMove.execute("missing", "first"));

        games.save(Game.start("over").resign(Color.White, null));
        assertThrows(GameOverException.class, () -> playAiMove.execute("over", "first"));
    }

    @Test
    void theAiLevelsKeepTheirOrder() {
        engines.put("random", _ -> null);
        engines.put("greedy", _ -> null);
        engines.put("minimax", _ -> null);
        assertEquals(List.of("random", "greedy", "minimax"), new GetAiLevelsUseCase(engines).execute());
    }
}
