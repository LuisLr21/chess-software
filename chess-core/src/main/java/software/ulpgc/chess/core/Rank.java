package software.ulpgc.chess.core;

public enum Rank {
    R1(1), R2(2), R3(3), R4(4), R5(5), R6(6), R7(7), R8(8);

    private final int value;

    Rank(int value) {
        this.value = value;
    }

    public int value(){
        return value;
    }

    public static Rank fromValue(int value){
        return switch (value){
          case 1 -> R1;
          case 2 -> R2;
          case 3 -> R3;
          case 4 -> R4;
          case 5 -> R5;
          case 6 -> R6;
          case 7 -> R7;
          case 8 -> R8;
          default -> throw new IllegalArgumentException("Invalid rank: " + value);
        };
    }
}
