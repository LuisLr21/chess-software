package software.ulpgc.chess.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InsufficientMaterialDetectorTest {
    private final InsufficientMaterialDetector detector = new InsufficientMaterialDetector();

    @Test
    void kingsAloneCannotMate() {
        assertTrue(detector.isInsufficient(Positions.board("Ke1", "ke8")));
    }

    @Test
    void aSingleMinorPieceCannotMate() {
        assertTrue(detector.isInsufficient(Positions.board("Ke1", "Bc1", "ke8")));
        assertTrue(detector.isInsufficient(Positions.board("Ke1", "ke8", "ng8")));
    }

    @Test
    void bishopsOnSquaresOfTheSameColorCannotMate() {
        assertTrue(detector.isInsufficient(Positions.board("Ke1", "Bc1", "ke8", "bf4")));
    }

    @Test
    void bishopsOnSquaresOfDifferentColorsCanMate() {
        assertFalse(detector.isInsufficient(Positions.board("Ke1", "Bc1", "ke8", "bc8")));
    }

    @Test
    void anyOtherMaterialCanMate() {
        assertFalse(detector.isInsufficient(Positions.board("Ke1", "Ra1", "ke8")));
        assertFalse(detector.isInsufficient(Positions.board("Ke1", "Pe2", "ke8")));
        assertFalse(detector.isInsufficient(Positions.board("Ke1", "Nb1", "Ng1", "ke8")));
        assertFalse(detector.isInsufficient(Positions.board("Ke1", "Nb1", "ke8", "bc8")));
    }
}
