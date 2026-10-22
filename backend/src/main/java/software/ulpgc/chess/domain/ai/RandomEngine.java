package software.ulpgc.chess.domain.ai;

import software.ulpgc.chess.domain.LegalMoveGenerator;
import software.ulpgc.chess.domain.Move;
import software.ulpgc.chess.domain.Position;

import java.util.List;
import java.util.Random;

public class RandomEngine implements Engine{
    private final LegalMoveGenerator generator = new LegalMoveGenerator();
    private final Random random = new Random();

    @Override
    public Move chooseMove(Position position) {
        List<Move> moves = generator.allLegalMoves(position);
        return moves.get(random.nextInt(moves.size()));
    }
}
