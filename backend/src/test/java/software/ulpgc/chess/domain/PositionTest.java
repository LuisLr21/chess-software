package software.ulpgc.chess.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PositionTest {
    @Test
    void initialPosition() {
        Position position = Position.initial();
        assertEquals(Color.White, position.sideToMove());
        assertEquals(32, position.board().pieceCount());
        assertEquals(CastlingRights.initial(), position.castlingRights());
        assertNull(position.enPassantTarget());
        assertEquals(0, position.halfMoveClock());
    }

    @Test
    void movingPassesTheTurnAndLeavesTheOriginalUntouched() {
        Position initial = Position.initial();
        Position after = initial.move(Square.at("g1"), Square.at("f3"));
        assertEquals(Color.Black, after.sideToMove());
        assertEquals(Piece.WhiteKnight, after.board().pieceAt("f3"));
        assertNull(after.board().pieceAt("g1"));
        assertEquals(Piece.WhiteKnight, initial.board().pieceAt("g1"));
    }

    @Test
    void aDoubleStepLeavesAnEnPassantTarget() {
        assertEquals(Square.at("e3"), Position.initial().move(Square.at("e2"), Square.at("e4")).enPassantTarget());
        assertNull(Position.initial().move(Square.at("e2"), Square.at("e3")).enPassantTarget());
    }

    @Test
    void enPassantRemovesTheCapturedPawn() {
        Position position = new Position(Positions.board("Pe5", "pd5"), Color.White, Positions.NO_CASTLING, Square.at("d6"), 0);
        assertEquals(Piece.BlackPawn, position.capturedPiece(Square.at("e5"), Square.at("d6")));

        Position after = position.move(Square.at("e5"), Square.at("d6"));
        assertEquals(Piece.WhitePawn, after.board().pieceAt("d6"));
        assertNull(after.board().pieceAt("d5"));
    }

    @Test
    void aPawnOnTheLastRankIsPromoted() {
        Position position = Positions.of(Color.White, "Pe7");
        assertEquals(Piece.WhiteKnight, position.move(Square.at("e7"), Square.at("e8"), PieceType.Knight).board().pieceAt("e8"));
        assertEquals(Piece.WhiteQueen, position.move(Square.at("e7"), Square.at("e8")).board().pieceAt("e8"));
    }

    @Test
    void theHalfMoveClockResetsOnPawnMovesAndCaptures() {
        Position quiet = Position.initial().move(Square.at("g1"), Square.at("f3"));
        assertEquals(1, quiet.halfMoveClock());
        assertEquals(0, quiet.move(Square.at("e7"), Square.at("e5")).halfMoveClock());

        Position capture = Positions.of(Color.White, "Rd1", "nd8").move(Square.at("d1"), Square.at("d8"));
        assertEquals(0, capture.halfMoveClock());
    }
}
