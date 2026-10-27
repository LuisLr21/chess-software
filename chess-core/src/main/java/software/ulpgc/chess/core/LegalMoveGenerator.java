package software.ulpgc.chess.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LegalMoveGenerator {
    private static final List<PieceType> PROMOTIONS = List.of(PieceType.Queen, PieceType.Rook, PieceType.Bishop, PieceType.Knight);

    private final LegalMoveValidator validator = new LegalMoveValidator();

    public List<Move> allLegalMoves(Position position) {
        List<Move> moves = new ArrayList<>();
        for (Map.Entry<Square, Piece> entry : position.board().pieces().entrySet()) {
            if (entry.getValue().color() != position.sideToMove()) continue;
            Square from = entry.getKey();
            for (Square to : legalMovesFrom(position, from)) addMoves(position, from, to, moves);
        }
        return moves;
    }

    public List<Move> allLegalCaptures(Position position) {
        List<Move> captures = new ArrayList<>();
        for (Map.Entry<Square, Piece> entry : position.board().pieces().entrySet()) {
            if (entry.getValue().color() != position.sideToMove()) continue;
            Square from = entry.getKey();
            for (Square to : targetsOf(position, entry.getValue())) {
                if (validator.isLegal(position, from, to)) addMoves(position, from, to, captures);
            }
        }
        return captures;
    }

    private List<Square> targetsOf(Position position, Piece piece) {
        List<Square> targets = new ArrayList<>();
        for (Map.Entry<Square, Piece> entry : position.board().pieces().entrySet()) {
            if (entry.getValue().color() != piece.color()) targets.add(entry.getKey());
        }
        if (piece.type() == PieceType.Pawn && position.enPassantTarget() != null) targets.add(position.enPassantTarget());
        return targets;
    }

    private void addMoves(Position position, Square from, Square to, List<Move> moves) {
        if (position.isPromotion(from, to)) {
            PROMOTIONS.forEach(promotion -> moves.add(new Move(from, to, promotion)));
        } else {
            moves.add(new Move(from, to, null));
        }
    }

    public List<Square> legalMovesFrom(Position position, Square from) {
        List<Square> destinations = new ArrayList<>();
        for (File file : File.values()) {
            for (Rank rank : Rank.values()) {
                Square to = new Square(file, rank);
                if (validator.isLegal(position, from, to)) destinations.add(to);
            }
        }
        return destinations;
    }

    public boolean hasAnyLegalMove(Position position) {
        for (Map.Entry<Square, Piece> entry : position.board().pieces().entrySet()) {
            if (entry.getValue().color() == position.sideToMove()
                    && !legalMovesFrom(position, entry.getKey()).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
