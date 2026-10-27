package software.ulpgc.chess.ai.application.domain;

import software.ulpgc.chess.core.LegalMoveGenerator;
import software.ulpgc.chess.core.Move;
import software.ulpgc.chess.core.Position;

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
