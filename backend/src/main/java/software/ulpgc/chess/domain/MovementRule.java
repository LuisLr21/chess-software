package software.ulpgc.chess.domain;


public interface MovementRule {
    boolean canMove(Position position, Square from, Square to);

    default boolean isOccupiedBySameColor(Board board, Square square, Color color){
        Piece piece = board.pieceAt(square);
        return piece != null && piece.color() == color;
    }

    default boolean isPathClear(Board board, Square from, Square to){
        int fileStep = Integer.signum(to.file().ordinal() - from.file().ordinal());
        int rankStep = Integer.signum(to.rank().value() - from.rank().value());

        int file = from.file().ordinal() + fileStep;
        int rank = from.rank().value() + rankStep;

        while (file != to.file().ordinal() || rank != to.rank().value()){
            Square square = new Square(File.values()[file], Rank.fromValue(rank));
            if (board.pieceAt(square) != null) return false;
            file += fileStep;
            rank += rankStep;
        }
        return true;
    }
}
