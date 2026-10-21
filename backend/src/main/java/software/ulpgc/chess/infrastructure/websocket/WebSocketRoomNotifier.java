package software.ulpgc.chess.infrastructure.websocket;

import io.javalin.config.RoutesConfig;
import io.javalin.websocket.WsContext;
import software.ulpgc.chess.application.RoomNotifier;
import software.ulpgc.chess.application.RoomRepository;
import software.ulpgc.chess.application.RoomState;
import software.ulpgc.chess.domain.ChatMessage;
import software.ulpgc.chess.domain.Color;
import software.ulpgc.chess.domain.Room;
import software.ulpgc.chess.infrastructure.http.ChatMessageResponse;
import software.ulpgc.chess.infrastructure.http.RoomResponse;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketRoomNotifier implements RoomNotifier {
    private final RoomRepository rooms;
    private final Map<String, Map<String, Connection>> connections = new ConcurrentHashMap<>();

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

    public void register(RoutesConfig routes) {
        routes.ws("/rooms/{code}/events", ws -> {
            ws.onConnect(ctx -> {
                String code = code(ctx);
                String token = ctx.queryParam("token");
                connectionsOf(code).put(ctx.sessionId(), new Connection(ctx, token, colorOf(code, token)));
                broadcastPresence(code);
            });
            ws.onClose(ctx -> {
                String code = code(ctx);
                connectionsOf(code).remove(ctx.sessionId());
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
