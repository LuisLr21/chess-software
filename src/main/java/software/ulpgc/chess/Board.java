package software.ulpgc.chess;

import java.util.HashMap;
import java.util.Map;

public final class Board {
    private final Map<Square, Piece> pieces;
    private final CastlingRights castlingRights;
    private final Square enPassantTarget;

    public Board(Map<Square, Piece> pieces){
        this(pieces, CastlingRights.initial(), null);
    }

    public Board(Map<Square, Piece> pieces, CastlingRights castlingRights){
        this(pieces, castlingRights, null);
    }

    public Board(Map<Square, Piece> pieces, CastlingRights castlingRights, Square enPassantTarget){
        this.pieces = Map.copyOf(pieces);
        this.castlingRights = castlingRights;
        this.enPassantTarget = enPassantTarget;
    }

    public static Board initial() {
        return new Board(initialPieces());
    }

    private static Map<Square, Piece> initialPieces() {
        return Map.ofEntries(
                Map.entry(Square.at("a1"), Piece.WhiteRook),
                Map.entry(Square.at("b1"), Piece.WhiteKnight),
                Map.entry(Square.at("c1"), Piece.WhiteBishop),
                Map.entry(Square.at("d1"), Piece.WhiteQueen),
                Map.entry(Square.at("e1"), Piece.WhiteKing),
                Map.entry(Square.at("f1"), Piece.WhiteBishop),
                Map.entry(Square.at("g1"), Piece.WhiteKnight),
                Map.entry(Square.at("h1"), Piece.WhiteRook),
                Map.entry(Square.at("a2"), Piece.WhitePawn),
                Map.entry(Square.at("b2"), Piece.WhitePawn),
                Map.entry(Square.at("c2"), Piece.WhitePawn),
                Map.entry(Square.at("d2"), Piece.WhitePawn),
                Map.entry(Square.at("e2"), Piece.WhitePawn),
                Map.entry(Square.at("f2"), Piece.WhitePawn),
                Map.entry(Square.at("g2"), Piece.WhitePawn),
                Map.entry(Square.at("h2"), Piece.WhitePawn),
                Map.entry(Square.at("a7"), Piece.BlackPawn),
                Map.entry(Square.at("b7"), Piece.BlackPawn),
                Map.entry(Square.at("c7"), Piece.BlackPawn),
                Map.entry(Square.at("d7"), Piece.BlackPawn),
                Map.entry(Square.at("e7"), Piece.BlackPawn),
                Map.entry(Square.at("f7"), Piece.BlackPawn),
                Map.entry(Square.at("g7"), Piece.BlackPawn),
                Map.entry(Square.at("h7"), Piece.BlackPawn),
                Map.entry(Square.at("a8"), Piece.BlackRook),
                Map.entry(Square.at("b8"), Piece.BlackKnight),
                Map.entry(Square.at("c8"), Piece.BlackBishop),
                Map.entry(Square.at("d8"), Piece.BlackQueen),
                Map.entry(Square.at("e8"), Piece.BlackKing),
                Map.entry(Square.at("f8"), Piece.BlackBishop),
                Map.entry(Square.at("g8"), Piece.BlackKnight),
                Map.entry(Square.at("h8"), Piece.BlackRook)
        );
    }

    public Piece pieceAt(Square square) {
        return pieces.get(square);
    }

    public Piece pieceAt(String square) {
        return pieceAt(Square.at(square));
    }

    public Map<Square, Piece> pieces() {
        return pieces;
    }

    public CastlingRights castlingRights() {
        return castlingRights;
    }

    public Square enPassantTarget() {
        return enPassantTarget;
    }

    public int pieceCount() {
        return pieces.size();
    }

    public Board move(Square from, Square to){
        Piece piece = pieces.get(from);
        Piece capturedPiece = pieces.get(to);

        Map<Square, Piece> newPieces = new HashMap<>(pieces);
        newPieces.remove(from);
        newPieces.put(to, piece);

        CastlingRights newRights = castlingRights;

        if (piece == Piece.WhiteKing) newRights = newRights.withoutWhiteCastling();
        if (piece == Piece.BlackKing) newRights = newRights.withoutBlackCastling();

        if (piece == Piece.WhiteRook){
            if (from.equals(Square.at("a1"))) newRights = newRights.withoutWhiteQueenSide();
            if (from.equals(Square.at("h1"))) newRights = newRights.withoutWhiteKingSide();
        }
        if (piece == Piece.BlackRook){
            if (from.equals(Square.at("a8"))) newRights = newRights.withoutBlackQueenSide();
            if (from.equals(Square.at("h8"))) newRights = newRights.withoutBlackKingSide();
        }

        if (capturedPiece == Piece.WhiteRook){
            if (to.equals(Square.at("a1"))) newRights = newRights.withoutWhiteQueenSide();
            if (to.equals(Square.at("h1"))) newRights = newRights.withoutWhiteKingSide();
        }
        if (capturedPiece == Piece.BlackRook){
            if (to.equals(Square.at("a8"))) newRights = newRights.withoutBlackQueenSide();
            if (to.equals(Square.at("h8"))) newRights = newRights.withoutBlackKingSide();
        }

        Square newEnPassantTarget = null;

        if (piece != null && piece.type() == PieceType.Pawn && Math.abs(to.rank().value() - from.rank().value()) == 2){
            int middleRankValue = (from.rank().value() + to.rank().value()) / 2;
            newEnPassantTarget = new Square(from.file(), Rank.fromValue(middleRankValue));
        }
        return new Board(newPieces, newRights, newEnPassantTarget);
    }
}
