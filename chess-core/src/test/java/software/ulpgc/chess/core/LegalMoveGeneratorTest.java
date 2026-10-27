package software.ulpgc.chess.core;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static software.ulpgc.chess.core.Positions.squares;

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
    void listsOnlyTheCaptures() {
        Position position = Positions.of(Color.White, "Ka1", "Rd4", "Pb2", "kh8", "pd7", "ph4", "pc3");
        assertEquals(Set.of(Games.move("d4d7"), Games.move("d4h4"), Games.move("b2c3")),
                Set.copyOf(generator.allLegalCaptures(position)));
    }

    @Test
    void theCapturesIncludeEnPassant() {
        Position position = Games.play("e2e4", "a7a6", "e4e5", "d7d5").position();
        assertTrue(generator.allLegalCaptures(position).contains(Games.move("e5d6")));
    }

    @Test
    void detectsWhenThereAreNoLegalMoves() {
        assertTrue(generator.hasAnyLegalMove(Position.initial()));
        assertFalse(generator.hasAnyLegalMove(Positions.of(Color.Black, "ka8", "Qb6", "Kc6")));
    }
}
