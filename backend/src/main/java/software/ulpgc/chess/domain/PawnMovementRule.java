package software.ulpgc.chess.domain;

public class PawnMovementRule implements MovementRule{

    @Override
    public boolean canMove(Position position, Square from, Square to) {
        Board board = position.board();
        Piece movingPiece = board.pieceAt(from);
        if (movingPiece == null || movingPiece.type() != PieceType.Pawn) {
            return false;
        }
        if (isOccupiedBySameColor(board, to, movingPiece.color())){
            return false;
        }
        int direction = movingPiece.color() == Color.White ? 1 : -1;
        int startRank = movingPiece.color() == Color.White ? 2 : 7;
        int fileDiff = Math.abs(to.file().ordinal() - from.file().ordinal());
        int rankDelta = to.rank().value() - from.rank().value();

        return isSingleStep(board, to, fileDiff, rankDelta, direction)
                || isDoubleStep(board, from, to, fileDiff, rankDelta, direction, startRank)
                || isCapture(position, to, fileDiff, rankDelta, direction);
    }

    private boolean isSingleStep(Board board, Square to, int fileDiff, int rankDelta, int direction){
        return fileDiff == 0
                && rankDelta == direction
                && board.pieceAt(to) == null;
    }

    private boolean isDoubleStep(Board board, Square from, Square to, int fileDiff, int rankDelta, int direction, int startRank){
        return fileDiff == 0
                && rankDelta == 2 * direction
                && from.rank().value() == startRank
                && board.pieceAt(to) == null
                && isPathClear(board, from, to);
    }

    private boolean isCapture(Position position, Square to, int fileDiff, int rankDelta, int direction){
        Board board = position.board();
        return fileDiff == 1
                && rankDelta == direction
                && (board.pieceAt(to) != null || to.equals(position.enPassantTarget()));
    }
}
