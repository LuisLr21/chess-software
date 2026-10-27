package software.ulpgc.chess.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static software.ulpgc.chess.core.Positions.reachable;
import static software.ulpgc.chess.core.Positions.squares;

class KingMovementRuleTest {
    private final MovementRule rule = new KingMovementRule();

    @Test
    void movesOneSquareInAnyDirection() {
        Position position = Positions.of(Color.White, "Kd4");
        assertEquals(squares("c3", "c4", "c5", "d3", "d5", "e3", "e4", "e5"), reachable(rule, position, "d4"));
    }

    @Test
    void hasThreeMovesInTheCorner() {
        Position position = Positions.of(Color.White, "Ka1");
        assertEquals(squares("a2", "b1", "b2"), reachable(rule, position, "a1"));
    }

    @Test
    void cannotCaptureOwnPieces() {
        Position position = Positions.of(Color.White, "Ka1", "Pa2", "pb2");
        assertEquals(squares("b1", "b2"), reachable(rule, position, "a1"));
    }

    @Test
    void castlingIsNotPartOfTheMovementRule() {
        Position position = Positions.of(Color.White, CastlingRights.initial(), "Ke1", "Rh1");
        assertFalse(rule.canMove(position, Square.at("e1"), Square.at("g1")));
    }
}
