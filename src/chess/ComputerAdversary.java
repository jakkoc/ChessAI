package chess;

import java.util.Random;

public abstract class ComputerAdversary {
    protected static Random random = new Random();
    protected ChessBoard board;
    protected ChessBoard.ChessPiece.Color color;

    public ComputerAdversary(ChessBoard.ChessPiece.Color color, ChessBoard board) {
        this.color = color;
        this.board = board;
    }

    public abstract ChessBoard.Field.Move chooseMove();

    public abstract ChessBoard.ChessPiece.Piece choosePromotion();
}
