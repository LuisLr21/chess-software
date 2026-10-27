package software.ulpgc.chess.ai.application.usecases;

import software.ulpgc.chess.core.Move;

import java.util.List;

public record BestMoveQuery(String level, List<Move> moves) {
}
