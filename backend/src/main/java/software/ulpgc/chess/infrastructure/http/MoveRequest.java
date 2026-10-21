package software.ulpgc.chess.infrastructure.http;

import software.ulpgc.chess.domain.PieceType;

public record MoveRequest(String from, String to, String promotion) {
    public PieceType promotionType() {
        if (promotion == null || promotion.isBlank()) return PieceType.Queen;
        return PieceType.fromSymbol(promotion.charAt(0));
    }
}
