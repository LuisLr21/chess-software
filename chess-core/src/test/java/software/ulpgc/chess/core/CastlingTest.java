package software.ulpgc.chess.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CastlingTest {
    private final LegalMoveValidator validator = new LegalMoveValidator();

    private static Position withPieces(String... extra) {
        String[] pieces = new String[extra.length + 4];
        pieces[0] = "Ke1";
        pieces[1] = "Ra1";
        pieces[2] = "Rh1";
        pieces[3] = "ke8";
        System.arraycopy(extra, 0, pieces, 4, extra.length);
        return Positions.of(Color.White, CastlingRights.initial(), pieces);
    }

    private boolean canCastle(Position position, String to) {
        return validator.isLegal(position, Square.at("e1"), Square.at(to));
    }

    @Test
    void castlesOnBothSides() {
        assertTrue(canCastle(withPieces(), "g1"));
        assertTrue(canCastle(withPieces(), "c1"));
    }

    @Test
    void castlingMovesTheRookToo() {
        Position kingSide = withPieces().move(Square.at("e1"), Square.at("g1"));
        assertEquals(Piece.WhiteKing, kingSide.board().pieceAt("g1"));
        assertEquals(Piece.WhiteRook, kingSide.board().pieceAt("f1"));
        assertNull(kingSide.board().pieceAt("h1"));

        Position queenSide = withPieces().move(Square.at("e1"), Square.at("c1"));
        assertEquals(Piece.WhiteKing, queenSide.board().pieceAt("c1"));
        assertEquals(Piece.WhiteRook, queenSide.board().pieceAt("d1"));
        assertNull(queenSide.board().pieceAt("a1"));
    }

    @Test
    void cannotCastleWithoutTheRight() {
        Position position = Positions.of(Color.White, CastlingRights.initial().withoutWhiteKingSide(), "Ke1", "Ra1", "Rh1", "ke8");
        assertFalse(canCastle(position, "g1"));
        assertTrue(canCastle(position, "c1"));
    }

    @Test
    void cannotCastleThroughPieces() {
        assertFalse(canCastle(withPieces("Bf1"), "g1"));
        assertFalse(canCastle(withPieces("nb1"), "c1"));
    }

    @Test
    void cannotCastleOutOfCheck() {
        Position position = withPieces("re5");
        assertFalse(canCastle(position, "g1"));
        assertFalse(canCastle(position, "c1"));
    }

    @Test
    void cannotCastleThroughOrIntoAnAttackedSquare() {
        assertFalse(canCastle(withPieces("rf8"), "g1"));
        assertTrue(canCastle(withPieces("rf8"), "c1"));
        assertFalse(canCastle(withPieces("rg8"), "g1"));
        assertFalse(canCastle(withPieces("rc8"), "c1"));
    }

    @Test
    void theRookMayCrossAnAttackedSquare() {
        assertTrue(canCastle(withPieces("rb8"), "c1"));
    }

    @Test
    void movingTheKingOrARookLosesTheRights() {
        Position afterKing = withPieces().move(Square.at("e1"), Square.at("f1"));
        assertFalse(afterKing.castlingRights().whiteKingSide());
        assertFalse(afterKing.castlingRights().whiteQueenSide());

        Position afterRook = withPieces().move(Square.at("h1"), Square.at("h2"));
        assertFalse(afterRook.castlingRights().whiteKingSide());
        assertTrue(afterRook.castlingRights().whiteQueenSide());
    }

    @Test
    void losingTheRookLosesTheRight() {
        Position position = Positions.of(Color.White, CastlingRights.initial(), "Ke1", "Rh1", "ke8", "rh8");
        Position after = position.move(Square.at("h1"), Square.at("h8"));
        assertFalse(after.castlingRights().blackKingSide());
        assertTrue(after.castlingRights().blackQueenSide());
    }
}
