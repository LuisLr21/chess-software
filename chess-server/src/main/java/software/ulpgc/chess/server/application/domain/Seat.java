package software.ulpgc.chess.server.application.domain;

public record Seat(String player, String name) {
    private static final int MAX_NAME_LENGTH = 16;

    public static Seat of(String player, String name, String fallback) {
        String clean = name == null || name.isBlank() ? fallback : name.trim();
        return new Seat(player, clean.length() > MAX_NAME_LENGTH ? clean.substring(0, MAX_NAME_LENGTH) : clean);
    }
}
