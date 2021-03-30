package chess;

import java.util.*;

import static chess.ChessBoard.ChessPiece.Color.BLACK;

public class ChessBoard {
    private final Field[][] board;
    private ComputerAdversary adversary;
    private ChessPiece.Color colorToMove;
    private final Stack<ChessPiece> lastTaken;
    private boolean whiteQueenStanding;
    private boolean blackQueenStanding;
    private Field whiteKingField;
    private Field blackKingField;

    public static ChessBoard startingPosition() {
        Field[][] board = new Field[8][8];
        PieceTables.initializePieceTables();

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
        whiteQueenStanding = chessBoard.whiteQueenStanding;
        blackQueenStanding = chessBoard.blackQueenStanding;
        whiteKingField = new Field(chessBoard.whiteKingField);
        blackKingField = new Field(chessBoard.blackKingField);
    }

    private ChessBoard(Field[][] board) {
        this.board = board;
        adversary = new MiniMaxAdversary(BLACK, this, 3, true);
        lastTaken = new Stack<>();
        whiteQueenStanding = true;
        blackQueenStanding = true;

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                board[row][column] = new Field(row, column, new ChessPiece());
            }
        }

        putPiecesOnStartingPositions(board);
        colorToMove = ChessPiece.Color.WHITE;
    }

    public void setAdversary(ComputerAdversary adversary) {
        this.adversary = adversary;
    }

    public boolean cannotMove(ChessPiece.Color color) {
        ChessPiece.Color previousColor = colorToMove;
        colorToMove = color;

        List<Field.Move> possibleMoves = getAllValidMoves(color);

        colorToMove = previousColor;

        return possibleMoves.isEmpty();
    }

    public boolean isCheckMate(ChessPiece.Color matingSite) {
        Field enemyKingField = colorToMove == ChessPiece.Color.WHITE ? whiteKingField : blackKingField;
        ChessPiece.Color opposingColor = ChessPiece.Color.getOpposingColor(matingSite);

        boolean isAttacked = enemyKingField.getPosition().isAttacked(matingSite);


        return cannotMove(opposingColor) && isAttacked;
    }

    public void makeMove(Field.Move moveToMake) {
        ChessPiece movedPiece = new ChessPiece(getField(moveToMake.getFrom().getPosition()).getChessPiece());
        lastTaken.push(getField(moveToMake.getPosition()).getChessPiece());
        getField(moveToMake.getFrom().getPosition()).setChessPiece(new ChessPiece());
        getField(moveToMake.getPosition()).setChessPiece(movedPiece);
        changeTurn();

        if(lastTaken.peek().getPiece() == ChessPiece.Piece.QUEEN) {
            if(lastTaken.peek().getColor() == ChessPiece.Color.WHITE) whiteQueenStanding = false;
            else blackQueenStanding = false;
        }

        if (movedPiece.getPiece() == ChessPiece.Piece.KING) {
            if(movedPiece.getColor() == ChessPiece.Color.WHITE) whiteKingField = getField(moveToMake.getPosition());
            else blackKingField = getField(moveToMake.getPosition());
        }

    }

    public void unmakeMove(Field.Move moveMade) {
        ChessPiece chessPiece = lastTaken.pop();

        ChessPiece movedPiece = new ChessPiece(getField(moveMade.getPosition()).getChessPiece());
        getField(moveMade.getPosition()).setChessPiece(chessPiece);
        getField(moveMade.getFrom().getPosition()).setChessPiece(movedPiece);
        changeTurn();

        if(chessPiece.getPiece() == ChessPiece.Piece.QUEEN) {
            if(chessPiece.getColor() == ChessPiece.Color.WHITE) whiteQueenStanding = true;
            else blackQueenStanding = true;
        }

        if(movedPiece.getPiece() == ChessPiece.Piece.KING) {
            if(movedPiece.getColor() == ChessPiece.Color.WHITE) whiteKingField = moveMade.getFrom();
            else blackKingField = moveMade.getFrom();
        }
    }

    public boolean isEndgame() {
        return !whiteQueenStanding && !blackQueenStanding;
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

        if(chessPiece.getPiece() == ChessPiece.Piece.QUEEN) {
            if(chessPiece.getColor() == ChessPiece.Color.WHITE) whiteQueenStanding = true;
            else blackQueenStanding = true;
        }

        if (chessPiece.getPiece() == ChessPiece.Piece.KING) {
            if (chessPiece.getColor() == ChessPiece.Color.WHITE) whiteKingField = getField(row, column);
            else blackKingField = getField(row, column);
        }
    }

    public void addPiece(Field.Position position, ChessPiece chessPiece) {
        addPiece(position.getRow(), position.getColumn(), chessPiece);
    }

    public void removePiece(int row, int column) {
        ChessPiece removedPiece = board[row][column].getChessPiece();

        if(removedPiece.getPiece() == ChessPiece.Piece.QUEEN) {
            if(removedPiece.getColor() == ChessPiece.Color.WHITE) whiteQueenStanding = false;
            else blackQueenStanding = false;
        }

        board[row][column].setChessPiece(new ChessPiece());
    }

    public void removePiece(Field.Position position) {
        removePiece(position.getRow(), position.getColumn());
    }

    public ChessPiece.Color getColorToMove() {
        return colorToMove;
    }

    public void changeTurn() {
        if (colorToMove == ChessPiece.Color.WHITE) colorToMove = BLACK;
        else colorToMove = ChessPiece.Color.WHITE;

        for (Field[] fieldRow : board) {
            for (Field field : fieldRow) {
                if (field.getChessPiece().getColor() != ChessPiece.Color.NONE) {
                    field.turnStanding++;
                }
            }
        }
    }

    private void putPiecesOnStartingPositions(Field[][] board) {
        putPawns(board);
        putRooks(board);
        putKnights(board);
        putBishops(board);
        putQueens(board);
        putKings(board);
    }

    private void putPawns(Field[][] board) {
        for (Field field : board[1]) {
            field.setChessPiece(new ChessPiece(BLACK, ChessPiece.Piece.PAWN));
        }

        for (Field field : board[6]) {
            field.setChessPiece(new ChessPiece(ChessPiece.Color.WHITE, ChessPiece.Piece.PAWN));
        }
    }

    private void putRooks(Field[][] board) {
        board[0][0].setChessPiece(new ChessPiece(BLACK, ChessPiece.Piece.ROOK));
        board[0][7].setChessPiece(new ChessPiece(BLACK, ChessPiece.Piece.ROOK));
        board[7][0].setChessPiece(new ChessPiece(ChessPiece.Color.WHITE, ChessPiece.Piece.ROOK));
        board[7][7].setChessPiece(new ChessPiece(ChessPiece.Color.WHITE, ChessPiece.Piece.ROOK));
    }

    private void putKnights(Field[][] board) {
        board[0][1].setChessPiece(new ChessPiece(BLACK, ChessPiece.Piece.KNIGHT));
        board[0][6].setChessPiece(new ChessPiece(BLACK, ChessPiece.Piece.KNIGHT));
        board[7][1].setChessPiece(new ChessPiece(ChessPiece.Color.WHITE, ChessPiece.Piece.KNIGHT));
        board[7][6].setChessPiece(new ChessPiece(ChessPiece.Color.WHITE, ChessPiece.Piece.KNIGHT));
    }

    private void putBishops(Field[][] board) {
        board[0][2].setChessPiece(new ChessPiece(BLACK, ChessPiece.Piece.BISHOP));
        board[0][5].setChessPiece(new ChessPiece(BLACK, ChessPiece.Piece.BISHOP));
        board[7][2].setChessPiece(new ChessPiece(ChessPiece.Color.WHITE, ChessPiece.Piece.BISHOP));
        board[7][5].setChessPiece(new ChessPiece(ChessPiece.Color.WHITE, ChessPiece.Piece.BISHOP));
    }

    private void putQueens(Field[][] board) {
        board[0][3].setChessPiece(new ChessPiece(BLACK, ChessPiece.Piece.QUEEN));
        board[7][3].setChessPiece(new ChessPiece(ChessPiece.Color.WHITE, ChessPiece.Piece.QUEEN));
    }

    private void putKings(Field[][] board) {
        board[0][4].setChessPiece(new ChessPiece(BLACK, ChessPiece.Piece.KING));
        board[7][4].setChessPiece(new ChessPiece(ChessPiece.Color.WHITE, ChessPiece.Piece.KING));

        whiteKingField = getField(7, 4);
        blackKingField = getField(0, 4);
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
         * Lst of valid moves taking king safety into account
         * @return list mentioned
         */
        public List<Move> getValidMoves() {
            List<Move> validMoves = new ArrayList<>();

            ChessPiece.Color opposingColor = ChessPiece.Color.getOpposingColor(chessPiece.getColor());

            for (Move move : intermediateMoves()) {

                ChessPiece attackedPiece = getField(move.getPosition()).getChessPiece();

                makeMove(move);

                Position kingPosition = (getColorToMove() == ChessPiece.Color.WHITE ? blackKingField : whiteKingField).getPosition();

                if(!kingPosition.isAttacked(opposingColor) && attackedPiece.getPiece() != ChessPiece.Piece.KING) {
                    validMoves.add(move);
                }

                unmakeMove(move);
            }

            return validMoves;
        }

        /**
         * List of moves that can be made from this field not taking king safety into account
         * @return List of said moves
         */
        private List<Move> intermediateMoves() {
            if(chessPiece.getColor() != colorToMove) {
                return new ArrayList<>();
            }

            switch(chessPiece.getPiece()) {
                case PAWN:
                    return generatePawnMoves();
                case BISHOP:
                    return generateBishopMoves();
                case KNIGHT:
                    return generateKnightMoves();
                case ROOK:
                    return generateRookMoves();
                case QUEEN:
                    return generateQueenMoves();
                case KING:
                    return generateKingMoves();
                default:
                    return new ArrayList<>();
            }
        }

        /** Method that returns all possible moves that the pawn can make
         * @return All possible moves that the pawn can make not taking king safety into account
         */
        private List<Move> generatePawnMoves() {
            List<Move> possibleMoves = new ArrayList<>();
            int moveDirection = chessPiece.getColor() == ChessPiece.Color.WHITE ? -1 : 1;
            Position currentPosition = getPosition();
            Move proposedMove;

            //Move one square forward
            proposedMove = new Move(new Position(currentPosition.getRow() + moveDirection, currentPosition.getColumn()), getField(currentPosition), ChessPiece.SpecialMove.NON_ATTACKING);


            if (proposedMove.getPosition().isValid()  && getField(proposedMove.getPosition()).getChessPiece().getColor() == ChessPiece.Color.NONE) {
                if(proposedMove.getPosition().getRow() == 0 || proposedMove.getPosition().getRow() == 7) {
                    proposedMove.setSpecialMove(ChessPiece.SpecialMove.PROMOTION);
                }

                possibleMoves.add(proposedMove);
            }

            //Move two squares forward
            proposedMove = new Move(new Position(currentPosition.getRow() + 2 * moveDirection, currentPosition.getColumn()), getField(currentPosition), ChessPiece.SpecialMove.NON_ATTACKING);

            if(chessPiece.getMovesMade() == 0 && !possibleMoves.isEmpty() && getField(proposedMove.getPosition()).getChessPiece().getColor() == ChessPiece.Color.NONE) {
                possibleMoves.add(proposedMove);
            }

            //Capture
            if(pawnCanCapture(-1)) {
                proposedMove = new Move(new Position(currentPosition.getRow() + moveDirection, currentPosition.getColumn() - 1), getField(currentPosition));

                if(proposedMove.getPosition().getRow() == 0 || proposedMove.getPosition().getRow() == 7) {
                    proposedMove.setSpecialMove(ChessPiece.SpecialMove.PROMOTION);
                }

                possibleMoves.add(proposedMove);
            }

            if(pawnCanCapture(1)) {
                proposedMove = new Move(new Position(currentPosition.getRow() + moveDirection, currentPosition.getColumn() + 1), getField(currentPosition));

                if(proposedMove.getPosition().getRow() == 0 || proposedMove.getPosition().getRow() == 7) {
                    proposedMove.setSpecialMove(ChessPiece.SpecialMove.PROMOTION);
                }

                possibleMoves.add(proposedMove);
            }

            //En Passant
            if(pawnCanEnPassant(-1)) {
                proposedMove = new Move(new Position(currentPosition.getRow() + moveDirection, currentPosition.getColumn() - 1),getField(currentPosition));
                proposedMove.setSpecialMove(ChessPiece.SpecialMove.EN_PASSANT);

                possibleMoves.add(proposedMove);
            }

            if(pawnCanEnPassant(1)) {
                proposedMove = new Move(new Position(currentPosition.getRow() + moveDirection, currentPosition.getColumn() + 1),getField(currentPosition));
                proposedMove.setSpecialMove(ChessPiece.SpecialMove.EN_PASSANT);

                possibleMoves.add(proposedMove);
            }

            return possibleMoves;
        }

        /**
         * Tests whether pawn can capture a piece in diagonal specified by direction
         * @param direction -1 is left diagonal and 1 is right diagonal
         * @return whether pawn can capture a piece in specified diagonal
         */
        private boolean pawnCanCapture(int direction) {
            int moveDirection = chessPiece.getColor() == ChessPiece.Color.WHITE ? -1 : 1;
            ChessPiece.Color opposingColor = ChessPiece.Color.getOpposingColor(chessPiece.getColor());

            Position attackedPosition = new Position(getPosition().getRow() + moveDirection, getPosition().getColumn() + direction);

            if (attackedPosition.isValid() && getField(attackedPosition).getChessPiece().getColor() == opposingColor) {
                return true;
            }

            return false;
        }

        /**
         * Tests whether pawn can make an en passant move
         * @param direction direction to make an en passant move
         * @return whether en passant move is possible
         */
        private boolean pawnCanEnPassant(int direction) {
            int moveDirection = chessPiece.getColor() == ChessPiece.Color.WHITE ? -1 : 1;
            ChessPiece.Color opposingColor = ChessPiece.Color.getOpposingColor(chessPiece.getColor());

            Position attackedPosition = new Position(getPosition().getRow() + moveDirection, getPosition().getColumn() + direction);

            if (attackedPosition.isValid()) {
                ChessPiece attackedPiece = getField(attackedPosition).getChessPiece();

                if(attackedPiece.getColor() == opposingColor && chessPiece.getMovesMade() == 1 && chessPiece.getPiece() == ChessPiece.Piece.PAWN) {
                    return opposingColor == BLACK && attackedPosition.getRow() == 3 || opposingColor == ChessPiece.Color.WHITE && attackedPosition.getRow() == 4;
                }
            }

            return false;
        }

        /**
         * List of moves that can be made from this field by a rook not taking king safety into account
         * @return list mentioned
         */
        private List<Move> generateBishopMoves() {
            List<Move> possibleMoves = new ArrayList<>();

            possibleMoves.addAll(checkDiagonals(8,0));
            possibleMoves.addAll(checkDiagonals(8, 1));
            possibleMoves.addAll(checkDiagonals(8, 2));
            possibleMoves.addAll(checkDiagonals(8, 3));

            return possibleMoves;
        }

        /** List of moves that can be made from this field by a knight not taking king safety into account
         * @return list mentioned
         */
        private List<Move> generateKnightMoves() {
            List<Move> possibleMoves = new ArrayList<>();
            Position proposedPosition;

            for (int row = -2; row < 3; row++) {
                for (int column = -2; column < 3; column++) {
                    if(Math.abs(row) + Math.abs(column) == 3) {
                        proposedPosition = new Position(getPosition().getRow() + row, getPosition().getColumn() + column);

                        if (proposedPosition.isValid() && getField(proposedPosition).getChessPiece().getColor() != chessPiece.getColor()) {
                            possibleMoves.add(new Move(proposedPosition, getField(getPosition())));
                        }
                    }
                }
            }

            return possibleMoves;
        }

        /** List of moves that can be made from this field by a rook not taking king safety into account
         * @return list mentioned
         */
        private List<Move> generateRookMoves() {
            List<Move> possibleMoves = new ArrayList<>();
            possibleMoves.addAll(checkRows(8, 1));
            possibleMoves.addAll(checkRows(8, -1));
            possibleMoves.addAll(checkColumns(8, 1));
            possibleMoves.addAll(checkColumns(8, -1));

            return possibleMoves;
        }

        /** List of moves that can be made from this field by a queen not taking king safety into account
         * @return list mentioned
         */
        private List<Move> generateQueenMoves() {
            List<Move> possibleMoves = new ArrayList<>();

            possibleMoves.addAll(generateBishopMoves());
            possibleMoves.addAll(generateRookMoves());

            return possibleMoves;
        }

        /** List of moves that can be made from this field by a king not taking its safety into account
         * @return list mentioned above
         */
        private List<Move> generateKingMoves() {
            List<Move> possibleMoves = new ArrayList<>();

            for (int row = -1; row < 2; row++) {
                for (int column = -1; column < 2; column++) {
                    if (row != 0 || column != 0) {
                        Position proposedPosition = new Position(getPosition().getRow() + row, getPosition().getColumn() + column);

                        if(proposedPosition.isValid() && getField(proposedPosition).getChessPiece().getColor() != chessPiece.getColor()) {
                            possibleMoves.add(new Move(proposedPosition, getField(getPosition())));
                        }
                    }
                }
            }

            if (canCastle(-1)) {
                Position movePosition = new Position(getPosition().getRow(), 2);

                possibleMoves.add(new Move(movePosition,this, ChessPiece.SpecialMove.CASTLE));
            }

            if(canCastle(1)) {
                Position movePosition = new Position(getPosition().getRow(), 6);

                possibleMoves.add(new Move(movePosition, this, ChessPiece.SpecialMove.CASTLE));
            }

            return possibleMoves;
        }

        /**
         * Checks whether king can castle in specified direction
         * @param direction Direction of the castle(-1 to the left, 1 - to the right)
         * @return whether king can castle in the specified direction
         */
        private boolean canCastle(int direction) {
            int row = chessPiece.getColor() == ChessPiece.Color.WHITE ? 7 : 0;
            int startingColumn = direction == -1 ? 1 : 5;
            int endingColumn = direction == -1 ? 3 : 6;
            ChessPiece rook = (direction == -1 ? getField(row, 0) : getField(row, 7)).getChessPiece();
            ChessPiece.Color opposingColor = ChessPiece.Color.getOpposingColor(chessPiece.getColor());

            if(getChessPiece().getMovesMade() != 0) return false;
            if(!rook.isOfType(chessPiece.getColor(), ChessPiece.Piece.ROOK)) return false;
            if(rook.getMovesMade() != 0) return false;

            for (int column = startingColumn; column <= endingColumn; column++) {
                Field currentField = getField(row, column);

                if (currentField.getChessPiece().getColor() != ChessPiece.Color.NONE || currentField.getPosition().isAttacked(opposingColor)) {
                    return false;
                }
            }

            return true;
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

            private boolean isAttacked(ChessPiece.Color color) {
                return isAttackedByPawn(color) || isAttackedByKing(color) || isAttackedByPiece(color, ChessPiece.Piece.BISHOP)
                   ||  isAttackedByPiece(color, ChessPiece.Piece.KNIGHT) || isAttackedByPiece(color, ChessPiece.Piece.ROOK)
                   ||  isAttackedByPiece(color, ChessPiece.Piece.QUEEN);
            }

            /**
             * Checks whether this position is attacked by a pawn of specified color
             * @param color Color of attacking piece
             * @return whether this position is attacked by a pawn of specified color
             */
            private boolean isAttackedByPawn(ChessPiece.Color color) {
                for(int direction = 0; direction < 4 ; direction++) {
                    if(isAttackedDiagonally(color, direction)) {
                        return true;
                    }
                }

                return false;
            }

            /**
             * Checks whether this position is attacked by a piece of color diagonally
             * @param color Color of attacking pawn
             * @param direction Which diagonal to check(0 - upper right, 1 - lower right, 2 - upper right, 3 - lower right)
             * @return whether this position is attacked by a pawn of specified color in specified direction
             */
            private boolean isAttackedDiagonally(ChessPiece.Color color, int direction) {
                int rowDirection = direction % 2 == 0 ? 1 : -1;
                int columnDirection = direction / 2 == 0  ? 1 : -1;

                Position checkedPosition = new Position(getRow() + rowDirection, getColumn() + columnDirection);

                if(checkedPosition.isValid()) {
                    ChessPiece attackedPiece = getField(checkedPosition).getChessPiece();

                    return attackedPiece.isOfType(color, ChessPiece.Piece.PAWN);
                }

                return false;
            }

            /**
             * Checks whether this position is attacked by a king of specified color
             * @param color Color of attacking king
             * @return whether this position is attacked by a king of specified color
             */
            private boolean isAttackedByKing(ChessPiece.Color color) {
                for (int row = -1; row < 2; row++) {
                    for (int column = -1; column < 2; column++) {
                        if (row != 0 || column != 0) {
                            Position checkedPosition = new Position(getRow() + row, getColumn() + column);

                            if (checkedPosition.isValid()) {
                                ChessPiece attackedPiece = getField(checkedPosition).getChessPiece();

                                if (attackedPiece.isOfType(color, ChessPiece.Piece.KING)) {
                                    return true;
                                }
                            }
                        }
                    }
                }

                return false;
            }

            /**
             * Checks whether this position is attacked by a piece of specified color and piece type
             * @param color Color of attacking piece
             * @param piece Type of attacking piece
             * @return whether this position is attacked by a specified chess piece
             */
            private boolean isAttackedByPiece(ChessPiece.Color color, ChessPiece.Piece piece) {
                List<Move> possibleMoves;

                switch(piece) {
                    case KNIGHT:
                        possibleMoves = getField(this).generateKnightMoves();
                        break;
                    case BISHOP:
                        possibleMoves = getField(this).generateBishopMoves();
                        break;
                    case ROOK:
                        possibleMoves = getField(this).generateRookMoves();
                        break;
                    case QUEEN:
                        possibleMoves = getField(this).generateQueenMoves();
                        break;
                    default:
                        possibleMoves = new ArrayList<>();
                }

                for (Move move : possibleMoves) {
                    ChessPiece attackedPiece = getField(move.getPosition()).getChessPiece();

                    if (attackedPiece.isOfType(color, piece)) {
                        return true;
                    }
                }

                return false;
            }
        }

        class Move {
            private final Field from;
            private final Position position;
            private ChessPiece.SpecialMove specialMove;
            private int guessedValue;

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

            public int getGuessedValue() {
                return guessedValue;
            }

            public void setGuessedValue(int guessedValue) {
                this.guessedValue = guessedValue;
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

        private boolean isOfType(Color color, Piece piece) {
            return this.color == color && this.piece == piece;
        }

        /**
         * Type of figure on the board
         */
        enum Piece {
            NONE("NoName", 0),
            PAWN("Pawn", 100),
            ROOK("Rook", 500),
            KNIGHT("Knight", 320),
            BISHOP("Bishop", 330),
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
