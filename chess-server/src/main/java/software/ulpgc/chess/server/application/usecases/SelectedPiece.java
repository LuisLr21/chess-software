package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.core.Square;

public record SelectedPiece(String gameId, Square from) {
}
