package software.ulpgc.chess.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SquareTest {
    @Test
    void parsesAlgebraicNotation() {
        assertEquals(new Square(File.E, Rank.R4), Square.at("e4"));
        assertEquals(new Square(File.A, Rank.R1), Square.at("a1"));
        assertEquals(new Square(File.H, Rank.R8), Square.at("h8"));
    }

    @Test
    void printsAlgebraicNotation() {
        assertEquals("e4", new Square(File.E, Rank.R4).toString());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"e", "e0", "e9", "i1", "E4", "e44"})
    void rejectsInvalidSquares(String square) {
        assertThrows(IllegalArgumentException.class, () -> Square.at(square));
    }
}
