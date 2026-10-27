package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.domain.Room;
import software.ulpgc.chess.server.application.ports.GameRepository;
import software.ulpgc.chess.server.application.ports.RoomNotifier;
import software.ulpgc.chess.server.application.ports.RoomRepository;

import java.time.Clock;

public class ResignUseCase implements UseCase<PlayerInRoom, RoomState> {
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

    @Override
    public synchronized RoomState execute(PlayerInRoom request) {
        RoomState state = getRoom.execute(request.code());
        Room room = state.room();
        room.ensureStarted();
        Game game = state.game().resign(room.playerColor(request.player()), clock.instant());
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
