package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.PieceType;
import software.ulpgc.chess.domain.Room;
import software.ulpgc.chess.domain.Square;

import java.time.Clock;

public class MoveInRoomUseCase {
    private final GameRepository games;
    private final RoomRepository rooms;
    private final GetRoomUseCase getRoom;
    private final RoomNotifier notifier;
    private final Clock clock;

    public MoveInRoomUseCase(GameRepository games, RoomRepository rooms, GetRoomUseCase getRoom, RoomNotifier notifier, Clock clock) {
        this.games = games;
        this.rooms = rooms;
        this.getRoom = getRoom;
        this.notifier = notifier;
        this.clock = clock;
    }

    public synchronized RoomState execute(String code, String player, Square from, Square to, PieceType promotion) {
        RoomState state = getRoom.execute(code);
        Room room = state.room();
        room.ensureCanMove(player, state.game().position().sideToMove());
        Game game = state.game().play(from, to, promotion, clock.instant());
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
