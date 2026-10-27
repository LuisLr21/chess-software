package software.ulpgc.chess.server.infrastructure.adapters.http;

import software.ulpgc.chess.core.Color;
import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.core.Move;
import software.ulpgc.chess.core.Piece;
import software.ulpgc.chess.core.PlayedMove;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record GameResponse(String id, String sideToMove, Map<String, String> pieces, String status, String winner,
                           boolean inCheck, List<String> moves, List<String> san, Map<String, List<String>> captured) {
    public static GameResponse from(Game game){
        Map<String, String> pieces = new HashMap<>();
        game.position().board().pieces()
                .forEach((square, piece) -> pieces.put(square.toString(), symbol(piece)));
        Color winner = game.winner();
        return new GameResponse(
                game.id(),
                game.position().sideToMove().name(),
                pieces,
                game.status().name(),
                winner == null ? null : winner.name(),
                game.isInCheck(),
                game.moves().stream().map(Move::toString).toList(),
                game.history().stream().map(PlayedMove::san).toList(),
                Map.of(
                        Color.White.name(), game.capturedBy(Color.White).stream().map(GameResponse::symbol).toList(),
                        Color.Black.name(), game.capturedBy(Color.Black).stream().map(GameResponse::symbol).toList()
                )
        );
    }

    private static String symbol(Piece piece) {
        return String.valueOf(piece.symbol());
    }
}
