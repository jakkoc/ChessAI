package chess;

import java.util.*;
import java.util.stream.Collectors;

public class ChessBoard {
    private final Field[][] board;
    private final ComputerAdversary adversary;
    private ChessPiece.Color colorToMove;
    private final Stack<ChessPiece> lastTaken;

    public static ChessBoard startingPosition() {
        Field[][] board = new Field[8][8];

        return new ChessBoard(board);
    }

    public ChessBoard(ChessBoard chessBoard) {
        Field[][] copiedBoard = new Field[8][8];

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                copiedBoard[row][column] = new Field(chessBoard.getField(row, column));
            }
        }

        board = copiedBoard;
        adversary = chessBoard.getAdversary();
        colorToMove = chessBoard.colorToMove;
        lastTaken = (Stack<ChessPiece>) chessBoard.lastTaken.clone();
    }

    private ChessBoard(Field[][] board) {
        this.board = board;
        adversary = new MiniMaxAdversary(ChessPiece.Color.BLACK, this, 3);
        lastTaken = new Stack<>();

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                board[row][column] = new Field(row, column, new ChessPiece());
            }
        }

        putPiecesOnStartingPositions(board);
        colorToMove = ChessPiece.Color.WHITE;
    }

    public void makeMove(Field.Move moveToMake) {
        ChessPiece movedPiece = new ChessPiece(getField(moveToMake.getFrom().getPosition()).getChessPiece());
        lastTaken.push(getField(moveToMake.getPosition()).getChessPiece());
        getField(moveToMake.getFrom().getPosition()).setChessPiece(new ChessPiece());
        getField(moveToMake.getPosition()).setChessPiece(movedPiece);
        changeTurn();
    }

    public void unmakeMove(Field.Move moveMade) {
        ChessPiece movedPiece = new ChessPiece(getField(moveMade.getPosition()).getChessPiece());
        getField(moveMade.getPosition()).setChessPiece(lastTaken.pop());
        getField(moveMade.getFrom().getPosition()).setChessPiece(movedPiece);
        changeTurn();
    }

    public ChessPiece.Color getWinner() {
        List<Field.Move> whiteMoves = new ArrayList<>();
        List<Field.Move> blackMoves = new ArrayList<>();
        ChessPiece.Color originalColor = colorToMove;

        for (Field[] fieldRow : board) {
            for (Field field : fieldRow) {
                if (field.getChessPiece().getColor() == ChessPiece.Color.WHITE) {
                    colorToMove = ChessPiece.Color.WHITE;
                    whiteMoves.addAll(field.getValidMoves());
                } else if (field.getChessPiece().getColor() == ChessPiece.Color.BLACK) {
                    colorToMove = ChessPiece.Color.BLACK;
                    blackMoves.addAll(field.getValidMoves());
                }
            }
        }

        colorToMove = originalColor;

        if (whiteMoves.isEmpty()) return ChessPiece.Color.BLACK;
        else if (blackMoves.isEmpty()) return ChessPiece.Color.WHITE;
        else return ChessPiece.Color.NONE;
    }

    public boolean isStalemate(ChessPiece.Color color) {
        ChessPiece.Color previousColor = colorToMove;
        ChessPiece.Color attackedColor = ChessPiece.Color.getOpposingColor(color);
        colorToMove = color;

        boolean isStalemate = !findKingPosition(attackedColor).isAttacked(color, true);

        colorToMove = previousColor;

        return isStalemate;
    }

    public ComputerAdversary getAdversary() {
        return adversary;
    }

    public List<Field.Move> getAllValidMoves(ChessBoard.ChessPiece.Color color) {
        List<Field.Move> validMoves = new ArrayList<>();

        for (Field[] fieldRow : board) {
            for (Field field : fieldRow) {
                if(field.getChessPiece().getColor() == color) {
                    validMoves.addAll(field.getValidMoves());
                }
            }
        }

        return validMoves;
    }

    public Field getField(int row, int column) {
        if (row < 0 || row > 7 || column < 0 || column > 7)
            throw new IllegalArgumentException("Such position does not exist!");

        return board[row][column];
    }

    public Field getField(Field.Position position) {
        return getField(position.getRow(), position.getColumn());
    }

    public void addPiece(int row, int column, ChessPiece chessPiece) {
        board[row][column].setChessPiece(chessPiece);
    }

    public void addPiece(Field.Position position, ChessPiece chessPiece) {
        addPiece(position.getRow(), position.getColumn(), chessPiece);
    }

    public void removePiece(int row, int column) {
        board[row][column].setChessPiece(new ChessPiece());
    }

    public void removePiece(Field.Position position) {
        removePiece(position.getRow(), position.getColumn());
    }

    public ChessPiece.Color getColorToMove() {
        return colorToMove;
    }

    public void changeTurn() {
        if (colorToMove == ChessPiece.Color.WHITE) colorToMove = ChessPiece.Color.BLACK;
        else colorToMove = ChessPiece.Color.WHITE;

        for (Field[] fieldRow : board) {
            for (Field field : fieldRow) {
                if (field.getChessPiece().getColor() != ChessPiece.Color.NONE) {
                    field.turnStanding++;
                }
            }
        }
    }

    private List<Field> fieldsAttackingKing() {
        List<Field> attackingFields = new ArrayList<>();
        ChessPiece chessPiece;
        boolean isAttacked;

        for (Field[] fieldRow : board) {
            for (Field field : fieldRow) {
                chessPiece = field.getChessPiece();
                isAttacked = field.getPosition().isAttacked(ChessPiece.Color.getOpposingColor(colorToMove), true);

                if (chessPiece.getPiece() == ChessPiece.Piece.KING && chessPiece.getColor() == colorToMove && isAttacked) {
                    attackingFields.add(field);
                }
            }
        }

        return attackingFields;
    }

    public Field.Position findKingPosition(ChessPiece.Color color) {
        for(Field[] fieldRow : board) {
            for (Field field : fieldRow) {
                if (field.getChessPiece().getColor() == color && field.getChessPiece().getPiece() == ChessPiece.Piece.KING) {
                    return field.getPosition();
                }
            }
        }

        return null;
    }

    private static void putPiecesOnStartingPositions(Field[][] board) {
        putPawns(board);
        putRooks(board);
        putKnights(board);
        putBishops(board);
        putQueens(board);
        putKings(board);
    }

    private static void putPawns(Field[][] board) {
        for (Field field : board[1]) {
            field.setChessPiece(new ChessPiece(ChessPiece.Color.BLACK, ChessPiece.Piece.PAWN));
        }

        for (Field field : board[6]) {
            field.setChessPiece(new ChessPiece(ChessPiece.Color.WHITE, ChessPiece.Piece.PAWN));
        }
    }

    private static void putRooks(Field[][] board) {
        board[0][0].setChessPiece(new ChessPiece(ChessPiece.Color.BLACK, ChessPiece.Piece.ROOK));
        board[0][7].setChessPiece(new ChessPiece(ChessPiece.Color.BLACK, ChessPiece.Piece.ROOK));
        board[7][0].setChessPiece(new ChessPiece(ChessPiece.Color.WHITE, ChessPiece.Piece.ROOK));
        board[7][7].setChessPiece(new ChessPiece(ChessPiece.Color.WHITE, ChessPiece.Piece.ROOK));
    }

    private static void putKnights(Field[][] board) {
        board[0][1].setChessPiece(new ChessPiece(ChessPiece.Color.BLACK, ChessPiece.Piece.KNIGHT));
        board[0][6].setChessPiece(new ChessPiece(ChessPiece.Color.BLACK, ChessPiece.Piece.KNIGHT));
        board[7][1].setChessPiece(new ChessPiece(ChessPiece.Color.WHITE, ChessPiece.Piece.KNIGHT));
        board[7][6].setChessPiece(new ChessPiece(ChessPiece.Color.WHITE, ChessPiece.Piece.KNIGHT));
    }

    private static void putBishops(Field[][] board) {
        board[0][2].setChessPiece(new ChessPiece(ChessPiece.Color.BLACK, ChessPiece.Piece.BISHOP));
        board[0][5].setChessPiece(new ChessPiece(ChessPiece.Color.BLACK, ChessPiece.Piece.BISHOP));
        board[7][2].setChessPiece(new ChessPiece(ChessPiece.Color.WHITE, ChessPiece.Piece.BISHOP));
        board[7][5].setChessPiece(new ChessPiece(ChessPiece.Color.WHITE, ChessPiece.Piece.BISHOP));
    }

    private static void putQueens(Field[][] board) {
        board[0][3].setChessPiece(new ChessPiece(ChessPiece.Color.BLACK, ChessPiece.Piece.QUEEN));
        board[7][3].setChessPiece(new ChessPiece(ChessPiece.Color.WHITE, ChessPiece.Piece.QUEEN));
    }

    private static void putKings(Field[][] board) {
        board[0][4].setChessPiece(new ChessPiece(ChessPiece.Color.BLACK, ChessPiece.Piece.KING));
        board[7][4].setChessPiece(new ChessPiece(ChessPiece.Color.WHITE, ChessPiece.Piece.KING));
    }

    /**
     * Represents a single field on the chess board
     */
    class Field {
        private final Position position;
        private int turnStanding;
        private ChessPiece chessPiece;

        public Field(int row, int column, ChessPiece chessPiece) {
            if (chessPiece == null) throw new IllegalArgumentException();

            this.position = new Position(row, column);
            this.chessPiece = chessPiece;
        }

        public Field(Field field) {
            position = new Position(field.getPosition());
            turnStanding = field.turnStanding;
            chessPiece = new ChessPiece(field.getChessPiece());
        }

        public List<Move> getValidMoves() {
            List<Move> possibleMoves = new ArrayList<>();
            Field targetField;
            ChessPiece previousFigure;

            for (Move move : getIntermediateMoves(false)) {
                Position position = move.getPosition();

                targetField = board[position.getRow()][position.getColumn()];

                previousFigure = new ChessPiece(targetField.getChessPiece());
                targetField.setChessPiece(chessPiece);
                chessPiece = new ChessPiece();

                if (fieldsAttackingKing().size() == 0) {
                    possibleMoves.add(move);
                }

                chessPiece = targetField.getChessPiece();
                targetField.setChessPiece(previousFigure);
            }

            return possibleMoves;
        }

        public Position getPosition() {
            return position;
        }

        public ChessPiece getChessPiece() {
            return chessPiece;
        }

        public void setChessPiece(ChessPiece chessPiece) {
            if (chessPiece == null) throw new IllegalArgumentException();
            this.chessPiece = chessPiece;
        }

        public void setTurnStanding(int turnStanding) {
            this.turnStanding = turnStanding;
        }

        /**
         * Moves that can be made from this field not taking kings safety into account
         *
         * @param excludeKing Whether fields attacked by king should be ignored
         * @return List of all possible moves that can be made not taking king safety into account
         */
        private List<Move> getIntermediateMoves(boolean excludeKing) {
            if (chessPiece == null || chessPiece.getColor() != colorToMove) {
                return new ArrayList<>();
            }

            switch (chessPiece.piece) {
                case KING:
                    return excludeKing ? new ArrayList<>() : generateKingMoves();
                case QUEEN:
                    return generateQueenMoves();
                case ROOK:
                    return generateRookMoves();
                case BISHOP:
                    return generateBishopMoves();
                case KNIGHT:
                    return generateKnightMoves();
                case PAWN:
                    return generatePawnMoves();
                default:
                    return new ArrayList<>();
            }
        }

        private List<Move> generateKingMoves() {
            List<Move> possibleMoves = new ArrayList<>();

            for (int i = 0; i < 4; i++) {
                possibleMoves.addAll(checkDiagonals(1, i));
            }

            if (canCastle(-1))
                possibleMoves.add(new Move(new Position(position.row, 2),this, ChessPiece.SpecialMove.CASTLE));
            if (canCastle(1)) possibleMoves.add(new Move(new Position(position.row, 6), this, ChessPiece.SpecialMove.CASTLE));

            possibleMoves.addAll(checkColumns(1, -1));
            possibleMoves.addAll(checkColumns(1, 1));

            for (Move move : checkRows(1, -1)) {
                if (possibleMoves.stream().noneMatch(m -> m.getPosition().equals(move.getPosition()))) {
                    possibleMoves.add(move);
                }
            }

            for (Move move : checkRows(1, 1)) {
                if (possibleMoves.stream().noneMatch(m -> m.getPosition().equals(move.getPosition()))) {
                    possibleMoves.add(move);
                }
            }


            return filterInvalidMoves(possibleMoves);
        }

        /**
         * Checks whether king can castle
         *
         * @param direction Direction in which to castle(-1 to the left, 1 - to the right)
         * @return whether king can castle
         */
        private boolean canCastle(int direction) {
            int startingColumn = direction == -1 ? 1 : 4;
            int endingColumn = direction == -1 ? 4 : 6;
            ChessPiece piece = getField(position.row, direction == -1 ? 0 : 7).getChessPiece();
            Field currentField;

            if (areInCorrectPositionsForCastle(piece)) {

                for (int i = startingColumn; i <= endingColumn; i++) {
                    currentField = getField(position.getRow(), i);

                    if (!canPassForCastle(currentField)) {
                        return false;
                    }
                }

                return true;
            }

            return false;
        }

        /**
         * Checks whether king can pass through this field for castle
         *
         * @param field Field to pass through
         * @return whether king can pass given field
         */
        private boolean canPassForCastle(Field field) {
            ChessPiece.Color opposingColor = ChessPiece.Color.getOpposingColor(colorToMove);

            return !field.getPosition().isAttacked(opposingColor, false) && field.getChessPiece().getColor() == ChessPiece.Color.NONE;
        }

        /**
         * Checks whether king and rook are in correct positions for castle
         *
         * @param piece Piece at the position of rook
         * @return whether king and rook are in correct positions for castle
         */
        private boolean areInCorrectPositionsForCastle(ChessPiece piece) {
            return chessPiece.getMovesMade() == 0 && piece.getPiece() == ChessPiece.Piece.ROOK && piece.getColor() == chessPiece.color && piece.getMovesMade() == 0;
        }

        private List<Move> filterInvalidMoves(List<Move> moves) {
            moves = moves.stream().filter(move -> !move.getPosition().isAttacked(ChessPiece.Color.getOpposingColor(colorToMove), true)).collect(Collectors.toList());
            return filterMovesAttackingEnemyKing(moves);
        }

        /**
         * Removes the moves that would put the king too close to enemy king
         * @param moves All moves that the king could make
         */
        private List<Move> filterMovesAttackingEnemyKing(List<Move> moves) {
            Position enemyKingPosition = findKingPosition(ChessPiece.Color.getOpposingColor(colorToMove));

            moves.removeIf(move -> {
                if(enemyKingPosition != null) return move.getPosition().distanceFrom(enemyKingPosition) < 2;
                return false;
            });

            return moves;
        }

        private List<Move> generateQueenMoves() {
            List<Move> possibleMoves = new ArrayList<>();

            for (int i = 0; i < 4; i++) {
                possibleMoves.addAll(checkDiagonals(8, i));
            }

            possibleMoves.addAll(checkRows(8, -1));
            possibleMoves.addAll(checkRows(8, 1));
            possibleMoves.addAll(checkColumns(8, -1));
            possibleMoves.addAll(checkColumns(8, 1));

            return possibleMoves;
        }

        private List<Move> generateRookMoves() {
            List<Move> possibleMoves = new ArrayList<>();

            possibleMoves.addAll(checkColumns(8, -1));
            possibleMoves.addAll(checkColumns(8, 1));
            possibleMoves.addAll(checkRows(8, -1));
            possibleMoves.addAll(checkRows(8, 1));

            return possibleMoves;
        }

        private List<Move> generateBishopMoves() {
            List<Move> possibleMoves = new ArrayList<>();

            for (int i = 0; i < 4; i++) {
                possibleMoves.addAll(checkDiagonals(8, i));
            }

            return possibleMoves;
        }

        private List<Move> generateKnightMoves() {
            List<Move> possibleMoves = new ArrayList<>();

            for (Move move : generateProposedKnightMoves()) {

                if (move.getPosition().isValid() && getField(move.getPosition()).getChessPiece().getColor() != colorToMove) {
                    possibleMoves.add(move);
                }
            }

            return possibleMoves;
        }

        private List<Move> generateProposedKnightMoves() {
            List<Move> proposedMoves = new ArrayList<>();

            for (int i = -2; i <= 2; i++) {
                for (int j = -2; j <= 2; j++) {
                    if (Math.abs(i) + Math.abs(j) == 3) {
                        proposedMoves.add(new Move(new Position(position.row + i, position.column + j), this));
                    }
                }
            }

            return proposedMoves;
        }

        private List<Move> generatePawnMoves() {
            List<Move> possibleMoves = new ArrayList<>();
            int moveDirection = chessPiece.getColor() == ChessPiece.Color.WHITE ? -1 : 1;
            Move proposedMove = new Move(new Position(position.getRow() + moveDirection, position.getColumn()),this, ChessPiece.SpecialMove.NON_ATTACKING);

            if (proposedMove.getPosition().isValid() && getField(proposedMove.getPosition()).getChessPiece().getPiece() == ChessPiece.Piece.NONE) {
                if (canBePromoted(proposedMove)) {
                    proposedMove.setSpecialMove(ChessPiece.SpecialMove.PROMOTION);
                }

                possibleMoves.add(proposedMove);
            }

            proposedMove = new Move(new Position(position.getRow() + 2 * moveDirection, position.getColumn()),this, ChessPiece.SpecialMove.NON_ATTACKING);

            if (proposedMove.getPosition().isValid() && chessPiece.getMovesMade() == 0 && !possibleMoves.isEmpty() && getField(proposedMove.getPosition()).getChessPiece().getPiece() == ChessPiece.Piece.NONE) {

                possibleMoves.add(proposedMove);
            }

            proposedMove = attackingMove(-1);
            if (proposedMove != null) possibleMoves.add(proposedMove);

            proposedMove = attackingMove(1);
            if (proposedMove != null) possibleMoves.add(proposedMove);

            proposedMove = enPassant(-1);
            if (proposedMove != null) possibleMoves.add(proposedMove);

            proposedMove = enPassant(1);
            if (proposedMove != null) possibleMoves.add(proposedMove);

            return possibleMoves;
        }

        /**
         * Checks whether pawn can make en passant move in specified direction
         *
         * @param direction Direction to attack(-1 to the left, 1 to the right)
         * @return move if it can be made or null
         */
        private Move enPassant(int direction) {
            int moveDirection = chessPiece.getColor() == ChessPiece.Color.BLACK ? 1 : -1;
            Move proposedMove = new Move(new Position(position.row, position.column + direction),this, ChessPiece.SpecialMove.EN_PASSANT);

            if (proposedMove.getPosition().isValid() && doublePawnMoveMade(getField(proposedMove.getPosition()))) {
                proposedMove.getPosition().setRow(proposedMove.getPosition().getRow() + moveDirection);
                return proposedMove;
            }

            return null;
        }

        //Checks whether pawn move spanning two fields was made on this field previous turn
        private boolean doublePawnMoveMade(Field field) {
            ChessPiece.Color opposingColor = ChessPiece.Color.getOpposingColor(colorToMove);
            boolean doubleMove = field.turnStanding == 1 && (field.getChessPiece().getColor() == ChessPiece.Color.BLACK ? 3 : 4) == field.getPosition().getRow();

            return field.getChessPiece().getColor() == opposingColor && field.getChessPiece().getPiece() == ChessPiece.Piece.PAWN && doubleMove;
        }

        /**
         * Checks whether pawn can attack in specified direction
         *
         * @param direction Direction to attack(-1 to the left, 1 to the right)
         * @return move if it can be made or null
         */
        private Move attackingMove(int direction) {
            int moveDirection = chessPiece.getColor() == ChessPiece.Color.BLACK ? 1 : -1;
            ChessPiece.Color opposingColor = ChessPiece.Color.getOpposingColor(colorToMove);
            Move proposedMove = new Move(new Position(position.getRow() + moveDirection, position.getColumn() + direction), this);

            if (proposedMove.getPosition().isValid() && getField(proposedMove.getPosition()).getChessPiece().getColor() == opposingColor) {
                if (canBePromoted(proposedMove)) {
                    proposedMove.setSpecialMove(ChessPiece.SpecialMove.PROMOTION);
                }

                return proposedMove;
            }

            return null;
        }

        /**
         * Checks whether the pawn can be promoted after the specified move
         *
         * @param move Move to be made
         * @return whether the pawn can be promoted after the specified move
         */
        private boolean canBePromoted(Move move) {
            return move.getPosition().getRow() == 0 || move.getPosition().getRow() == 7;
        }

        /**
         * Method that returns all possible moves that the piece can make following specified diagonal
         *
         * @param length   Maximum length of diagonal move
         * @param diagonal Diagonal to follow( 0 - lower left, 1 - upper left, 2 - lower right, 3 - upper right)
         * @return list of valid moves for piece following specified diagonal of specified length
         */
        private List<Move> checkDiagonals(int length, int diagonal) {

            List<Move> diagonalMoves = new ArrayList<>();
            int rowDirection = diagonal / 2 == 0 ? -1 : 1;
            int columnDirection = diagonal % 2 == 0 ? -1 : 1;
            Move currentMove = new Move(new Position(position.row + rowDirection, position.column + columnDirection), this);
            Field currentField;
            int counter = 0;

            while (currentMove.getPosition().isValid() && counter < length) {
                currentField = getField(currentMove.getPosition());

                if (currentField.getChessPiece().getColor() == ChessPiece.Color.NONE) {
                    diagonalMoves.add(currentMove);
                } else if (currentField.getChessPiece().getColor() == chessPiece.getColor()) {
                    break;
                } else {
                    diagonalMoves.add(currentMove);
                    break;
                }


                counter++;
                currentMove = new Move(new Position(currentField.getPosition().getRow() + rowDirection, currentField.getPosition().getColumn() + columnDirection), this);
            }

            return diagonalMoves;
        }

        /**
         * Method that returns all possible moves that the piece can make following specified row
         *
         * @param length    Maximum length of the move in this direction
         * @param direction Direction of the move(1 - means to the right, -1 means to the left)
         * @return All possible moves that can me made in this direction of that length
         */
        private List<Move> checkRows(int length, int direction) {
            List<Move> rowMoves = new ArrayList<>();
            Move currentMove = new Move(new Position(position.row, position.column + direction), this);
            int counter = 0;
            Field currentField;

            while (currentMove.getPosition().isValid() && counter < length) {
                currentField = getField(currentMove.getPosition());

                if (currentField.getChessPiece().getColor() == ChessPiece.Color.NONE) {
                    rowMoves.add(currentMove);
                } else if (currentField.getChessPiece().getColor() == chessPiece.getColor()) {
                    break;
                } else {
                    rowMoves.add(currentMove);
                    break;
                }


                counter++;
                currentMove = new Move(new Position(currentField.getPosition().getRow(), currentField.getPosition().getColumn() + direction), this);
            }

            return rowMoves;
        }

        /**
         * Method that returns all possible moves that the piece can make following specified column
         *
         * @param length    Maximum length of the move in this direction
         * @param direction Direction of the move(1 - means up, -1 means down)
         * @return All possible moves that can me made in this direction of that length
         */
        private List<Move> checkColumns(int length, int direction) {
            List<Move> columnMoves = new ArrayList<>();
            Move currentMove = new Move(new Position(position.getRow() + direction, position.getColumn()), this);
            Field currentField;
            int counter = 0;

            while (currentMove.getPosition().isValid() && counter < length) {
                currentField = getField(currentMove.getPosition());

                if (currentField.getChessPiece().getColor() == ChessPiece.Color.NONE) {
                    columnMoves.add(currentMove);
                } else if (currentField.getChessPiece().getColor() == chessPiece.getColor()) {
                    break;
                } else {
                    columnMoves.add(currentMove);
                    break;
                }

                counter++;
                currentMove = new Move(new Position(currentField.getPosition().getRow() + direction, currentField.getPosition().getColumn()), this);
            }

            return columnMoves;
        }

        /**
         * Represents coordinates on the chess board
         */
        class Position {
            private int row;
            private int column;

            public Position(int row, int column) {
                this.row = row;
                this.column = column;
            }

            public Position(Position position) {
                this(position.getRow(), position.getColumn());
            }

            public int getRow() {
                return row;
            }

            public int getColumn() {
                return column;
            }

            public void setRow(int row) {
                this.row = row;
            }

            public void setColumn(int column) {
                this.column = column;
            }

            private boolean isValid() {
                return row >= 0 && row <= 7 && column >= 0 && column <= 7;
            }

            //TODO DEBUG
            public String toString() {
                return row + " " + column;
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof Position)) return false;

                Position otherPosition = (Position) o;

                return otherPosition.row == row && otherPosition.column == column;
            }

            public boolean isAttacked(ChessPiece.Color color, boolean excludingKing) {
                ChessPiece.Color previousColor = colorToMove;
                colorToMove = color;

                for (Field[] fieldRow : board) {
                    for (Field field : fieldRow) {
                        if (field.getChessPiece().getColor() == color) {
                            if (!(field.getChessPiece().getPiece() == ChessPiece.Piece.KING && excludingKing) && field.getIntermediateMoves(true).stream().anyMatch(move -> move.getPosition().equals(this) && move.getSpecialMove() != ChessPiece.SpecialMove.NON_ATTACKING)) {
                                colorToMove = previousColor;
                                return true;
                            }
                        }
                    }
                }

                colorToMove = previousColor;
                return false;
            }

            //Returns distance from other position
            public int distanceFrom(Position otherPosition) {
                int rowDistance = Math.abs(row - otherPosition.row);
                int columnDistance = Math.abs(column - otherPosition.column);

                return Math.max(rowDistance, columnDistance);
            }
        }

        class Move {
            private final Field from;
            private final Position position;
            private ChessPiece.SpecialMove specialMove;

            public Move(Position position, Field from) {
                this.position = position;
                this.from = from;
                specialMove = ChessPiece.SpecialMove.NONE;
            }

            public Move(Position position, Field from, ChessPiece.SpecialMove specialMove) {
                this(position, from);
                this.specialMove = specialMove;
            }

            public Position getPosition() {
                return position;
            }

            public Field getFrom() {
                return from;
            }

            public ChessPiece.SpecialMove getSpecialMove() {
                return specialMove;
            }

            public void setSpecialMove(ChessPiece.SpecialMove specialMove) {
                this.specialMove = specialMove;
            }
        }
    }

    /**
     * Represents chess piece on the board
     */
    static class ChessPiece {
        private final Color color;
        private Piece piece;
        private int movesMade;

        public ChessPiece() {
            this(Color.NONE, Piece.NONE, 0);
        }

        public ChessPiece(Color color, Piece piece) {
            this(color, piece, 0);
        }

        public ChessPiece(ChessPiece chessPiece) {
            this(chessPiece.getColor(), chessPiece.getPiece(), chessPiece.movesMade);
        }

        public ChessPiece(Color color, Piece piece, int movesMade) {
            this.color = color == null ? Color.NONE : color;
            this.piece = piece == null ? Piece.NONE : piece;
            this.movesMade = movesMade;
        }

        /**
         * Finds path to image representing this chess piece
         *
         * @return path to the image or "NoPath" in case of no piece or no color
         */
        public String getImagePath() {
            return color == Color.NONE || piece == Piece.NONE ? "NoPath" : String.format("Images/%s/%s.png", color.getColor(), piece.getName());
        }

        public Color getColor() {
            return color;
        }

        public Piece getPiece() {
            return piece;
        }

        public int getMovesMade() {
            return movesMade;
        }

        public void setPiece(Piece piece) {
            this.piece = piece;
        }

        public void incrementMovesMade() {
            movesMade++;
        }

        /**
         * Type of figure on the board
         */
        enum Piece {
            NONE("NoName", 0),
            PAWN("Pawn", 100),
            ROOK("Rook", 500),
            KNIGHT("Knight", 300),
            BISHOP("Bishop", 300),
            QUEEN("Queen", 900),
            KING("King", 0);

            private final String name;
            private final int value;

            Piece(String name, int value) {
                this.name = name;
                this.value = value;
            }

            public String getName() {
                return name;
            }

            public int getValue() {
                return value;
            }
        }

        /**
         * Color of the figure
         */
        enum Color {
            NONE("NoColor"),
            WHITE("White"),
            BLACK("Black");

            private final String color;

            Color(String color) {
                this.color = color;
            }

            public String getColor() {
                return color;
            }

            public static Color getOpposingColor(Color color) {
                if (color == NONE) return NONE;
                return color == WHITE ? BLACK : WHITE;
            }
        }

        //Represents special moves on the chess board
        enum SpecialMove {
            NONE,
            NON_ATTACKING,
            EN_PASSANT,
            PROMOTION,
            CASTLE
        }
    }
}
