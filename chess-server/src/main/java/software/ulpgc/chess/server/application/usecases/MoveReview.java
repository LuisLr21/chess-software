package software.ulpgc.chess.server.application.usecases;

public record MoveReview(String played, String suggested) {
    public boolean isBest() {
        return played.equals(suggested);
    }
}
