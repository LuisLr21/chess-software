package software.ulpgc.chess.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameStatusEvaluatorTest {
    private final GameStatusEvaluator evaluator = new GameStatusEvaluator();

    @Test
    void theInitialPositionIsInProgress() {
        assertEquals(GameStatus.InProgress, evaluator.evaluate(Position.initial()));
    }

    @Test
    void detectsCheckmate() {
        Position foolsMate = Games.play("f2f3", "e7e5", "g2g4", "d8h4").position();
        assertEquals(GameStatus.Checkmate, evaluator.evaluate(foolsMate));
    }

    @Test
    void detectsStalemate() {
        assertEquals(GameStatus.Stalemate, evaluator.evaluate(Positions.of(Color.Black, "ka8", "Qb6", "Kc6")));
    }

    @Test
    void detectsInsufficientMaterial() {
        assertEquals(GameStatus.InsufficientMaterial, evaluator.evaluate(Positions.of(Color.White, "Ke1", "ke8")));
    }

    @Test
    void appliesTheFiftyMoveRuleAfterOneHundredHalfMoves() {
        Board board = Positions.board("Ke1", "Ra1", "ke8");
        assertEquals(GameStatus.InProgress, evaluator.evaluate(new Position(board, Color.White, Positions.NO_CASTLING, null, 99)));
        assertEquals(GameStatus.FiftyMoveRule, evaluator.evaluate(new Position(board, Color.White, Positions.NO_CASTLING, null, 100)));
    }
}
