package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.core.PieceType;
import software.ulpgc.chess.core.Square;

public record GameMove(String gameId, Square from, Square to, PieceType promotion) {
}
