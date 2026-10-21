package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.Color;
import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.Room;
import software.ulpgc.chess.domain.Seat;

import java.time.Clock;
import java.util.UUID;

public class JoinRoomUseCase {
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

    public synchronized PlayerSession execute(String code, String name) {
        Room room = rooms.findByCode(code).orElseThrow(() -> new RoomNotFoundException(code));
        String player = UUID.randomUUID().toString();
        Room joined = room.join(Seat.of(player, name, "Negras"));
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
