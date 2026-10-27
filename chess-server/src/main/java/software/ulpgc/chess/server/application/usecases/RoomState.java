package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.server.application.domain.Room;

public record RoomState(Room room, Game game) {
}
