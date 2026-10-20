package software.ulpgc.chess;

public record Position(Board board, Color sideToMove, CastlingRights castlingRights, Square enPassantTarget) {
    public static Position initial(){
        return new Position(Board.initial(), Color.White, CastlingRights.initial(), null);
    }

    public Position move(Square from, Square to){
        Piece piece = board.pieceAt(from);
        Piece capturedPiece = board.pieceAt(to);
        Board newBoard = board.move(from, to);
        if (isEnPassantCapture(piece, to)) newBoard = newBoard.remove(new Square(to.file(), from.rank()));
        return new Position(
                newBoard,
                sideToMove.opposite(),
                castlingRightsAfter(piece, capturedPiece, from, to),
                enPassantTargetAfter(piece, from, to)
        );
    }

    private CastlingRights castlingRightsAfter(Piece piece, Piece capturedPiece, Square from, Square to){
        CastlingRights rights = castlingRights;
        if (piece == Piece.WhiteKing) rights = rights.withoutWhiteCastling();
        if (piece == Piece.BlackKing) rights = rights.withoutBlackCastling();

        if (piece == Piece.WhiteRook){
            if (from.equals(Square.at("a1"))) rights = rights.withoutWhiteQueenSide();
            if (from.equals(Square.at("h1"))) rights = rights.withoutWhiteKingSide();
        }
        if (piece == Piece.BlackRook){
            if (from.equals(Square.at("a8"))) rights = rights.withoutBlackQueenSide();
            if (from.equals(Square.at("h8"))) rights = rights.withoutBlackKingSide();
        }

        if (capturedPiece == Piece.WhiteRook){
            if (to.equals(Square.at("a1"))) rights = rights.withoutWhiteQueenSide();
            if (to.equals(Square.at("h1"))) rights = rights.withoutWhiteKingSide();
        }
        if (capturedPiece == Piece.BlackRook){
            if (to.equals(Square.at("a8"))) rights = rights.withoutBlackQueenSide();
            if (to.equals(Square.at("h8"))) rights = rights.withoutBlackKingSide();
        }
        return rights;
    }

    private Square enPassantTargetAfter(Piece piece, Square from, Square to){
        if (piece == null || piece.type() != PieceType.Pawn) return null;
        if (Math.abs(to.rank().value() - from.rank().value()) != 2) return null;
        int middleRankValue = (from.rank().value() + to.rank().value()) / 2;
        return new Square(from.file(), Rank.fromValue(middleRankValue));
    }

    private boolean isEnPassantCapture(Piece piece, Square to) {
        return piece != null && piece.type() == PieceType.Pawn && to.equals(enPassantTarget);
    }
}
