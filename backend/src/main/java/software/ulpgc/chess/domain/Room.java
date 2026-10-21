package software.ulpgc.chess.domain;

import java.util.Optional;

public record Room(String code, String gameId, Seat white, Seat black, Color drawOfferedBy, Color rematchOfferedBy) {

    public static Room open(String code, String gameId, Seat white) {
        return new Room(code, gameId, white, null, null, null);
    }

    public boolean isFull() {
        return black != null;
    }

    public Room join(Seat seat) {
        if (isFull()) throw new RoomFullException(code);
        return new Room(code, gameId, white, seat, null, null);
    }

    public Optional<Color> colorOf(String player) {
        if (player == null) return Optional.empty();
        if (player.equals(white.player())) return Optional.of(Color.White);
        if (isFull() && player.equals(black.player())) return Optional.of(Color.Black);
        return Optional.empty();
    }

    public Color playerColor(String player) {
        return colorOf(player).orElseThrow(() -> new NotAPlayerException(code));
    }

    public void ensureCanMove(String player, Color sideToMove) {
        if (!isFull()) throw new WaitingForOpponentException(code);
        if (playerColor(player) != sideToMove) throw new NotYourTurnException("It is not your turn");
    }

    public void ensureStarted() {
        if (!isFull()) throw new WaitingForOpponentException(code);
    }

    public Room withDrawOffer(Color color) {
        return new Room(code, gameId, white, black, color, rematchOfferedBy);
    }

    public Room withoutDrawOffer() {
        return new Room(code, gameId, white, black, null, rematchOfferedBy);
    }

    public Room withRematchOffer(Color color) {
        return new Room(code, gameId, white, black, drawOfferedBy, color);
    }

    public Room rematch(String newGameId) {
        return new Room(code, newGameId, black, white, null, null);
    }
}
