package chess;

import javax.swing.*;
import java.awt.*;

public class DifficultyCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        ComputerAdversaryFactory.Difficulty difficulty = (ComputerAdversaryFactory.Difficulty) value;

        component.setBackground(difficulty.getColor());
        component.setForeground(Colors.IVORY.getColor());

        return component;
    }
}
