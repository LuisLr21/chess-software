package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.ChessClock;
import software.ulpgc.chess.domain.Color;
import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.Room;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

public class RematchUseCase {
    private final GameRepository games;
    private final RoomRepository rooms;
    private final GetRoomUseCase getRoom;
    private final RoomNotifier notifier;
    private final Clock clock;

    public RematchUseCase(GameRepository games, RoomRepository rooms, GetRoomUseCase getRoom, RoomNotifier notifier, Clock clock) {
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
        Color color = room.playerColor(player);
        Game game = state.game();
        if (!game.isOver()) throw new GameNotOverException();
        if (room.rematchOfferedBy() == color.opposite()) {
            game = Game.start(UUID.randomUUID().toString(), freshClock(game.clock())).startClock(clock.instant());
            games.save(game);
            room = room.rematch(game.id());
        } else {
            room = room.withRematchOffer(color);
        }
        rooms.save(room);
        RoomState newState = new RoomState(room, game);
        notifier.roomUpdated(newState);
        return newState;
    }

    private ChessClock freshClock(ChessClock previous) {
        return previous == null ? null : ChessClock.of(Duration.ofMillis(previous.initialMillis()));
    }
}
