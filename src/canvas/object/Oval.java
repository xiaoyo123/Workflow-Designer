package canvas.object;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Oval extends BasicObject {
    public Oval(int x, int y, int depth) {
        super(x, y, x + 100, y + 60, depth);
    }

    public Oval(int x1, int y1, int x2, int y2, int depth) {
        super(x1, y1, x2, y2, depth);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(labelColor);
        g.fillOval(getLeft(), getTop(), getWidth(), getHeight());

        g.setColor(Color.BLACK);
        g.drawOval(getLeft(), getTop(), getWidth(), getHeight());

        drawCenteredLabel(g);
        drawPorts(g);
    }

    @Override
    public boolean isInside(int x, int y) {
        if (getWidth() == 0 || getHeight() == 0) {
            return false;
        }

        double rx = getWidth() / 2.0;
        double ry = getHeight() / 2.0;
        double cx = getCenterX();
        double cy = getCenterY();
        double normalized = Math.pow((x - cx) / rx, 2) + Math.pow((y - cy) / ry, 2);
        return normalized <= 1.0;
    }

    @Override
    public List<Point> getPorts() {
        List<Point> ports = new ArrayList<>();
        ports.add(new Point(getCenterX(), getTop()));
        ports.add(new Point(getRight(), getCenterY()));
        ports.add(new Point(getCenterX(), getBottom()));
        ports.add(new Point(getLeft(), getCenterY()));
        return ports;
    }
}
