package software.ulpgc.chess.core;

import java.util.Map;

public class SanFormatter {
    private final LegalMoveValidator validator = new LegalMoveValidator();
    private final CheckDetector checkDetector = new CheckDetector();
    private final LegalMoveGenerator generator = new LegalMoveGenerator();

    public String format(Position before, Move move, Position after) {
        return body(before, move) + suffix(after);
    }

    private String body(Position before, Move move) {
        Square from = move.from();
        Square to = move.to();
        if (before.isCastling(from, to)) return to.file() == File.G ? "O-O" : "O-O-O";
        Piece piece = before.board().pieceAt(from);
        boolean capture = before.capturedPiece(from, to) != null;
        String promotion = move.promotion() == null ? "" : "=" + Character.toUpperCase(move.promotion().symbol());
        if (piece.type() == PieceType.Pawn) {
            return (capture ? from.file().symbol() + "x" : "") + to + promotion;
        }
        return Character.toUpperCase(piece.type().symbol()) + disambiguation(before, piece, from, to) + (capture ? "x" : "") + to;
    }

    private String disambiguation(Position before, Piece piece, Square from, Square to) {
        boolean ambiguous = false;
        boolean sameFile = false;
        boolean sameRank = false;
        for (Map.Entry<Square, Piece> entry : before.board().pieces().entrySet()) {
            Square other = entry.getKey();
            if (other.equals(from) || entry.getValue() != piece || !validator.isLegal(before, other, to)) continue;
            ambiguous = true;
            if (other.file() == from.file()) sameFile = true;
            if (other.rank() == from.rank()) sameRank = true;
        }
        if (!ambiguous) return "";
        if (!sameFile) return String.valueOf(from.file().symbol());
        if (!sameRank) return String.valueOf(from.rank().value());
        return from.toString();
    }

    private String suffix(Position after) {
        if (!checkDetector.isInCheck(after, after.sideToMove())) return "";
        return generator.hasAnyLegalMove(after) ? "+" : "#";
    }
}
