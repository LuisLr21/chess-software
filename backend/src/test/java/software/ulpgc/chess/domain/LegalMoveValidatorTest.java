package software.ulpgc.chess.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegalMoveValidatorTest {
    private final LegalMoveValidator validator = new LegalMoveValidator();

    @Test
    void onlyTheSideToMoveCanMove() {
        Position position = Position.initial();
        assertTrue(validator.isLegal(position, Square.at("e2"), Square.at("e4")));
        assertFalse(validator.isLegal(position, Square.at("e7"), Square.at("e5")));
    }

    @Test
    void anEmptySquareCannotMove() {
        assertFalse(validator.isLegal(Position.initial(), Square.at("e4"), Square.at("e5")));
    }

    @Test
    void aPinnedPieceCannotLeaveTheKingInCheck() {
        Position position = Positions.of(Color.White, "Ke1", "Ne2", "re8", "kh8");
        assertFalse(validator.isLegal(position, Square.at("e2"), Square.at("c3")));
    }

    @Test
    void theKingCannotMoveIntoCheck() {
        Position position = Positions.of(Color.White, "Ke1", "ra2", "kh8");
        assertFalse(validator.isLegal(position, Square.at("e1"), Square.at("e2")));
        assertTrue(validator.isLegal(position, Square.at("e1"), Square.at("f1")));
    }

    @Test
    void theKingCannotCaptureADefendedPiece() {
        Position position = Positions.of(Color.White, "Ke1", "qe2", "ke3");
        assertFalse(validator.isLegal(position, Square.at("e1"), Square.at("e2")));
    }

    @Test
    void aCheckMustBeAnswered() {
        Position position = Positions.of(Color.White, "Ke1", "Pa2", "Rd2", "re8", "kh8");
        assertFalse(validator.isLegal(position, Square.at("a2"), Square.at("a3")));
        assertTrue(validator.isLegal(position, Square.at("d2"), Square.at("e2")));
        assertTrue(validator.isLegal(position, Square.at("e1"), Square.at("f1")));
    }
}
