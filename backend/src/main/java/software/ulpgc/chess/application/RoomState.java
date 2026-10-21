package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.Room;

public record RoomState(Room room, Game game) {
}
