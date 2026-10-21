package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.Color;

public record PlayerSession(RoomState state, Color color, String playerToken) {
}
