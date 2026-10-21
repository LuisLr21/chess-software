package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.Room;

import java.time.Clock;

public class ResignUseCase {
    private final GameRepository games;
    private final RoomRepository rooms;
    private final GetRoomUseCase getRoom;
    private final RoomNotifier notifier;
    private final Clock clock;

    public ResignUseCase(GameRepository games, RoomRepository rooms, GetRoomUseCase getRoom, RoomNotifier notifier, Clock clock) {
        this.games = games;
        this.rooms = rooms;
        this.getRoom = getRoom;
        this.notifier = notifier;
        this.clock = clock;
    }

    public synchronized RoomState execute(String code, String player) {
        RoomState state = getRoom.execute(code);
        Room room = state.room();
        room.ensureStarted();
        Game game = state.game().resign(room.playerColor(player), clock.instant());
        games.save(game);
        if (room.drawOfferedBy() != null) {
            room = room.withoutDrawOffer();
            rooms.save(room);
        }
        RoomState newState = new RoomState(room, game);
        notifier.roomUpdated(newState);
        return newState;
    }
}
