package software.ulpgc.chess.server.application.usecases;

import software.ulpgc.chess.core.ChessClock;
import software.ulpgc.chess.core.Color;
import software.ulpgc.chess.core.Game;
import software.ulpgc.chess.server.application.UseCase;
import software.ulpgc.chess.server.application.domain.Room;
import software.ulpgc.chess.server.application.domain.Seat;
import software.ulpgc.chess.server.application.ports.GameRepository;
import software.ulpgc.chess.server.application.ports.RoomRepository;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;

public class CreateRoomUseCase implements UseCase<NewRoom, PlayerSession> {
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_MINUTES = 60;

    private final GameRepository games;
    private final RoomRepository rooms;
    private final SecureRandom random = new SecureRandom();

    public CreateRoomUseCase(GameRepository games, RoomRepository rooms) {
        this.games = games;
        this.rooms = rooms;
    }

    @Override
    public PlayerSession execute(NewRoom newRoom) {
        int minutes = newRoom.minutes();
        if (minutes < 0 || minutes > MAX_MINUTES) throw new IllegalArgumentException("Minutes must be between 0 and " + MAX_MINUTES);
        Game game = Game.start(UUID.randomUUID().toString(), minutes == 0 ? null : ChessClock.of(Duration.ofMinutes(minutes)));
        games.save(game);
        String player = UUID.randomUUID().toString();
        Room room = Room.open(uniqueCode(), game.id(), Seat.of(player, newRoom.name(), "Blancas"));
        rooms.save(room);
        return new PlayerSession(new RoomState(room, game), Color.White, player);
    }

    private String uniqueCode() {
        String code;
        do {
            code = randomCode();
        } while (rooms.findByCode(code).isPresent());
        return code;
    }

    private String randomCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
        }
        return code.toString();
    }
}
