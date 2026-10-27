package software.ulpgc.chess.server.infrastructure.adapters.persistence;

import software.ulpgc.chess.core.Color;
import software.ulpgc.chess.server.application.domain.ChatMessage;
import software.ulpgc.chess.server.application.ports.ChatRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SqliteChatRepository implements ChatRepository {
    private final SqliteDatabase database;

    public SqliteChatRepository(SqliteDatabase database) {
        this.database = database;
    }

    @Override
    public void save(ChatMessage message) {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO messages (room_code, author, author_name, text, sent_at) VALUES (?, ?, ?, ?, ?)")) {
            statement.setString(1, message.roomCode());
            statement.setString(2, message.author().name());
            statement.setString(3, message.authorName());
            statement.setString(4, message.text());
            statement.setString(5, message.sentAt().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Could not save message of room " + message.roomCode(), e);
        }
    }

    @Override
    public List<ChatMessage> findByRoom(String roomCode) {
        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT author, author_name, text, sent_at FROM messages WHERE room_code = ? ORDER BY id")) {
            statement.setString(1, roomCode);
            try (ResultSet result = statement.executeQuery()) {
                List<ChatMessage> messages = new ArrayList<>();
                while (result.next()) {
                    messages.add(new ChatMessage(
                            roomCode,
                            Color.valueOf(result.getString("author")),
                            result.getString("author_name"),
                            result.getString("text"),
                            Instant.parse(result.getString("sent_at"))
                    ));
                }
                return messages;
            }
        } catch (SQLException e) {
            throw new PersistenceException("Could not load messages of room " + roomCode, e);
        }
    }
}
