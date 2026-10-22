package software.ulpgc.chess.domain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Positions {
    public static final CastlingRights NO_CASTLING = new CastlingRights(false, false, false, false);

    public static Position of(Color sideToMove, String... pieces) {
        return of(sideToMove, NO_CASTLING, pieces);
    }

    public static Position of(Color sideToMove, CastlingRights castlingRights, String... pieces) {
        return new Position(board(pieces), sideToMove, castlingRights, null, 0);
    }

    public static Board board(String... pieces) {
        Map<Square, Piece> board = new HashMap<>();
        for (String piece : pieces) {
            char symbol = piece.charAt(0);
            Color color = Character.isUpperCase(symbol) ? Color.White : Color.Black;
            board.put(Square.at(piece.substring(1)), Piece.of(color, PieceType.fromSymbol(symbol)));
        }
        return new Board(board);
    }

    public static Set<Square> squares(String... squares) {
        return Arrays.stream(squares).map(Square::at).collect(Collectors.toSet());
    }

    public static Set<Square> reachable(MovementRule rule, Position position, String from) {
        Set<Square> destinations = new HashSet<>();
        for (File file : File.values()) {
            for (Rank rank : Rank.values()) {
                Square to = new Square(file, rank);
                if (rule.canMove(position, Square.at(from), to)) destinations.add(to);
            }
        }
        return destinations;
    }
}
