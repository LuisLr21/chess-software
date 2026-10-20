package software.ulpgc.chess;

public class MovementRules implements MovementRule{

    @Override
    public boolean canMove(Position position, Square from, Square to){
        Piece piece = position.board().pieceAt(from);
        if (piece == null) return false;
        return ruleFor(piece.type()).canMove(position, from, to);
    }

    private MovementRule ruleFor(PieceType type) {
        return switch (type) {
            case Pawn -> new PawnMovementRule();
            case Knight -> new KnightMovementRule();
            case Bishop -> new BishopMovementRule();
            case Rook -> new RookMovementRule();
            case Queen -> new QueenMovementRule();
            case King -> new KingMovementRule();
        };
    }
}
