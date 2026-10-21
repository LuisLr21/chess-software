package software.ulpgc.chess.application;

import software.ulpgc.chess.domain.Color;
import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.Room;

import java.security.SecureRandom;
import java.util.UUID;

public class CreateRoomUseCase {
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;

    private final GameRepository games;
    private final RoomRepository rooms;
    private final SecureRandom random = new SecureRandom();

    public CreateRoomUseCase(GameRepository games, RoomRepository rooms) {
        this.games = games;
        this.rooms = rooms;
    }

    public PlayerSession execute() {
        Game game = Game.start(UUID.randomUUID().toString());
        games.save(game);
        String player = UUID.randomUUID().toString();
        Room room = Room.open(uniqueCode(), game.id(), player);
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
