package software.ulpgc.chess.core;

public class CastlingRule {
    private final AttackDetector attackDetector = new AttackDetector();

    public boolean canCastle(Position position, Square from, Square to) {
        Piece king = position.board().pieceAt(from);
        if (king == null || king.type() != PieceType.King) return false;

        Rank homeRank = king.color() == Color.White ? Rank.R1 : Rank.R8;
        if (!from.equals(new Square(File.E, homeRank)) || to.rank() != homeRank) return false;

        boolean kingSide = to.file() == File.G;
        if (!kingSide && to.file() != File.C) return false;
        if (!hasRight(position.castlingRights(), king.color(), kingSide)) return false;

        File[] between = kingSide ? new File[]{File.F, File.G} : new File[]{File.B, File.C, File.D};
        if (!areEmpty(position.board(), homeRank, between)) return false;

        Color opponent = king.color().opposite();
        Square crossedSquare = new Square(kingSide ? File.F : File.D, homeRank);
        return !attackDetector.isAttacked(position, from, opponent)
                && !attackDetector.isAttacked(position, crossedSquare, opponent);
    }

    private boolean areEmpty(Board board, Rank rank, File[] files) {
        for (File file : files) {
            if (board.pieceAt(new Square(file, rank)) != null) return false;
        }
        return true;
    }

    private boolean hasRight(CastlingRights rights, Color color, boolean kingSide) {
        if (color == Color.White) return kingSide ? rights.whiteKingSide() : rights.whiteQueenSide();
        return kingSide ? rights.blackKingSide() : rights.blackQueenSide();
    }
}
