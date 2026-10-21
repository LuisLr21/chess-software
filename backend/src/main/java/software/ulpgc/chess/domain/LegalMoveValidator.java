package software.ulpgc.chess.domain;

public class LegalMoveValidator {
    private final MovementRule rules = new MovementRules();
    private final CastlingRule castlingRule = new CastlingRule();
    private final CheckDetector checkDetector = new CheckDetector();

    public boolean isLegal(Position position, Square from, Square to) {
        Piece piece = position.board().pieceAt(from);
        if (piece == null) return false;
        if (piece.color() != position.sideToMove()) return false;
        if (!rules.canMove(position, from, to) && !castlingRule.canCastle(position, from, to)) return false;
        Position after = position.move(from, to);
        return !checkDetector.isInCheck(after, piece.color());
    }
}
