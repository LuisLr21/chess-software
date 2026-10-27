package software.ulpgc.chess.core;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static software.ulpgc.chess.core.Positions.squares;

class GameTest {
    private static final Instant START = Instant.parse("2026-10-22T10:00:00Z");

    @Test
    void startsInTheInitialPosition() {
        Game game = Game.start("id");
        assertEquals(Position.initial(), game.position());
        assertEquals(GameStatus.InProgress, game.status());
        assertTrue(game.history().isEmpty());
        assertNull(game.winner());
    }

    @Test
    void playingRecordsTheMoveAndPassesTheTurn() {
        Game game = Games.play("e2e4");
        assertEquals(Color.Black, game.position().sideToMove());
        assertEquals(List.of(Games.move("e2e4")), game.moves());
        assertEquals("e4", game.history().getFirst().san());
    }

    @Test
    void rejectsIllegalMoves() {
        Game game = Game.start("id");
        assertThrows(IllegalMoveException.class, () -> game.play(Square.at("e2"), Square.at("e5")));
        assertThrows(IllegalMoveException.class, () -> game.play(Square.at("e7"), Square.at("e5")));
    }

    @Test
    void rejectsPromotingToAKingOrAPawn() {
        Game game = Game.start("id");
        assertThrows(IllegalArgumentException.class, () -> game.play(Square.at("e2"), Square.at("e4"), PieceType.King));
        assertThrows(IllegalArgumentException.class, () -> game.play(Square.at("e2"), Square.at("e4"), PieceType.Pawn));
    }

    @Test
    void promotesToTheChosenPiece() {
        Game game = new Game("id", Positions.of(Color.White, "Ka1", "Pe7", "kh1"), List.of(), null, null);
        Game promoted = game.play(Square.at("e7"), Square.at("e8"), PieceType.Rook);
        assertEquals(Piece.WhiteRook, promoted.position().board().pieceAt("e8"));
        assertEquals(PieceType.Rook, promoted.moves().getFirst().promotion());
    }

    @Test
    void enPassantIsRecordedAsACapture() {
        Game game = Games.play("e2e4", "a7a6", "e4e5", "d7d5", "e5d6");
        assertEquals(List.of(Piece.BlackPawn), game.capturedBy(Color.White));
        assertEquals("exd6", game.history().getLast().san());
    }

    @Test
    void checkmateEndsTheGame() {
        Game game = Games.play("f2f3", "e7e5", "g2g4", "d8h4");
        assertEquals(GameStatus.Checkmate, game.status());
        assertEquals(Color.Black, game.winner());
        assertTrue(game.isInCheck());
        assertTrue(game.isOver());
        assertTrue(game.legalMovesFrom(Square.at("e1")).isEmpty());
        assertThrows(GameOverException.class, () -> game.play(Square.at("a2"), Square.at("a3")));
    }

    @Test
    void listsTheLegalMovesOfAPiece() {
        assertEquals(squares("e3", "e4"), Set.copyOf(Game.start("id").legalMovesFrom(Square.at("e2"))));
    }

    @Test
    void resigningGivesTheWinToTheOpponent() {
        Game game = Game.start("id").resign(Color.White, null);
        assertEquals(GameStatus.Resignation, game.status());
        assertEquals(Color.Black, game.winner());
        assertThrows(GameOverException.class, () -> game.play(Square.at("e2"), Square.at("e4")));
        assertThrows(GameOverException.class, () -> game.resign(Color.Black, null));
    }

    @Test
    void abandoningGivesTheWinToTheOpponent() {
        Game game = Games.play("e2e4").abandon(Color.Black, null);
        assertEquals(GameStatus.Abandonment, game.status());
        assertEquals(Color.White, game.winner());
        assertThrows(GameOverException.class, () -> game.abandon(Color.White, null));
    }

    @Test
    void movesAreReadFromUciText() {
        assertEquals(new Move(Square.at("e2"), Square.at("e4"), null), Move.fromUci("e2e4"));
        assertEquals(new Move(Square.at("a7"), Square.at("a8"), PieceType.Knight), Move.fromUci("a7a8n"));
        assertEquals("a7a8n", Move.fromUci("a7a8n").toString());
        assertThrows(IllegalArgumentException.class, () -> Move.fromUci("e2"));
        assertThrows(IllegalArgumentException.class, () -> Move.fromUci(null));
    }

    @Test
    void aDrawByAgreementHasNoWinner() {
        Game game = Games.play("e2e4").agreeDraw(null);
        assertEquals(GameStatus.DrawByAgreement, game.status());
        assertNull(game.winner());
    }

    @Test
    void keepsTrackOfCapturedPieces() {
        Game game = Games.play("e2e4", "d7d5", "e4d5", "d8d5");
        assertEquals(List.of(Piece.BlackPawn), game.capturedBy(Color.White));
        assertEquals(List.of(Piece.WhitePawn), game.capturedBy(Color.Black));
    }

    @Test
    void restoringReplaysTheMoves() {
        Game game = Games.play("e2e4", "e7e5", "g1f3", "b8c6", "f1b5");
        assertEquals(game, Game.restore("test", game.moves(), null, null));
    }

    @Test
    void theClockRunsForTheSideToMove() {
        Game game = Game.start("id", ChessClock.of(Duration.ofMinutes(1))).startClock(START);
        Game after = game.play(Square.at("e2"), Square.at("e4"), PieceType.Queen, START.plusSeconds(10));
        assertEquals(50_000, after.clock().whiteMillis());
        assertEquals(60_000, after.clock().blackMillis());
        assertEquals(START.plusSeconds(10), after.clock().turnStartedAt());
    }

    @Test
    void runningOutOfTimeLosesTheGame() {
        Game game = Game.start("id", ChessClock.of(Duration.ofMinutes(1))).startClock(START);
        assertSame(game, game.flagTimeout(START.plusSeconds(30)));

        Game flagged = game.flagTimeout(START.plusSeconds(61));
        assertEquals(GameStatus.Timeout, flagged.status());
        assertEquals(Color.Black, flagged.winner());
        assertFalse(flagged.clock().isRunning());
        assertThrows(GameOverException.class,
                () -> game.play(Square.at("e2"), Square.at("e4"), PieceType.Queen, START.plusSeconds(61)));
    }

    @Test
    void theClockStopsWhenTheGameEnds() {
        Game game = Game.start("id", ChessClock.of(Duration.ofMinutes(1))).startClock(START);
        Instant now = START;
        for (String move : List.of("f2f3", "e7e5", "g2g4", "d8h4")) {
            now = now.plusSeconds(1);
            Move parsed = Games.move(move);
            game = game.play(parsed.from(), parsed.to(), PieceType.Queen, now);
        }
        assertEquals(GameStatus.Checkmate, game.status());
        assertFalse(game.clock().isRunning());
    }
}
