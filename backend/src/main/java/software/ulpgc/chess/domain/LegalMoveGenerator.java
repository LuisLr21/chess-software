package software.ulpgc.chess.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LegalMoveGenerator {
    private final LegalMoveValidator validator = new LegalMoveValidator();

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
