package software.ulpgc.chess.core;

public class GameStatusEvaluator {
    private final LegalMoveGenerator generator = new LegalMoveGenerator();
    private final CheckDetector checkDetector = new CheckDetector();
    private final InsufficientMaterialDetector materialDetector = new InsufficientMaterialDetector();

    public GameStatus evaluate(Position position) {
        if (!generator.hasAnyLegalMove(position)) {
            return checkDetector.isInCheck(position, position.sideToMove()) ? GameStatus.Checkmate : GameStatus.Stalemate;
        }
        if (materialDetector.isInsufficient(position.board())) return GameStatus.InsufficientMaterial;
        if (position.halfMoveClock() >= 100) return GameStatus.FiftyMoveRule;
        return GameStatus.InProgress;
    }
}
