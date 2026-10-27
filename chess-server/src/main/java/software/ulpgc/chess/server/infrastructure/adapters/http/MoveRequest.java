package software.ulpgc.chess.server.infrastructure.adapters.http;

import software.ulpgc.chess.core.PieceType;

public record MoveRequest(String from, String to, String promotion) {
    public PieceType promotionType() {
        if (promotion == null || promotion.isBlank()) return PieceType.Queen;
        return PieceType.fromSymbol(promotion.charAt(0));
    }
}
