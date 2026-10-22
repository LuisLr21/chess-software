package software.ulpgc.chess.domain.ai;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.domain.Color;
import software.ulpgc.chess.domain.Games;
import software.ulpgc.chess.domain.LegalMoveGenerator;
import software.ulpgc.chess.domain.Position;
import software.ulpgc.chess.domain.Positions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnginesTest {
    private static final Position SCHOLARS_MATE_IN_ONE = Games.play("e2e4", "e7e5", "f1c4", "b8c6", "d1h5", "g8f6").position();
    private static final Position FOOLS_MATE_IN_ONE = Games.play("f2f3", "e7e5", "g2g4").position();
    private static final Position HANGING_QUEEN = Positions.of(Color.White, "Ka1", "Rd4", "ka8", "pd7", "qh4");
    private static final Position DEFENDED_PAWN = Positions.of(Color.White, "Ka1", "Qd1", "ke8", "pd7");

    private final LegalMoveGenerator generator = new LegalMoveGenerator();

    @RepeatedTest(20)
    void randomEngineAlwaysPlaysALegalMove() {
        Position position = Games.play("e2e4", "e7e5").position();
        assertTrue(generator.allLegalMoves(position).contains(new RandomEngine().chooseMove(position)));
    }

    @Test
    void greedyEngineCapturesTheMostValuablePiece() {
        assertEquals(Games.move("d4h4"), new GreedyEngine().chooseMove(HANGING_QUEEN));
    }

    @Test
    void greedyEngineTakesDefendedPiecesToo() {
        assertEquals(Games.move("d1d7"), new GreedyEngine().chooseMove(DEFENDED_PAWN));
    }

    @RepeatedTest(10)
    void greedyEnginePlaysALegalMoveWhenThereIsNothingToCapture() {
        Position position = Position.initial();
        assertTrue(generator.allLegalMoves(position).contains(new GreedyEngine().chooseMove(position)));
    }

    @Test
    void minimaxEngineFindsAMateInOneForBothColors() {
        assertEquals(Games.move("h5f7"), new MinimaxEngine(2).chooseMove(SCHOLARS_MATE_IN_ONE));
        assertEquals(Games.move("d8h4"), new MinimaxEngine(2).chooseMove(FOOLS_MATE_IN_ONE));
    }

    @Test
    void minimaxEngineCapturesAHangingPiece() {
        assertEquals(Games.move("d4h4"), new MinimaxEngine(2).chooseMove(HANGING_QUEEN));
    }

    @Test
    void minimaxEngineDoesNotGiveAwayItsQueen() {
        assertNotEquals(Games.move("d1d7"), new MinimaxEngine(2).chooseMove(DEFENDED_PAWN));
    }

    @Test
    void positionEvaluatorCountsMaterial() {
        PositionEvaluator evaluator = new PositionEvaluator();
        assertEquals(0, evaluator.evaluate(Position.initial(), Color.White));
        assertEquals(800, evaluator.evaluate(DEFENDED_PAWN, Color.White));
        assertEquals(-800, evaluator.evaluate(DEFENDED_PAWN, Color.Black));
    }
}
