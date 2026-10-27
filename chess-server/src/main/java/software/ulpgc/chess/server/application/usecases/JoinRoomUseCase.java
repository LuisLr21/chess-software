package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.core.Color;
import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.domain.Room;
import software.ulpgc.chess.server.application.domain.Seat;
import software.ulpgc.chess.server.application.ports.GameRepository;
import software.ulpgc.chess.server.application.ports.RoomNotifier;
import software.ulpgc.chess.server.application.ports.RoomRepository;

import java.time.Clock;
import java.util.UUID;

public class JoinRoomUseCase implements UseCase<RoomEntry, PlayerSession> {
    private final GameRepository games;
    private final RoomRepository rooms;
    private final RoomNotifier notifier;
    private final Clock clock;

    public JoinRoomUseCase(GameRepository games, RoomRepository rooms, RoomNotifier notifier, Clock clock) {
        this.games = games;
        this.rooms = rooms;
        this.notifier = notifier;
        this.clock = clock;
    }

    @Override
    public synchronized PlayerSession execute(RoomEntry entry) {
        Room room = rooms.findByCode(entry.code()).orElseThrow(() -> new RoomNotFoundException(entry.code()));
        String player = UUID.randomUUID().toString();
        Room joined = room.join(Seat.of(player, entry.name(), "Negras"));
        rooms.save(joined);
        Game game = games.findById(joined.gameId())
                .orElseThrow(() -> new GameNotFoundException(joined.gameId()))
                .startClock(clock.instant());
        games.save(game);
        RoomState state = new RoomState(joined, game);
        notifier.roomUpdated(state);
        return new PlayerSession(state, Color.Black, player);
    }
}
