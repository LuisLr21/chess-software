package software.ulpgc.chess.server.application.ports;

import software.ulpgc.chess.server.application.domain.Room;

import java.util.Optional;

public interface RoomRepository {
    void save(Room room);
    Optional<Room> findByCode(String code);
}
