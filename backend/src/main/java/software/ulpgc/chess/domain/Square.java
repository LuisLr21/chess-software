package software.ulpgc.chess.domain;

public record Square(File file, Rank rank) {
    public static Square at(String square) {
        if (square == null || square.length() != 2) {
            throw new IllegalArgumentException("Invalid square: " + square);
        }
        try {
            File file = File.fromSymbol(square.charAt(0));
            Rank rank = Rank.fromValue(Character.getNumericValue(square.charAt(1)));
            return new Square(file, rank);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid square: " + square, e);
        }
    }

    @Override
    public int hashCode() {
        return file.ordinal() * 8 + rank.ordinal();
    }

    @Override
    public String toString() {
        return "" + file.symbol() + rank.value();
    }
}
