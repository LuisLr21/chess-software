package software.ulpgc.chess.server.infrastructure.adapters.ai;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.core.Move;
import software.ulpgc.chess.server.application.ports.ChessEngine;
import software.ulpgc.chess.server.application.ports.ChessEngineUnavailableException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class HttpChessEngine implements ChessEngine {
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final String baseUrl;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public HttpChessEngine(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @Override
    public List<String> levels() {
        HttpRequest request = requestTo("/levels").GET().build();
        return List.of(read(send(request), String[].class));
    }

    @Override
    public Move chooseMove(Game game, String level) {
        BestMoveRequest body = new BestMoveRequest(level, game.moves().stream().map(Move::toString).toList());
        HttpRequest request = requestTo("/best-move")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(write(body)))
                .build();
        return Move.fromUci(read(send(request), BestMoveResponse.class).move());
    }

    private HttpRequest.Builder requestTo(String path) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path)).timeout(TIMEOUT);
    }

    private String send(HttpRequest request) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ChessEngineUnavailableException("AI service answered " + response.statusCode() + ": " + response.body());
            }
            return response.body();
        } catch (IOException e) {
            throw new ChessEngineUnavailableException("AI service not reachable at " + baseUrl, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ChessEngineUnavailableException("Interrupted while waiting for the AI service", e);
        }
    }

    private <T> T read(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            throw new ChessEngineUnavailableException("Unexpected answer from the AI service: " + json, e);
        }
    }

    private String write(Object body) {
        try {
            return mapper.writeValueAsString(body);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private record BestMoveRequest(String level, List<String> moves) {
    }

    private record BestMoveResponse(String move) {
    }
}
