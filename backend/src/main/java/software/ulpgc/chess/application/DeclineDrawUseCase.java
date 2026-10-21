package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.Room;

public class DeclineDrawUseCase {
    private final RoomRepository rooms;
    private final GetRoomUseCase getRoom;
    private final RoomNotifier notifier;

    public DeclineDrawUseCase(RoomRepository rooms, GetRoomUseCase getRoom, RoomNotifier notifier) {
        this.rooms = rooms;
        this.getRoom = getRoom;
        this.notifier = notifier;
    }

    public synchronized RoomState execute(String code, String player) {
        RoomState state = getRoom.execute(code);
        state.room().playerColor(player);
        Room room = state.room().withoutDrawOffer();
        rooms.save(room);
        RoomState newState = new RoomState(room, state.game());
        notifier.roomUpdated(newState);
        return newState;
    }
}
