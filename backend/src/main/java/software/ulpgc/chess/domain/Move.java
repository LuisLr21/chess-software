package software.ulpgc.chess.domain;

public record Move(Square from, Square to, PieceType promotion) {
    @Override
    public String toString() {
        return "" + from + to + (promotion == null ? "" : promotion.symbol());
    }
}
