package canvas.object;

import java.awt.Graphics;
import java.awt.Color;

public class Rect extends BasicObject {
    public Rect(int x, int y, int depth) {
        super(x, y, x + 100, y + 100, depth);
    }

    public Rect(int x1, int y1, int x2, int y2, int depth) {
        super(x1, y1, x2, y2, depth);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(fillColor);
        g.fillRect(getLeft(), getTop(), getWidth(), getHeight());

        g.setColor(Color.BLACK);
        g.drawRect(getLeft(), getTop(), getWidth(), getHeight());

        drawCenteredLabel(g);

        drawPorts(g);
    }

    @Override
    public boolean isInside(int x, int y) {
        return x >= getLeft() && x <= getRight() &&
               y >= getTop() && y <= getBottom();
    }
}
