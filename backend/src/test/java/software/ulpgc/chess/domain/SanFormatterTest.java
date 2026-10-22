package software.ulpgc.chess.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SanFormatterTest {
    private final SanFormatter formatter = new SanFormatter();

    private String san(Position before, String uci) {
        Move move = Games.move(uci);
        PieceType promotion = move.promotion() == null ? PieceType.Queen : move.promotion();
        return formatter.format(before, move, before.move(move.from(), move.to(), promotion));
    }

    @Test
    void pawnAndPieceMoves() {
        assertEquals(List.of("e4", "Nf6", "Nc3"), Games.play("e2e4", "g8f6", "b1c3").history().stream()
                .map(PlayedMove::san).toList());
    }

    @Test
    void captures() {
        assertEquals("exd5", san(Games.play("e2e4", "d7d5").position(), "e4d5"));
        assertEquals("Nxe5", san(Games.play("g1f3", "e7e5").position(), "f3e5"));
    }

    @Test
    void castling() {
        Position position = Positions.of(Color.White, CastlingRights.initial(), "Ke1", "Ra1", "Rh1", "ke8");
        assertEquals("O-O", san(position, "e1g1"));
        assertEquals("O-O-O", san(position, "e1c1"));
    }

    @Test
    void promotion() {
        Position position = Positions.of(Color.White, "Ka1", "Pe7", "kh8");
        assertEquals("e8=Q+", san(position, "e7e8q"));
        assertEquals("e8=N", san(position, "e7e8n"));
    }

    @Test
    void disambiguatesByFileThenByRank() {
        assertEquals("Nbd2", san(Positions.of(Color.White, "Ke1", "Nb1", "Nf3", "ke8"), "b1d2"));
        assertEquals("R1a3", san(Positions.of(Color.White, "Kh2", "Ra1", "Ra5", "kh8"), "a1a3"));
    }

    @Test
    void checkmate() {
        assertEquals("Qxf7#", Games.play("e2e4", "e7e5", "f1c4", "b8c6", "d1h5", "g8f6", "h5f7")
                .history().getLast().san());
    }
}
