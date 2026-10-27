package software.ulpgc.chess.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static software.ulpgc.chess.core.Positions.reachable;
import static software.ulpgc.chess.core.Positions.squares;

class PawnMovementRuleTest {
    private final MovementRule rule = new PawnMovementRule();

    @Test
    void movesOneOrTwoSquaresFromItsStartingRank() {
        Position position = Positions.of(Color.White, "Pe2");
        assertEquals(squares("e3", "e4"), reachable(rule, position, "e2"));
    }

    @Test
    void movesOnlyOneSquareAfterLeavingItsStartingRank() {
        Position position = Positions.of(Color.White, "Pe3");
        assertEquals(squares("e4"), reachable(rule, position, "e3"));
    }

    @Test
    void blackPawnsMoveDown() {
        Position position = Positions.of(Color.Black, "pd7");
        assertEquals(squares("d6", "d5"), reachable(rule, position, "d7"));
    }

    @Test
    void isBlockedByAnyPieceInFront() {
        assertTrue(reachable(rule, Positions.of(Color.White, "Pe2", "pe3"), "e2").isEmpty());
        assertEquals(squares("e3"), reachable(rule, Positions.of(Color.White, "Pe2", "Ne4"), "e2"));
    }

    @Test
    void capturesDiagonallyForward() {
        Position position = Positions.of(Color.White, "Pe4", "pd5", "Pf5", "pe5", "pd3");
        assertEquals(squares("d5"), reachable(rule, position, "e4"));
    }

    @Test
    void capturesEnPassantOnTheTargetSquare() {
        Position position = new Position(Positions.board("Pe5", "pd5"), Color.White, Positions.NO_CASTLING, Square.at("d6"), 0);
        assertEquals(squares("e6", "d6"), reachable(rule, position, "e5"));
    }
}
