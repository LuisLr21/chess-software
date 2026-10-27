package software.ulpgc.chess.core;

public enum Piece {
    WhitePawn(Color.White, PieceType.Pawn),
    WhiteKnight(Color.White, PieceType.Knight),
    WhiteBishop(Color.White, PieceType.Bishop),
    WhiteRook(Color.White, PieceType.Rook),
    WhiteQueen(Color.White, PieceType.Queen),
    WhiteKing(Color.White, PieceType.King),
    BlackPawn(Color.Black, PieceType.Pawn),
    BlackKnight(Color.Black, PieceType.Knight),
    BlackBishop(Color.Black, PieceType.Bishop),
    BlackRook(Color.Black, PieceType.Rook),
    BlackQueen(Color.Black, PieceType.Queen),
    BlackKing(Color.Black, PieceType.King);

    private final Color color;
    private final PieceType type;

    Piece(Color color, PieceType type) {
        this.color = color;
        this.type = type;
    }

    public Color color(){
        return color;
    }

    public PieceType type(){
        return type;
    }

    public char symbol(){
        char base = type.symbol();
        return color == Color.White ? Character.toUpperCase(base) : base;
    }

    public static Piece of(Color color, PieceType type){
        return switch (color){
            case White -> switch (type){
                case Pawn -> WhitePawn;
                case Knight -> WhiteKnight;
                case Bishop -> WhiteBishop;
                case Rook -> WhiteRook;
                case Queen -> WhiteQueen;
                case King -> WhiteKing;
            };
            case Black -> switch (type){
                case Pawn -> BlackPawn;
                case Knight -> BlackKnight;
                case Bishop -> BlackBishop;
                case Rook -> BlackRook;
                case Queen -> BlackQueen;
                case King -> BlackKing;
            };
        };
    }
}
