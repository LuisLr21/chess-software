package software.ulpgc.chess.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static software.ulpgc.chess.domain.Positions.squares;

class LegalMoveGeneratorTest {
    private final LegalMoveGenerator generator = new LegalMoveGenerator();

    @Test
    void thereAreTwentyMovesInTheInitialPosition() {
        assertEquals(20, generator.allLegalMoves(Position.initial()).size());
    }

    @Test
    void listsTheDestinationsOfOnePiece() {
        assertEquals(squares("a3", "c3"), Set.copyOf(generator.legalMovesFrom(Position.initial(), Square.at("b1"))));
    }

    @Test
    void aPromotionIsOneMovePerPiece() {
        Position position = Positions.of(Color.White, "Ka1", "Pe7", "kh1");
        List<Move> promotions = generator.allLegalMoves(position).stream()
                .filter(move -> move.from().equals(Square.at("e7")))
                .toList();
        assertEquals(List.of(PieceType.Queen, PieceType.Rook, PieceType.Bishop, PieceType.Knight),
                promotions.stream().map(Move::promotion).toList());
    }

    @Test
    void onlyListsMovesOfTheSideToMove() {
        assertTrue(generator.allLegalMoves(Position.initial()).stream()
                .allMatch(move -> move.from().rank() == Rank.R1 || move.from().rank() == Rank.R2));
    }

    @Test
    void detectsWhenThereAreNoLegalMoves() {
        assertTrue(generator.hasAnyLegalMove(Position.initial()));
        assertFalse(generator.hasAnyLegalMove(Positions.of(Color.Black, "ka8", "Qb6", "Kc6")));
    }
}
