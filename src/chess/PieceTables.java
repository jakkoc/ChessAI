package chess;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static chess.ChessBoard.ChessPiece.Color.BLACK;
import static chess.ChessBoard.ChessPiece.Color.WHITE;

public class PieceTables {
    private static Integer[][] pawnTable;
    private static Integer[][] knightTable;
    private static Integer[][] bishopTable;
    private static Integer[][] rookTable;
    private static Integer[][] queenTable;
    private static Integer[][] kingMidGameTable;
    private static Integer[][] kingLateGameTable;

    public static void initializePieceTables() {
        pawnTable = parseInputToPieceTable("PieceTables/Pawn.txt");
        knightTable = parseInputToPieceTable("PieceTables/Knight.txt");
        bishopTable = parseInputToPieceTable("PieceTables/Bishop.txt");
        rookTable = parseInputToPieceTable("PieceTables/Rook.txt");
        queenTable = parseInputToPieceTable("PieceTables/Queen.txt");
        kingMidGameTable = parseInputToPieceTable("PieceTables/KingMidGame.txt");
        kingLateGameTable = parseInputToPieceTable("PieceTables/KingLateGame.txt");
    }

    public static int evaluateMovePositionChange(ChessBoard board, ChessBoard.Field.Move move) {
        ChessBoard.ChessPiece chessPiece = move.getFrom().getChessPiece();

        return positionValue(move.getPosition(), chessPiece, board.isEndgame()) - positionValue(move.getFrom().getPosition(), chessPiece, board.isEndgame());
    }

    private static int positionValue(ChessBoard.Field.Position position, ChessBoard.ChessPiece chessPiece, boolean isEndgame) {
        ChessBoard.ChessPiece.Color color = chessPiece.getColor();
        int row = color == WHITE ? position.getRow() : 7 - position.getRow();
        int column = color == BLACK ? position.getColumn() : 7 - position.getColumn();

        switch(chessPiece.getPiece()) {
            case PAWN:
                return pawnTable[row][column];
            case BISHOP:
                return bishopTable[row][column];
            case KING:
                return isEndgame ? kingLateGameTable[row][column] : kingMidGameTable[row][column];
            case KNIGHT:
                return knightTable[row][column];
            case ROOK:
                return rookTable[row][column];
            case QUEEN:
                return queenTable[row][column];
            default:
                return 0;
        }
    }

    private static Integer[][] parseInputToPieceTable(String filepath) {
        Integer[][] pieceTable = new Integer[8][8];
        String[] tokens;
        int currentLine = 0;

        try(Scanner scanner = new Scanner(new File(filepath))) {
            while(scanner.hasNextLine()) {
                tokens = scanner.nextLine().split(",");

                for (int column = 0; column < tokens.length; column++) {
                    pieceTable[currentLine][column] = Integer.parseInt(tokens[column].trim());
                }

                currentLine++;
            }

        } catch(FileNotFoundException e) {
            Logger.getLogger("global").log(Level.FINE,filepath + " not found!", e);
        }

        return pieceTable;
    }
}
