package chess;

import java.awt.*;

public class Main {

    //TODO Handle invalid king moves and game ending
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            ChessFrame frame = new ChessFrame(ChessBoard.startingPosition());
            frame.setVisible(true);
        });
    }
}
