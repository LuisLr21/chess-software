package software.ulpgc.chess.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static software.ulpgc.chess.core.Positions.reachable;
import static software.ulpgc.chess.core.Positions.squares;

class SlidingPiecesMovementRuleTest {
    private final MovementRule bishop = new BishopMovementRule();
    private final MovementRule rook = new RookMovementRule();
    private final MovementRule queen = new QueenMovementRule();

    @Test
    void bishopMovesAlongDiagonals() {
        Position position = Positions.of(Color.White, "Bd4");
        assertEquals(squares("a1", "b2", "c3", "e5", "f6", "g7", "h8", "a7", "b6", "c5", "e3", "f2", "g1"),
                reachable(bishop, position, "d4"));
    }

    @Test
    void bishopStopsBeforeOwnPiecesAndOnOpponentPieces() {
        Position position = Positions.of(Color.White, "Bd4", "Pf6", "pb2");
        assertTrue(bishop.canMove(position, Square.at("d4"), Square.at("e5")));
        assertFalse(bishop.canMove(position, Square.at("d4"), Square.at("f6")));
        assertFalse(bishop.canMove(position, Square.at("d4"), Square.at("g7")));
        assertTrue(bishop.canMove(position, Square.at("d4"), Square.at("b2")));
        assertFalse(bishop.canMove(position, Square.at("d4"), Square.at("a1")));
    }

    @Test
    void rookMovesAlongFilesAndRanks() {
        Position position = Positions.of(Color.White, "Rd4");
        assertEquals(squares("d1", "d2", "d3", "d5", "d6", "d7", "d8", "a4", "b4", "c4", "e4", "f4", "g4", "h4"),
                reachable(rook, position, "d4"));
    }

    @Test
    void rookCannotJumpOverPieces() {
        Position position = Positions.of(Color.White, "Ra1", "pa4");
        assertTrue(rook.canMove(position, Square.at("a1"), Square.at("a4")));
        assertFalse(rook.canMove(position, Square.at("a1"), Square.at("a5")));
        assertFalse(rook.canMove(position, Square.at("a1"), Square.at("b2")));
    }

    @Test
    void queenCombinesRookAndBishop() {
        Position position = Positions.of(Color.White, "Qd4");
        assertEquals(27, reachable(queen, position, "d4").size());
        assertTrue(queen.canMove(position, Square.at("d4"), Square.at("h8")));
        assertTrue(queen.canMove(position, Square.at("d4"), Square.at("d8")));
        assertFalse(queen.canMove(position, Square.at("d4"), Square.at("e6")));
    }

    @Test
    void queenIsBlockedLikeOtherSlidingPieces() {
        Position position = Positions.of(Color.Black, "qd8", "pd7", "Pe7");
        assertFalse(queen.canMove(position, Square.at("d8"), Square.at("d5")));
        assertTrue(queen.canMove(position, Square.at("d8"), Square.at("e7")));
        assertFalse(queen.canMove(position, Square.at("d8"), Square.at("f6")));
    }
}
