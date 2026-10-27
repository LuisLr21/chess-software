package software.ulpgc.chess.server.infrastructure.adapters.persistence;

import software.ulpgc.chess.server.application.domain.Room;
import software.ulpgc.chess.server.application.ports.RoomRepository;

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
