package chess;

import java.awt.*;

public enum Colors {
    BROWN(new Color(82, 43, 41)),
    IVORY(new Color(252, 255, 235)),
    PINK(new Color(255, 173, 173)),
    SCARLET(new Color(102, 0, 0));

    private Color color;

    Colors(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
