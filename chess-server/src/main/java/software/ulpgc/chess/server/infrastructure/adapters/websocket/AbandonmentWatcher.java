package software.ulpgc.chess.server.infrastructure.adapters.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.usecases.PlayerInRoom;
import software.ulpgc.chess.server.application.usecases.RoomState;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AbandonmentWatcher {
    private static final Logger log = LoggerFactory.getLogger(AbandonmentWatcher.class);

    private final UseCase<PlayerInRoom, RoomState> abandonGame;
    private final Duration gracePeriod;
    private final Map<String, ScheduledFuture<?>> pending = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "abandonment-watcher");
        thread.setDaemon(true);
        return thread;
    });

    public AbandonmentWatcher(UseCase<PlayerInRoom, RoomState> abandonGame, Duration gracePeriod) {
        this.abandonGame = abandonGame;
        this.gracePeriod = gracePeriod;
    }

    public void playerLeft(String code, String player) {
        ScheduledFuture<?> abandon = scheduler.schedule(() -> abandon(code, player), gracePeriod.toMillis(), TimeUnit.MILLISECONDS);
        ScheduledFuture<?> previous = pending.put(key(code, player), abandon);
        if (previous != null) previous.cancel(false);
    }

    public void playerReturned(String code, String player) {
        ScheduledFuture<?> abandon = pending.remove(key(code, player));
        if (abandon != null) abandon.cancel(false);
    }

    private void abandon(String code, String player) {
        pending.remove(key(code, player));
        try {
            abandonGame.execute(new PlayerInRoom(code, player));
        } catch (RuntimeException e) {
            log.warn("Could not mark room {} as abandoned: {}", code, e.getMessage());
        }
    }

    private static String key(String code, String player) {
        return code + "/" + player;
    }
}
