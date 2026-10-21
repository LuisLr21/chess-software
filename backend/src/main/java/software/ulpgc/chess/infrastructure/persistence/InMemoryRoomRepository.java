package software.ulpgc.chess.infrastructure.persistence;

import software.ulpgc.chess.application.RoomRepository;
import software.ulpgc.chess.domain.Room;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRoomRepository implements RoomRepository {
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    @Override
    public void save(Room room) {
        rooms.put(room.code(), room);
    }

    @Override
    public Optional<Room> findByCode(String code) {
        return Optional.ofNullable(rooms.get(code));
    }
}
