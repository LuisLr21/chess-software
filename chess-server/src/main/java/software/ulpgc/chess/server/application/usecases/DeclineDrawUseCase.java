package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.domain.Room;
import software.ulpgc.chess.server.application.ports.RoomNotifier;
import software.ulpgc.chess.server.application.ports.RoomRepository;

public class DeclineDrawUseCase implements UseCase<PlayerInRoom, RoomState> {
    private final RoomRepository rooms;
    private final GetRoomUseCase getRoom;
    private final RoomNotifier notifier;

    public DeclineDrawUseCase(RoomRepository rooms, GetRoomUseCase getRoom, RoomNotifier notifier) {
        this.rooms = rooms;
        this.getRoom = getRoom;
        this.notifier = notifier;
    }

    @Override
    public synchronized RoomState execute(PlayerInRoom request) {
        RoomState state = getRoom.execute(request.code());
        state.room().playerColor(request.player());
        Room room = state.room().withoutDrawOffer();
        rooms.save(room);
        RoomState newState = new RoomState(room, state.game());
        notifier.roomUpdated(newState);
        return newState;
    }
}
