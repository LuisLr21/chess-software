package software.ulpgc.chess.infrastructure.http;

import software.ulpgc.chess.domain.Game;

import java.util.HashMap;
import java.util.Map;

public record GameResponse(String id, String sideToMove, Map<String, String> pieces) {
    public static GameResponse from(Game game){
        Map<String, String> pieces = new HashMap<>();
        game.position().board().pieces()
                .forEach((square, piece) -> pieces.put(square.toString(), String.valueOf(piece.symbol())));
        return new GameResponse(game.id(), game.position().sideToMove().name(), pieces);
    }
}
