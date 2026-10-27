package software.ulpgc.chess.core;

public record Move(Square from, Square to, PieceType promotion) {
    public static Move fromUci(String uci) {
        if (uci == null || (uci.length() != 4 && uci.length() != 5)) {
            throw new IllegalArgumentException("Invalid move: " + uci);
        }
        PieceType promotion = uci.length() == 5 ? PieceType.fromSymbol(uci.charAt(4)) : null;
        return new Move(Square.at(uci.substring(0, 2)), Square.at(uci.substring(2, 4)), promotion);
    }

    @Override
    public String toString() {
        return "" + from + to + (promotion == null ? "" : promotion.symbol());
    }
}
