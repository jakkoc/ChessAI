package chess;

import java.awt.*;

public class Main {
    
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            ChessFrame frame = new ChessFrame(ChessBoard.startingPosition());
            frame.setVisible(true);
            frame.chooseDifficulty();
        });
    }
}
