package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.core.Color;

public record PlayerSession(RoomState state, Color color, String playerToken) {
}
