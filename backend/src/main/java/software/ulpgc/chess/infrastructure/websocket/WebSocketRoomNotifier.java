package software.ulpgc.chess.infrastructure.websocket;

import io.javalin.config.RoutesConfig;
import io.javalin.websocket.WsContext;
import software.ulpgc.chess.application.RoomNotifier;
import software.ulpgc.chess.application.RoomState;
import software.ulpgc.chess.infrastructure.http.RoomResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketRoomNotifier implements RoomNotifier {
    private final Map<String, Map<String, WsContext>> connections = new ConcurrentHashMap<>();

    public void register(RoutesConfig routes) {
        routes.ws("/rooms/{code}/events", ws -> {
            ws.onConnect(ctx -> connectionsOf(code(ctx)).put(ctx.sessionId(), ctx));
            ws.onClose(ctx -> connectionsOf(code(ctx)).remove(ctx.sessionId()));
        });
    }

    @Override
    public void roomUpdated(RoomState state) {
        RoomResponse response = RoomResponse.from(state);
        for (WsContext ctx : connectionsOf(state.room().code()).values()) {
            try {
                ctx.send(response);
            } catch (RuntimeException e) {
                connectionsOf(state.room().code()).remove(ctx.sessionId());
            }
        }
    }

    private Map<String, WsContext> connectionsOf(String code) {
        return connections.computeIfAbsent(code, c -> new ConcurrentHashMap<>());
    }

    private static String code(WsContext ctx) {
        return ctx.pathParam("code").toUpperCase();
    }
}
