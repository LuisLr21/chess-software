package software.ulpgc.chess.server.application.usecases;

import org.junit.jupiter.api.Test;
import software.ulpgc.chess.core.Color;
import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.core.GameOverException;
import software.ulpgc.chess.core.Games;
import software.ulpgc.chess.core.PieceType;
import software.ulpgc.chess.core.Square;
import software.ulpgc.chess.server.application.ports.GameRepository;
import software.ulpgc.chess.server.infrastructure.adapters.persistence.InMemoryGameRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameUseCasesTest {
    private final GameRepository games = new InMemoryGameRepository();
    private final CreateGameUseCase createGame = new CreateGameUseCase(games);
    private final GetGameUseCase getGame = new GetGameUseCase(games);
    private final MovePieceUseCase movePiece = new MovePieceUseCase(games);
    private final GetLegalMovesUseCase legalMoves = new GetLegalMovesUseCase(games);
    private final FakeChessEngine engine = new FakeChessEngine();
    private final PlayAiMoveUseCase playAiMove = new PlayAiMoveUseCase(games, engine);

    @Test
    void aCreatedGameCanBeRead() {
        Game game = createGame.execute(null);
        assertEquals(game, getGame.execute(game.id()));
    }

    @Test
    void aMoveIsSaved() {
        Game game = createGame.execute(null);
        Game moved = movePiece.execute(new GameMove(game.id(), Square.at("e2"), Square.at("e4"), PieceType.Queen));
        assertEquals(Color.Black, moved.position().sideToMove());
        assertEquals(moved, getGame.execute(game.id()));
    }

    @Test
    void listsTheLegalMovesOfAPiece() {
        Game game = createGame.execute(null);
        assertEquals(List.of(Square.at("e3"), Square.at("e4")), legalMoves.execute(new SelectedPiece(game.id(), Square.at("e2"))));
    }

    @Test
    void aMissingGameFails() {
        assertThrows(GameNotFoundException.class, () -> getGame.execute("missing"));
        assertThrows(GameNotFoundException.class, () -> movePiece.execute(new GameMove("missing", Square.at("e2"), Square.at("e4"), PieceType.Queen)));
        assertThrows(GameNotFoundException.class, () -> legalMoves.execute(new SelectedPiece("missing", Square.at("e2"))));
    }

    @Test
    void theAiPlaysTheMoveOfTheChosenEngine() {
        engine.plays("first", "e2e4");
        engine.plays("second", "d2d4");
        Game game = createGame.execute(null);

        Game moved = playAiMove.execute(new AiTurn(game.id(), "second"));
        assertEquals(List.of(Games.move("d2d4")), moved.moves());
        assertEquals(moved, getGame.execute(game.id()));
    }

    @Test
    void theAiNeedsAKnownLevelAndAGameInProgress() {
        engine.plays("first", "e2e4");
        Game game = createGame.execute(null);
        assertThrows(IllegalArgumentException.class, () -> playAiMove.execute(new AiTurn(game.id(), "unknown")));
        assertThrows(IllegalArgumentException.class, () -> playAiMove.execute(new AiTurn(game.id(), null)));
        assertThrows(GameNotFoundException.class, () -> playAiMove.execute(new AiTurn("missing", "first")));

        games.save(Game.start("over").resign(Color.White, null));
        assertThrows(GameOverException.class, () -> playAiMove.execute(new AiTurn("over", "first")));
    }

    @Test
    void theAiLevelsKeepTheirOrder() {
        engine.plays("random", null);
        engine.plays("greedy", null);
        engine.plays("minimax", null);
        assertEquals(List.of("random", "greedy", "minimax"), new GetAiLevelsUseCase(engine).execute(null));
    }
}
