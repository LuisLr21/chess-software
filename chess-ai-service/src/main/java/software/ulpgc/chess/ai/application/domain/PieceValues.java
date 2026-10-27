package software.ulpgc.chess.ai.application.domain;

import software.ulpgc.chess.core.PieceType;

public class PieceValues {
    public static int of(PieceType type) {
        return switch (type) {
            case Pawn -> 100;
            case Knight -> 300;
            case Bishop -> 300;
            case Rook -> 500;
            case Queen -> 900;
            case King -> 0;
        };
    }
}
