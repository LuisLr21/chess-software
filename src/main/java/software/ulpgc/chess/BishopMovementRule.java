package software.ulpgc.chess;

public class BishopMovementRule implements MovementRule{

    @Override
    public boolean canMove(Board board, Square from, Square to){
        Piece movingPiece = board.pieceAt(from);
        if (movingPiece == null || movingPiece.type() != PieceType.Bishop){
            return false;
        }
        if (isOccupiedBySameColor(board, to, movingPiece.color())){
            return false;
        }
        int fileDiff = Math.abs(to.file().ordinal() - from.file().ordinal());
        int rankDiff = Math.abs(to.rank().value() - from.rank().value());
        if (fileDiff != rankDiff) return false;
        return isPathClear(board, from, to);
    }
}
