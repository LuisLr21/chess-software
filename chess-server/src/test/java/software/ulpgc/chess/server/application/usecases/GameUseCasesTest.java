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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameUseCasesTest {
    private final GameRepository games = new InMemoryGameRepository();
    private final CreateGameUseCase createGame = new CreateGameUseCase(games);
    private final GetGameUseCase getGame = new GetGameUseCase(games);
    private final MovePieceUseCase movePiece = new MovePieceUseCase(games);
    private final GetLegalMovesUseCase legalMoves = new GetLegalMovesUseCase(games);
    private final FakeChessEngine engine = new FakeChessEngine();
    private final PlayAiMoveUseCase playAiMove = new PlayAiMoveUseCase(games, engine);
    private final ReviewMoveUseCase reviewMove = new ReviewMoveUseCase(games, engine, "coach");

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
    void aReviewComparesTheMoveWithTheOneTheEngineSuggests() {
        engine.plays("coach", "d2d4");
        games.save(Games.play("e2e4"));

        MoveReview review = reviewMove.execute(new MoveToReview("test", 0));
        assertEquals("e4", review.played());
        assertEquals("d4", review.suggested());
        assertFalse(review.isBest());
    }

    @Test
    void aReviewRecognisesTheBestMove() {
        engine.plays("coach", "e2e4");
        games.save(Games.play("e2e4"));

        assertTrue(reviewMove.execute(new MoveToReview("test", 0)).isBest());
    }

    @Test
    void aReviewUsesThePositionBeforeTheMoveEvenIfTheGameWentOn() {
        engine.plays("coach", "g8f6");
        games.save(Games.play("e2e4", "e7e5", "g1f3"));

        MoveReview review = reviewMove.execute(new MoveToReview("test", 1));
        assertEquals("e5", review.played());
        assertEquals("Nf6", review.suggested());
    }

    @Test
    void aReviewNeedsAnExistingMove() {
        engine.plays("coach", "e2e4");
        games.save(Games.play("e2e4"));

        assertThrows(IllegalArgumentException.class, () -> reviewMove.execute(new MoveToReview("test", -1)));
        assertThrows(IllegalArgumentException.class, () -> reviewMove.execute(new MoveToReview("test", 1)));
        assertThrows(GameNotFoundException.class, () -> reviewMove.execute(new MoveToReview("missing", 0)));
    }

    @Test
    void theAiLevelsKeepTheirOrder() {
        engine.plays("random", null);
        engine.plays("greedy", null);
        engine.plays("minimax", null);
        assertEquals(List.of("random", "greedy", "minimax"), new GetAiLevelsUseCase(engine).execute(null));
    }
}
