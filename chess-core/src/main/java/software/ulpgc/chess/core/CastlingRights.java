package software.ulpgc.chess.core;

public record CastlingRights(
        boolean whiteKingSide,
        boolean whiteQueenSide,
        boolean blackKingSide,
        boolean blackQueenSide
) {
    public static CastlingRights initial(){
        return new CastlingRights(true, true, true, true);
    }

    public CastlingRights withoutWhiteKingSide(){
        return new CastlingRights(false, whiteQueenSide, blackKingSide, blackQueenSide);
    }

    public CastlingRights withoutWhiteQueenSide(){
        return new CastlingRights(whiteKingSide, false, blackKingSide, blackQueenSide);
    }

    public CastlingRights withoutWhiteCastling(){
        return new CastlingRights(false, false, blackKingSide, blackQueenSide);
    }

    public CastlingRights withoutBlackKingSide(){
        return new CastlingRights(whiteKingSide, whiteQueenSide, false, blackQueenSide);
    }

    public CastlingRights withoutBlackQueenSide(){
        return new CastlingRights(whiteKingSide, whiteQueenSide, blackKingSide, false);
    }

    public CastlingRights withoutBlackCastling(){
        return new CastlingRights(whiteKingSide, whiteQueenSide, false, false);
    }
}
