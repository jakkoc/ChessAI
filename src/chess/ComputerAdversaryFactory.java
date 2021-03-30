package chess;

import java.awt.*;
import java.util.Locale;

public class ComputerAdversaryFactory {

    public static ComputerAdversary ofDifficulty(ChessBoard board, ChessBoard.ChessPiece.Color color, Difficulty difficulty) {
        switch(difficulty) {
            case VERY_EASY:
                return new RandomAdversary(color, board);
            case EASY:
                return new MiniMaxAdversary(color, board, 2, false);
            case MEDIUM:
                return new MiniMaxAdversary(color, board, 3, true);
            case HARD:
                return new MiniMaxAdversary(color, board, 4, true);
            case VERY_HARD:
                return new MiniMaxAdversary(color, board, 5, true);
        }

        return new MiniMaxAdversary(color, board, 3, true);
    }

    public enum Difficulty {
        VERY_EASY(new Color(38, 50, 83)),
        EASY(new Color(42, 157, 143)),
        MEDIUM(new Color(233, 196, 106)),
        HARD(new Color(244, 162, 97)),
        VERY_HARD(new Color(231, 111, 81));

        private final Color color;

        Difficulty(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }

        @Override
        public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase(Locale.ROOT).replace('_',' ');
        }
    }
}
