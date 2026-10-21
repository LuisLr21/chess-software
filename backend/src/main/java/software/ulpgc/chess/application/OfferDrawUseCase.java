package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.Color;
import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.GameOverException;
import software.ulpgc.chess.domain.Room;

import java.time.Clock;

public class OfferDrawUseCase {
    private final GameRepository games;
    private final RoomRepository rooms;
    private final GetRoomUseCase getRoom;
    private final RoomNotifier notifier;
    private final Clock clock;

    public OfferDrawUseCase(GameRepository games, RoomRepository rooms, GetRoomUseCase getRoom, RoomNotifier notifier, Clock clock) {
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
        if (game.isOver()) throw new GameOverException(game.status());
        if (room.drawOfferedBy() == color.opposite()) {
            game = game.agreeDraw(clock.instant());
            games.save(game);
            room = room.withoutDrawOffer();
        } else {
            room = room.withDrawOffer(color);
        }
        rooms.save(room);
        RoomState newState = new RoomState(room, game);
        notifier.roomUpdated(newState);
        return newState;
    }
}
