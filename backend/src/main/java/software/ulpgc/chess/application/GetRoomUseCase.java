package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.Room;

import java.time.Clock;

public class GetRoomUseCase {
    private final GameRepository games;
    private final RoomRepository rooms;
    private final RoomNotifier notifier;
    private final Clock clock;

    public GetRoomUseCase(GameRepository games, RoomRepository rooms, RoomNotifier notifier, Clock clock) {
        this.games = games;
        this.rooms = rooms;
        this.notifier = notifier;
        this.clock = clock;
    }

    public RoomState execute(String code) {
        Room room = rooms.findByCode(code).orElseThrow(() -> new RoomNotFoundException(code));
        Game game = games.findById(room.gameId()).orElseThrow(() -> new GameNotFoundException(room.gameId()));
        Game checked = game.flagTimeout(clock.instant());
        RoomState state = new RoomState(room, checked);
        if (checked != game) {
            games.save(checked);
            notifier.roomUpdated(state);
        }
        return state;
    }
}
