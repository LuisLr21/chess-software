package software.ulpgc.chess.server.infrastructure.adapters.websocket;

import io.javalin.config.RoutesConfig;
import io.javalin.websocket.WsContext;
import software.ulpgc.chess.core.Color;
import software.ulpgc.chess.server.application.domain.ChatMessage;
import software.ulpgc.chess.server.application.domain.Report;
import software.ulpgc.chess.server.application.domain.Room;
import software.ulpgc.chess.server.application.ports.RoomNotifier;
import software.ulpgc.chess.server.application.ports.RoomRepository;
import software.ulpgc.chess.server.application.usecases.RoomState;
import software.ulpgc.chess.server.infrastructure.adapters.http.ChatMessageResponse;
import software.ulpgc.chess.server.infrastructure.adapters.http.ReportResponse;
import software.ulpgc.chess.server.infrastructure.adapters.http.RoomResponse;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketRoomNotifier implements RoomNotifier {
    private final RoomRepository rooms;
    private final Map<String, Map<String, Connection>> connections = new ConcurrentHashMap<>();
    private AbandonmentWatcher abandonmentWatcher;

    public WebSocketRoomNotifier(RoomRepository rooms) {
        this.rooms = rooms;
    }

    private record Connection(WsContext ctx, String token, Color color) {
    }

    public record RoomEvent(String type, RoomResponse room) {
    }

    public record PresenceEvent(String type, boolean white, boolean black) {
    }

    public record ChatEvent(String type, ChatMessageResponse message) {
    }

    public record ReportEvent(String type, ReportResponse report) {
    }

    public void watchAbandonment(AbandonmentWatcher watcher) {
        this.abandonmentWatcher = watcher;
    }

    public void register(RoutesConfig routes) {
        routes.ws("/rooms/{code}/events", ws -> {
            ws.onConnect(ctx -> {
                String code = code(ctx);
                String token = ctx.queryParam("token");
                Connection connection = new Connection(ctx, token, colorOf(code, token));
                connectionsOf(code).put(ctx.sessionId(), connection);
                if (connection.color() != null && abandonmentWatcher != null) abandonmentWatcher.playerReturned(code, token);
                broadcastPresence(code);
            });
            ws.onClose(ctx -> {
                String code = code(ctx);
                Connection closed = connectionsOf(code).remove(ctx.sessionId());
                if (closed != null && closed.color() != null && !isConnected(code, closed.token()) && abandonmentWatcher != null) {
                    abandonmentWatcher.playerLeft(code, closed.token());
                }
                broadcastPresence(code);
            });
        });
    }

    @Override
    public void roomUpdated(RoomState state) {
        String code = state.room().code();
        refreshColors(code, state.room());
        for (Connection connection : connectionsOf(code).values()) {
            send(code, connection, new RoomEvent("room", RoomResponse.from(state, connection.token())));
        }
        broadcastPresence(code);
    }

    private void refreshColors(String code, Room room) {
        connectionsOf(code).replaceAll((id, c) -> new Connection(c.ctx(), c.token(), room.colorOf(c.token()).orElse(null)));
    }

    @Override
    public void messageSent(ChatMessage message) {
        broadcast(message.roomCode(), new ChatEvent("chat", ChatMessageResponse.from(message)));
    }

    @Override
    public void reportFiled(Report report) {
        broadcast(report.roomCode(), new ReportEvent("report", ReportResponse.from(report)));
    }

    private boolean isConnected(String code, String token) {
        return connectionsOf(code).values().stream().anyMatch(c -> token.equals(c.token()));
    }

    private void broadcastPresence(String code) {
        boolean white = connectionsOf(code).values().stream().anyMatch(c -> c.color() == Color.White);
        boolean black = connectionsOf(code).values().stream().anyMatch(c -> c.color() == Color.Black);
        broadcast(code, new PresenceEvent("presence", white, black));
    }

    private void broadcast(String code, Object event) {
        for (Connection connection : connectionsOf(code).values()) {
            send(code, connection, event);
        }
    }

    private void send(String code, Connection connection, Object event) {
        try {
            connection.ctx().send(event);
        } catch (RuntimeException e) {
            connectionsOf(code).remove(connection.ctx().sessionId());
        }
    }

    private Color colorOf(String code, String token) {
        Optional<Room> room = rooms.findByCode(code);
        return room.flatMap(r -> r.colorOf(token)).orElse(null);
    }

    private Map<String, Connection> connectionsOf(String code) {
        return connections.computeIfAbsent(code, c -> new ConcurrentHashMap<>());
    }

    private static String code(WsContext ctx) {
        return ctx.pathParam("code").toUpperCase();
    }
}
