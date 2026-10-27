package software.ulpgc.chess.core;

public class RookMovementRule implements MovementRule{

    @Override
    public boolean canMove(Position position, Square from, Square to){
        Board board = position.board();
        Piece movingPiece = board.pieceAt(from);
        if (movingPiece == null || movingPiece.type() != PieceType.Rook){
            return false;
        }
        if (isOccupiedBySameColor(board, to, movingPiece.color())){
            return false;
        }
        int fileDiff = Math.abs(to.file().ordinal() - from.file().ordinal());
        int rankDiff = Math.abs(to.rank().value() - from.rank().value());
        if (fileDiff != 0 && rankDiff != 0) return false;
        return isPathClear(board, from, to);
    }
}
