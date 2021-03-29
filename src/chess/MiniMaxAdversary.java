package chess;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static chess.ChessBoard.ChessPiece.*;
import static chess.ChessBoard.ChessPiece.Color.*;

public class MiniMaxAdversary extends ComputerAdversary {
    private ChessBoard.Field.Move moveToMake;
    private final Thread[] threads;
    private int bestEvaluation;
    private static final int NUMBER_OF_THREADS = 4;
    private final int depth;

    public MiniMaxAdversary(ChessBoard.ChessPiece.Color color, ChessBoard board, int depth) {
        super(color, board);
        this.depth = depth;
        threads = new Thread[NUMBER_OF_THREADS];
    }

    @Override
    public ChessBoard.Field.Move chooseMove() {
        bestEvaluation = color == ChessBoard.ChessPiece.Color.WHITE ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            ChessBoard copiedChessBoard = new ChessBoard(board);
            List<ChessBoard.Field.Move> startingMoves = getPart(copiedChessBoard.getAllValidMoves(board.getColorToMove()), i);

            threads[i] = new Thread(() -> {
                minimax(copiedChessBoard, startingMoves, depth, board.getColorToMove() == WHITE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for(Thread thread : threads) {
            try {
                thread.join();
            } catch(InterruptedException e) {
                Logger.getLogger("global").log(Level.SEVERE,"Thread interrupted!", e);
            }
        }

        return moveToMake;
    }

    private List<ChessBoard.Field.Move> getPart(List<ChessBoard.Field.Move> moves, int number) {
        List<ChessBoard.Field.Move> parts= new ArrayList<>();

        for(int i = 0; i < moves.size(); i++) {
            if(i % NUMBER_OF_THREADS == number) parts.add(moves.get(i));
        }

        return parts;
    }

    @Override
    public Piece choosePromotion() {
        return Piece.QUEEN;
    }

    private int minimax(ChessBoard board, List<ChessBoard.Field.Move> startingMoves, int depth, boolean maximizingPlayer, int alpha, int beta) {
        if(depth == 0) {
            return evaluate(board);
        }

        if(maximizingPlayer) {
            int maximumEvaluation = Integer.MIN_VALUE;
            int currentEvaluation;
            List<ChessBoard.Field.Move> moves = this.depth == depth ? startingMoves : board.getAllValidMoves(board.getColorToMove());
            orderMoves(board, moves);

            for(ChessBoard.Field.Move move : moves) {
                board.makeMove(move);
                currentEvaluation = minimax(board, startingMoves, depth - 1, false, alpha, beta);
                board.unmakeMove(move);

                maximumEvaluation = Math.max(maximumEvaluation, currentEvaluation);

                if(this.depth == depth && currentEvaluation > bestEvaluation) {
                    bestEvaluation = currentEvaluation;
                    moveToMake = move;
                }

                alpha = Math.max(alpha, currentEvaluation);

                if(beta <= alpha) break;
            }

            return maximumEvaluation;
        }

        else {
            int minimumEvaluation = Integer.MAX_VALUE;
            int currentEvaluation;
            List<ChessBoard.Field.Move> moves = this.depth == depth ? startingMoves : board.getAllValidMoves(board.getColorToMove());
            orderMoves(board, moves);

            for(ChessBoard.Field.Move move : moves) {
                board.makeMove(move);
                currentEvaluation = minimax(board, startingMoves, depth - 1, true, alpha, beta);
                board.unmakeMove(move);

                minimumEvaluation = Math.min(currentEvaluation, minimumEvaluation);

                if(this.depth == depth && currentEvaluation < bestEvaluation) {
                    bestEvaluation = currentEvaluation;
                    moveToMake = move;
                }

                beta = Math.min(beta, currentEvaluation);

                if(beta <= alpha) break;
            }

            return minimumEvaluation;
        }
    }

    private void orderMoves(ChessBoard board, List<ChessBoard.Field.Move> moves) {
        moves.forEach(move -> guessValue(board, move));

        int sign = board.getColorToMove() == WHITE ? -1 : 1;

        moves.sort((m1, m2) -> sign * (m1.getGuessedValue() - m2.getGuessedValue()));
    }

    private void guessValue(ChessBoard board, ChessBoard.Field.Move move) {
        int guessedValue = 0;
        ChessBoard.ChessPiece.Piece capturePieceType = board.getField(move.getPosition()).getChessPiece().getPiece();

        guessedValue += capturePieceType.getValue();

        if(move.getSpecialMove() == SpecialMove.PROMOTION) {
            guessedValue += choosePromotion().getValue();
        }

        move.setGuessedValue(guessedValue);
    }

    private int evaluate(ChessBoard board) {
        return countMaterial(board, WHITE) - countMaterial(board, BLACK);
    }

    private int countMaterial(ChessBoard board, Color color) {
        int material = 0;

        for(int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                ChessBoard.Field field = board.getField(row, column);

                if (field.getChessPiece().getColor() == color) {
                    material += field.getChessPiece().getPiece().getValue();
                }
            }
        }

        return material;
    }
}
