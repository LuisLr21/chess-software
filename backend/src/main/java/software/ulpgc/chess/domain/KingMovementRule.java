package software.ulpgc.chess.domain;

public class KingMovementRule implements MovementRule{

    @Override
    public boolean canMove(Position position, Square from, Square to){
        Board board = position.board();
        Piece movingPiece = board.pieceAt(from);
        if (movingPiece == null || movingPiece.type() != PieceType.King){
            return false;
        }
        if (isOccupiedBySameColor(board, to, movingPiece.color())){
            return false;
        }
        int fileDiff = Math.abs(to.file().ordinal() - from.file().ordinal());
        int rankDiff = Math.abs(to.rank().value() - from.rank().value());

        return fileDiff <= 1 && rankDiff <= 1;
    }
}
