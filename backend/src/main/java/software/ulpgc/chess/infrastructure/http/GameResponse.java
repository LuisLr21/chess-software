package software.ulpgc.chess.infrastructure.http;

import software.ulpgc.chess.domain.Color;
import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.Move;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record GameResponse(String id, String sideToMove, Map<String, String> pieces, String status, String winner,
                           List<String> moves) {
    public static GameResponse from(Game game){
        Map<String, String> pieces = new HashMap<>();
        game.position().board().pieces()
                .forEach((square, piece) -> pieces.put(square.toString(), String.valueOf(piece.symbol())));
        Color winner = game.winner();
        return new GameResponse(
                game.id(),
                game.position().sideToMove().name(),
                pieces,
                game.status().name(),
                winner == null ? null : winner.name(),
                game.history().stream().map(Move::toString).toList()
        );
    }
}
