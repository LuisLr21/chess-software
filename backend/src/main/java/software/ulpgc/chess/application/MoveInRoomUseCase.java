package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.PieceType;
import software.ulpgc.chess.domain.Square;

public class MoveInRoomUseCase {
    private final GameRepository games;
    private final GetRoomUseCase getRoom;
    private final RoomNotifier notifier;

    public MoveInRoomUseCase(GameRepository games, GetRoomUseCase getRoom, RoomNotifier notifier) {
        this.games = games;
        this.getRoom = getRoom;
        this.notifier = notifier;
    }

    public synchronized RoomState execute(String code, String player, Square from, Square to, PieceType promotion) {
        RoomState state = getRoom.execute(code);
        state.room().ensureCanMove(player, state.game().position().sideToMove());
        Game game = state.game().play(from, to, promotion);
        games.save(game);
        RoomState newState = new RoomState(state.room(), game);
        notifier.roomUpdated(newState);
        return newState;
    }
}
