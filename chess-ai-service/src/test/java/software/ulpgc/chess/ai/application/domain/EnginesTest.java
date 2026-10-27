package software.ulpgc.chess.ai.application.domain;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import software.ulpgc.chess.core.Color;
import software.ulpgc.chess.core.GameStatus;
import software.ulpgc.chess.core.GameStatusEvaluator;
import software.ulpgc.chess.core.Games;
import software.ulpgc.chess.core.LegalMoveGenerator;
import software.ulpgc.chess.core.Move;
import software.ulpgc.chess.core.Position;
import software.ulpgc.chess.core.Positions;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnginesTest {
    private static final Position SCHOLARS_MATE_IN_ONE = Games.play("e2e4", "e7e5", "f1c4", "b8c6", "d1h5", "g8f6").position();
    private static final Position FOOLS_MATE_IN_ONE = Games.play("f2f3", "e7e5", "g2g4").position();
    private static final Position HANGING_QUEEN = Positions.of(Color.White, "Ka1", "Rd4", "ka8", "pd7", "qh4");
    private static final Position DEFENDED_PAWN = Positions.of(Color.White, "Ka1", "Qd1", "ke8", "pd7");
    private static final Position ROOK_LADDER = Positions.of(Color.White, "Ka1", "Ra6", "Rb5", "kh8");
    private static final Duration TIME_LIMIT = Duration.ofSeconds(5);

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

    @Test
    void expertEngineFindsAMateInOneForBothColors() {
        assertEquals(Games.move("h5f7"), expert(3).chooseMove(SCHOLARS_MATE_IN_ONE));
        assertEquals(Games.move("d8h4"), expert(3).chooseMove(FOOLS_MATE_IN_ONE));
    }

    @Test
    void expertEngineFindsAMateInTwo() {
        Position position = play(ROOK_LADDER, expert(4).chooseMove(ROOK_LADDER));
        for (Move reply : generator.allLegalMoves(position)) {
            Position afterReply = play(position, reply);
            Position afterMate = play(afterReply, expert(4).chooseMove(afterReply));
            assertEquals(GameStatus.Checkmate, new GameStatusEvaluator().evaluate(afterMate));
        }
    }

    @Test
    void expertEngineCapturesAHangingPiece() {
        assertEquals(Games.move("d4h4"), expert(3).chooseMove(HANGING_QUEEN));
    }

    @Test
    void expertEngineSeesTheRecaptureBeyondItsDepth() {
        assertNotEquals(Games.move("d1d7"), expert(1).chooseMove(DEFENDED_PAWN));
    }

    @Test
    void expertEngineReturnsNullWithoutLegalMoves() {
        assertNull(expert(3).chooseMove(Positions.of(Color.Black, "ka8", "Qb6", "Kc6")));
    }

    @Test
    void positionalEvaluatorIsZeroAtTheStartAndSymmetric() {
        PositionalEvaluator evaluator = new PositionalEvaluator();
        assertEquals(0, evaluator.evaluate(Position.initial(), Color.White));
        assertEquals(-evaluator.evaluate(HANGING_QUEEN, Color.White), evaluator.evaluate(HANGING_QUEEN, Color.Black));
    }

    @Test
    void positionalEvaluatorPrefersACentralKnight() {
        PositionalEvaluator evaluator = new PositionalEvaluator();
        Position central = Positions.of(Color.White, "Kg1", "Nd4", "Qd1", "kg8", "qd8", "pa7");
        Position rim = Positions.of(Color.White, "Kg1", "Na4", "Qd1", "kg8", "qd8", "pa7");
        assertTrue(evaluator.evaluate(central, Color.White) > evaluator.evaluate(rim, Color.White));
    }

    private ExpertEngine expert(int maxDepth) {
        return new ExpertEngine(TIME_LIMIT, maxDepth);
    }

    private Position play(Position position, Move move) {
        return position.move(move.from(), move.to(), move.promotion());
    }
}
