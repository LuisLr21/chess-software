package software.ulpgc.chess.infrastructure.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software.ulpgc.chess.domain.ChatMessage;
import software.ulpgc.chess.domain.ChessClock;
import software.ulpgc.chess.domain.Color;
import software.ulpgc.chess.domain.Game;
import software.ulpgc.chess.domain.Games;
import software.ulpgc.chess.domain.Piece;
import software.ulpgc.chess.domain.PieceType;
import software.ulpgc.chess.domain.Room;
import software.ulpgc.chess.domain.Seat;
import software.ulpgc.chess.domain.Square;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqliteRepositoriesTest {
    private static final Instant START = Instant.parse("2026-10-22T10:00:00Z");

    @TempDir
    Path directory;

    private SqliteGameRepository games;
    private SqliteRoomRepository rooms;
    private SqliteChatRepository chat;

    @BeforeEach
    void setUp() {
        SqliteDatabase database = new SqliteDatabase(directory.resolve("chess.db").toString());
        games = new SqliteGameRepository(database);
        rooms = new SqliteRoomRepository(database);
        chat = new SqliteChatRepository(database);
    }

    @Test
    void aMissingGameOrRoomIsEmpty() {
        assertEquals(Optional.empty(), games.findById("missing"));
        assertEquals(Optional.empty(), rooms.findByCode("NOPE00"));
        assertEquals(List.of(), chat.findByRoom("NOPE00"));
    }

    @Test
    void aGameIsRestoredFromItsMoves() {
        Game game = Games.play("e2e4", "e7e5", "g1f3", "b8c6");
        games.save(game);
        assertEquals(game, games.findById("test").orElseThrow());
    }

    @Test
    void savingAgainAddsTheNewMoves() {
        Game game = Games.play("e2e4");
        games.save(game);
        Game later = Games.play(game, "e7e5", "g1f3");
        games.save(later);
        assertEquals(later, games.findById("test").orElseThrow());
    }

    @Test
    void promotionsAreStored() {
        Game game = Games.play("a2a4", "b7b5", "a4b5", "a7a6", "b5a6", "g8f6", "a6a7", "f6g8", "a7b8n");
        games.save(game);
        Game restored = games.findById("test").orElseThrow();
        assertEquals(Piece.WhiteKnight, restored.position().board().pieceAt("b8"));
        assertEquals(game, restored);
    }

    @Test
    void theEndingAndTheClockAreStored() {
        Game game = Game.start("timed", ChessClock.of(Duration.ofMinutes(5)))
                .startClock(START)
                .play(Square.at("e2"), Square.at("e4"), PieceType.Queen, START.plusSeconds(5))
                .resign(Color.Black, START.plusSeconds(12));
        games.save(game);
        assertEquals(game, games.findById("timed").orElseThrow());
    }

    @Test
    void aRunningClockIsStored() {
        Game game = Game.start("running", ChessClock.of(Duration.ofMinutes(3))).startClock(START);
        games.save(game);
        assertEquals(game, games.findById("running").orElseThrow());
    }

    @Test
    void roomsAreStoredAndUpdated() {
        Room open = Room.open("ABC123", "game-1", new Seat("token-luis", "Luis"));
        rooms.save(open);
        assertEquals(open, rooms.findByCode("ABC123").orElseThrow());

        Room full = open.join(new Seat("token-ana", "Ana")).withDrawOffer(Color.Black).withRematchOffer(Color.White);
        rooms.save(full);
        assertEquals(full, rooms.findByCode("ABC123").orElseThrow());

        Room rematch = full.rematch("game-2");
        rooms.save(rematch);
        assertEquals(rematch, rooms.findByCode("ABC123").orElseThrow());
    }

    @Test
    void messagesAreStoredInOrderForEachRoom() {
        ChatMessage hello = ChatMessage.create("ABC123", Color.White, "Luis", "hola", START);
        ChatMessage luck = ChatMessage.create("ABC123", Color.Black, "Ana", "suerte", START.plusSeconds(3));
        ChatMessage other = ChatMessage.create("XYZ789", Color.White, "Pepe", "otra sala", START);
        chat.save(hello);
        chat.save(other);
        chat.save(luck);
        assertEquals(List.of(hello, luck), chat.findByRoom("ABC123"));
    }

    @Test
    void reopeningTheDatabaseKeepsTheData() {
        Game game = Games.play("d2d4", "d7d5");
        games.save(game);
        SqliteGameRepository reopened = new SqliteGameRepository(new SqliteDatabase(directory.resolve("chess.db").toString()));
        assertEquals(game, reopened.findById("test").orElseThrow());
    }
}
