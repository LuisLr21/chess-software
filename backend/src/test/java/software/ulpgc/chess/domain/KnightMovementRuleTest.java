package software.ulpgc.chess.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static software.ulpgc.chess.domain.Positions.reachable;
import static software.ulpgc.chess.domain.Positions.squares;

class KnightMovementRuleTest {
    private final MovementRule rule = new KnightMovementRule();

    @Test
    void movesInLShape() {
        Position position = Positions.of(Color.White, "Nd4");
        assertEquals(squares("b3", "b5", "c2", "c6", "e2", "e6", "f3", "f5"), reachable(rule, position, "d4"));
    }

    @Test
    void hasFewerMovesInTheCorner() {
        Position position = Positions.of(Color.White, "Na1");
        assertEquals(squares("b3", "c2"), reachable(rule, position, "a1"));
    }

    @Test
    void jumpsOverPieces() {
        Position position = Positions.of(Color.White, "Nb1", "Pa2", "Pb2", "Pc2", "Pd2");
        assertEquals(squares("a3", "c3"), reachable(rule, position, "b1"));
    }

    @Test
    void capturesOpponentPiecesButNotOwnPieces() {
        Position position = Positions.of(Color.White, "Nd4", "pe6", "Pf5");
        assertTrue(rule.canMove(position, Square.at("d4"), Square.at("e6")));
        assertFalse(rule.canMove(position, Square.at("d4"), Square.at("f5")));
    }

    @Test
    void onlyMovesKnights() {
        Position position = Positions.of(Color.White, "Bd4");
        assertTrue(reachable(rule, position, "d4").isEmpty());
    }
}
