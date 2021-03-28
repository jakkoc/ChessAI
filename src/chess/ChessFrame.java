package chess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.swing.BoxLayout.Y_AXIS;

public class ChessFrame extends JFrame implements MouseListener, MouseMotionListener {
    private JPanel chessPanel;
    private JPanel[][] fields;
    private JLayeredPane layeredPane;
    private JLabel chessPiece;
    private SoundEffect soundEffect;
    private final ChessBoard board;
    private ChessBoard.ChessPiece movedPiece;
    private ChessBoard.Field.Position movedPosition;
    private java.util.List<ChessBoard.Field.Move> validMoves;
    private int xAdjustment;
    private int yAdjustment;

    public ChessFrame(ChessBoard board) {
        this.board = board;
        configureFrame();
        configureChessPanel();
        configureLayeredPane();
        configureSoundEffect();
        drawChessBoardWithPieces();
        pack();
        setLocation();
    }

    private void configureFrame() {
        setResizable(false);
        setTitle("Chess");
        setIconImage(new ImageIcon("Images/icon.png").getImage());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void configureLayeredPane() {
        layeredPane = new JLayeredPane();
        getContentPane().add(layeredPane);
        layeredPane.setPreferredSize(getBoardSize());
        layeredPane.addMouseListener(this);
        layeredPane.addMouseMotionListener(this);
        layeredPane.add(chessPanel, JLayeredPane.DEFAULT_LAYER);
    }

    private Dimension getBoardSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new Dimension((int) ((3 * screenSize.getHeight() / 5)), (int) (3 * screenSize.getHeight() / 5));
    }

    private void configureChessPanel() {
        chessPanel = new JPanel();
        chessPanel.setLayout(new GridLayout(8, 8));

        Dimension boardSize = getBoardSize();

        chessPanel.setPreferredSize(boardSize);
        chessPanel.setBounds(0, 0, boardSize.width, boardSize.height);
    }

    private void drawChessBoardWithPieces() {
        fields = new JPanel[8][8];

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                createField(row, column);
                setFieldBackground(row, column);
                drawPiece(row, column);
            }
        }
    }

    private void createField(int row, int column) {
        fields[row][column] = new JPanel(new BorderLayout());
        chessPanel.add(fields[row][column]);
    }

    private void setFieldBackground(int row, int column) {
        if (row % 2 == 0) {
            fields[row][column].setBackground(column % 2 == 0 ? Colors.IVORY.getColor() : Colors.BROWN.getColor());
        } else {
            fields[row][column].setBackground(column % 2 == 0 ? Colors.BROWN.getColor() : Colors.IVORY.getColor());
        }
    }

    private void drawPiece(int row, int column) {
        String imagePath = board.getField(row, column).getChessPiece().getImagePath();

        if (!imagePath.equals("NoPath")) {
            JLabel label = new JLabel();
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setVerticalAlignment(JLabel.CENTER);
            label.setIcon(scaleImageToIcon(getImageIcon(board.getField(row, column).getChessPiece())));
            fields[row][column].add(label);
        }
    }

    private void configureSoundEffect() {
        soundEffect = new SoundEffect();
        try {
            soundEffect.setFile("Media/Move.wav");
        } catch(Exception e) {
            Logger.getLogger("global").log(Level.FINE,"Media/Move.wav could not be played", e);
            soundEffect = null;
        }
    }

    private void setLocation() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int) (screenSize.getWidth() - getWidth()) / 2, (int) (screenSize.getHeight() - getHeight()) / 2);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        chessPiece = null;
        Component component = chessPanel.findComponentAt(e.getPoint());

        if (component instanceof JPanel ||!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }

        if(findField(component.getParent()).getChessPiece().getColor() == ChessBoard.ChessPiece.Color.WHITE) {
            if(soundEffect != null) soundEffect.play();
            chessPiece = (JLabel) component;
            setAdjustments(component, e);
            findAndRemovePiece(component.getParent());
            addResizedToDragLayer(e);
        }
    }

    private void setAdjustments(Component component, MouseEvent e) {
        Point parentLocation = component.getParent().getLocation();
        xAdjustment = parentLocation.x - e.getX();
        yAdjustment = parentLocation.y - e.getY();
    }

    private void addResizedToDragLayer(MouseEvent e) {
        chessPiece.setLocation(e.getX() + xAdjustment, e.getY() + yAdjustment);
        chessPiece.setSize(chessPiece.getWidth(), chessPiece.getHeight());
        layeredPane.add(chessPiece, JLayeredPane.DRAG_LAYER);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (chessPiece == null || !SwingUtilities.isLeftMouseButton(e)) {
            return;
        }

        if(soundEffect != null) soundEffect.play();

        int row = movedPosition.getRow();
        int column = movedPosition.getColumn();

        chessPiece.setVisible(false);

        Component component = findMoveTarget(e);
        Container parent = component instanceof JLabel ? component.getParent() : (Container) component;

        ChessBoard.Field.Move move = findValidMove(parent);

        if (move != null) {
            move(parent, move);
        } else {
            cancelMove(fields[row][column]);
        }

        chessPiece.setVisible(true);
        checkForGameEnding();

        if(board.getColorToMove() == ChessBoard.ChessPiece.Color.BLACK) {
            computerMove();
        }
    }

    private void computerMove() {
        ComputerAdversary adversary = board.getAdversary();
        ChessBoard.Field.Move move= adversary.chooseMove();
        ChessBoard.Field field = move.getFrom();
        movedPosition = field.getPosition();
        movedPiece = new ChessBoard.ChessPiece(field.getChessPiece());

        JPanel startingField = fields[movedPosition.getRow()][movedPosition.getColumn()];
        JPanel endingField = fields[move.getPosition().getRow()][move.getPosition().getColumn()];
        chessPiece = (JLabel)startingField.getComponent(0);

        removePiece(movedPosition.getRow(), movedPosition.getColumn());

        move(endingField, move);
        chessPiece.setVisible(true);
        checkForGameEnding();
    }

    private void move(Container parent, ChessBoard.Field.Move move) {
        capturePiece(move);
        parent.add(chessPiece);
        movedPiece.incrementMovesMade();
        ChessBoard.Field newField = addPieceToBoard(parent);
        newField.setTurnStanding(0);
        board.changeTurn();
    }

    private void cancelMove(JPanel field) {
        field.add(chessPiece);
        addPieceToBoard(field);
    }

    private void checkForGameEnding() {
        ChessBoard.ChessPiece.Color winner = board.getWinner();

        if (winner != ChessBoard.ChessPiece.Color.NONE) {
            setGameEndingSound();
            if(soundEffect != null) soundEffect.play();
            displayVictoryPanel();
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }
    }

    private void setGameEndingSound() {
        try {
            soundEffect = new SoundEffect();
            soundEffect.setFile("Media/Ending.wav");
        } catch(Exception e) {
            Logger.getLogger("global").log(Level.FINE,"Ending.wav could not be played!", e);
            soundEffect = null;
        }
    }

    private void displayVictoryPanel() {
        JPanel panel = new JPanel();
        ChessBoard.ChessPiece.Color winner = board.getWinner();
        boolean isStalemate = board.isStalemate(winner);
        Font font = new Font("Times New Roman",Font.ITALIC, this.getWidth() / 20);
        panel.setPreferredSize(new Dimension(this.getWidth() / 2, (int)(this.getHeight() / 4.5)));
        JTextField text = new JTextField(isStalemate ? "A draw" : winner.getColor() + " player wins!");
        text.setPreferredSize(panel.getPreferredSize());
        text.setVisible(true);
        text.setBackground(Colors.BROWN.getColor());
        text.setForeground(Colors.IVORY.getColor());
        text.setFont(font);
        text.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(text);
        JOptionPane.showConfirmDialog(null, text,isStalemate ? "Stalemate" : "Checkmate", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
    }

    private Component findMoveTarget(MouseEvent e) {
        int row = movedPosition.getRow();
        int column = movedPosition.getColumn();
        Component returnValue = chessPanel.findComponentAt(e.getX(), e.getY());
        if(returnValue == null) returnValue = fields[row][column];
        return returnValue;
    }

    private ChessBoard.Field.Move findValidMove(Container parent) {
        return validMoves.stream().filter(m -> m.getPosition().equals(findField(parent).getPosition())).findAny().orElse(null);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (chessPiece == null) return;
        chessPiece.setLocation(e.getX() + xAdjustment, e.getY() + yAdjustment);
    }

    private void capturePiece(ChessBoard.Field.Move move) {

        switch (move.getSpecialMove()) {
            case CASTLE:
                castle(move);
                break;
            case EN_PASSANT:
                enPassant(move);
                break;
            case PROMOTION:
                promote();
                break;
            default:
        }

        int row = move.getPosition().getRow();
        int column = move.getPosition().getColumn();

        if (fields[row][column].getComponentCount() > 0) {
            removePiece(row, column);
        }
    }

    private void castle(ChessBoard.Field.Move move) {
        int row = move.getPosition().getRow();
        int column = move.getPosition().getColumn();
        int rookColumn = column == 2 ? 0 : 7;
        int newRookColumn = column == 2 ? 3 : 5;
        int movesMade = board.getField(row, rookColumn).getChessPiece().getMovesMade();
        Component rook = removePiece(row, rookColumn);
        fields[row][newRookColumn].add(rook).setVisible(true);
        board.addPiece(row, newRookColumn, new ChessBoard.ChessPiece(board.getColorToMove(), ChessBoard.ChessPiece.Piece.ROOK, movesMade));
    }

    private void enPassant(ChessBoard.Field.Move move) {
        boolean isWhite = board.getColorToMove() == ChessBoard.ChessPiece.Color.WHITE;
        move.getPosition().setRow(move.getPosition().getRow() + (isWhite ? 1 : -1));
    }

    private void promote() {
        ChessBoard.ChessPiece.Piece chosenPromotion = choosePromotionPiece();
        if (chosenPromotion == null) chosenPromotion = ChessBoard.ChessPiece.Piece.BISHOP;
        movedPiece.setPiece(chosenPromotion);
        chessPiece.setIcon(scaleImageToIcon(getImageIcon(movedPiece)));
    }

    private ChessBoard.ChessPiece.Piece choosePromotionPiece() {
        if(board.getColorToMove() == ChessBoard.ChessPiece.Color.WHITE) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, Y_AXIS));
            panel.setSize(this.getWidth() / 3, this.getHeight() / 6);
            ChessBoard.ChessPiece.Piece[] promotionPieces = {ChessBoard.ChessPiece.Piece.BISHOP, ChessBoard.ChessPiece.Piece.KNIGHT, ChessBoard.ChessPiece.Piece.ROOK, ChessBoard.ChessPiece.Piece.QUEEN};
            JList<ChessBoard.ChessPiece.Piece> list = new JList<>(promotionPieces);
            list.setFixedCellWidth(panel.getWidth());
            list.setFixedCellHeight(panel.getHeight() / 4);
            list.setCellRenderer(new ListCellRenderer());
            panel.add(list);
            JOptionPane.showMessageDialog(null, panel, "Promotion", JOptionPane.PLAIN_MESSAGE, null);
            return list.getSelectedValue();
        }

        else return board.getAdversary().choosePromotion();
    }

    private Image getImageIcon(ChessBoard.ChessPiece piece) {
        return new ImageIcon(piece.getImagePath()).getImage();
    }

    private Icon scaleImageToIcon(Image image) {
        int scaledWidth = chessPanel.getWidth() / 10;
        int scaledHeight = chessPanel.getHeight() / 10;
        return new ImageIcon(image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH));
    }

    private ChessBoard.Field addPieceToBoard(Container field) {

        for (int i = 0; i < fields.length; i++) {
            for (int j = 0; j < fields[i].length; j++) {
                if (fields[i][j] == field) {
                    board.addPiece(i, j, movedPiece);
                    hidePossibleMoves();
                    return board.getField(i, j);
                }
            }
        }

        hidePossibleMoves();
        return null;
    }

    private void findAndRemovePiece(Container container) {
        ChessBoard.Field field = findField(container);
        if (field != null) {
            movedPiece = field.getChessPiece();
            movedPosition = field.getPosition();
            showPossibleMoves(field);
            board.removePiece(movedPosition);
        }
    }

    private void showPossibleMoves(ChessBoard.Field field) {
        if (movedPiece == null) {
            return;
        }

        validMoves = field.getValidMoves();

        for (ChessBoard.Field.Move move : validMoves) {
            if (move.getPosition().getRow() % 2 == 0 && move.getPosition().getColumn() % 2 == 0 || move.getPosition().getRow() % 2 == 1 && move.getPosition().getColumn() % 2 == 1) {
                fields[move.getPosition().getRow()][move.getPosition().getColumn()].setBackground(Colors.PINK.getColor());
            } else {
                fields[move.getPosition().getRow()][move.getPosition().getColumn()].setBackground(Colors.SCARLET.getColor());
            }
        }
    }

    private void hidePossibleMoves() {
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {

                if (row % 2 == 0) {
                    fields[row][column].setBackground(column % 2 == 0 ? new Color(252, 255, 235) : new Color(82, 43, 41));
                } else {
                    fields[row][column].setBackground(column % 2 == 0 ? new Color(82, 43, 41) : new Color(252, 255, 235));
                }
            }
        }
    }

    private ChessBoard.Field findField(Container container) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (fields[i][j] == container) {
                    return board.getField(i, j);
                }
            }
        }

        return null;
    }

    private Component removePiece(int row, int column) {
        Component piece = fields[row][column].getComponent(0);
        piece.setVisible(false);
        fields[row][column].remove(0);
        board.removePiece(row, column);

        return piece;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}