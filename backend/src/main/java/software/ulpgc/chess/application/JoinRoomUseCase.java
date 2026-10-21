package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.Color;
import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.Room;

import java.util.UUID;

public class JoinRoomUseCase {
    private final GameRepository games;
    private final RoomRepository rooms;
    private final RoomNotifier notifier;

    public JoinRoomUseCase(GameRepository games, RoomRepository rooms, RoomNotifier notifier) {
        this.games = games;
        this.rooms = rooms;
        this.notifier = notifier;
    }

    public synchronized PlayerSession execute(String code) {
        Room room = rooms.findByCode(code).orElseThrow(() -> new RoomNotFoundException(code));
        String player = UUID.randomUUID().toString();
        Room joined = room.join(player);
        rooms.save(joined);
        Game game = games.findById(joined.gameId()).orElseThrow(() -> new GameNotFoundException(joined.gameId()));
        RoomState state = new RoomState(joined, game);
        notifier.roomUpdated(state);
        return new PlayerSession(state, Color.Black, player);
    }
}
