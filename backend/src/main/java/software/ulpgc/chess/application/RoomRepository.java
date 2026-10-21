package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.Room;

import java.util.Optional;

public interface RoomRepository {
    void save(Room room);
    Optional<Room> findByCode(String code);
}
