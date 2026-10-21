package software.ulpgc.chess.domain;

import java.util.Optional;

public record Room(String code, String gameId, String whitePlayer, String blackPlayer) {

    public static Room open(String code, String gameId, String whitePlayer) {
        return new Room(code, gameId, whitePlayer, null);
    }

    public boolean isFull() {
        return blackPlayer != null;
    }

    public Room join(String player) {
        if (isFull()) throw new RoomFullException(code);
        return new Room(code, gameId, whitePlayer, player);
    }

    public Optional<Color> colorOf(String player) {
        if (player == null) return Optional.empty();
        if (player.equals(whitePlayer)) return Optional.of(Color.White);
        if (player.equals(blackPlayer)) return Optional.of(Color.Black);
        return Optional.empty();
    }

    public void ensureCanMove(String player, Color sideToMove) {
        if (!isFull()) throw new WaitingForOpponentException(code);
        Color color = colorOf(player).orElseThrow(() -> new NotYourTurnException("You are not a player of room " + code));
        if (color != sideToMove) throw new NotYourTurnException("It is not your turn");
    }
}
