package software.ulpgc.chess.domain;

public record Position(Board board, Color sideToMove, CastlingRights castlingRights, Square enPassantTarget, int halfMoveClock) {
    public static Position initial(){
        return new Position(Board.initial(), Color.White, CastlingRights.initial(), null, 0);
    }

    public Position move(Square from, Square to){
        return move(from, to, PieceType.Queen);
    }

    public Position move(Square from, Square to, PieceType promotion){
        Piece piece = board.pieceAt(from);
        Piece capturedPiece = board.pieceAt(to);
        Board newBoard = board.move(from, to);
        if (isEnPassantCapture(piece, to)) newBoard = newBoard.remove(new Square(to.file(), from.rank()));
        if (isCastling(piece, from, to)) newBoard = moveCastlingRook(newBoard, from, to);
        if (isPromotion(piece, to)) newBoard = newBoard.put(to, Piece.of(piece.color(), promotion));
        return new Position(
                newBoard,
                sideToMove.opposite(),
                castlingRightsAfter(piece, capturedPiece, from, to),
                enPassantTargetAfter(piece, from, to),
                halfMoveClockAfter(piece, capturedPiece)
        );
    }

    public boolean isPromotion(Square from, Square to) {
        return isPromotion(board.pieceAt(from), to);
    }

    public boolean isCastling(Square from, Square to) {
        return isCastling(board.pieceAt(from), from, to);
    }

    public Piece capturedPiece(Square from, Square to) {
        Piece piece = board.pieceAt(from);
        if (isEnPassantCapture(piece, to)) return board.pieceAt(new Square(to.file(), from.rank()));
        return board.pieceAt(to);
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

    private boolean isCastling(Piece piece, Square from, Square to) {
        return piece != null && piece.type() == PieceType.King
                && Math.abs(to.file().ordinal() - from.file().ordinal()) == 2;
    }

    private Board moveCastlingRook(Board board, Square from, Square to) {
        boolean kingSide = to.file() == File.G;
        Square rookFrom = new Square(kingSide ? File.H : File.A, from.rank());
        Square rookTo = new Square(kingSide ? File.F : File.D, from.rank());
        return board.move(rookFrom, rookTo);
    }

    private boolean isPromotion(Piece piece, Square to) {
        return piece != null && piece.type() == PieceType.Pawn
                && (to.rank() == Rank.R8 || to.rank() == Rank.R1);
    }

    private int halfMoveClockAfter(Piece piece, Piece capturedPiece) {
        boolean isPawnMove = piece != null && piece.type() == PieceType.Pawn;
        return isPawnMove || capturedPiece != null ? 0 : halfMoveClock + 1;
    }
}
