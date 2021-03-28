package chess;

import java.util.List;

public class RandomAdversary extends ComputerAdversary {

    public RandomAdversary(ChessBoard.ChessPiece.Color color, ChessBoard board) {
        super(color, board);
    }

    public ChessBoard.Field.Move chooseMove() {
        List<ChessBoard.Field.Move> validMoves = board.getAllValidMoves(color);


        return validMoves.get(random.nextInt(validMoves.size()));
    }

    public ChessBoard.ChessPiece.Piece choosePromotion() {
        ChessBoard.ChessPiece.Piece[] possiblePromotions = {ChessBoard.ChessPiece.Piece.BISHOP, ChessBoard.ChessPiece.Piece.KNIGHT, ChessBoard.ChessPiece.Piece.ROOK, ChessBoard.ChessPiece.Piece.QUEEN};

        return possiblePromotions[random.nextInt(4)];
    }
}
