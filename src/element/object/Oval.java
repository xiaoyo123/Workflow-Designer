package element.object;

import element.Port;
import java.awt.*;
import java.util.List;

public class Oval extends BasicObject {

    public Oval(int x, int y, int depth) {
        super(x, y, x + 100, y + 60, depth);
    }

    public Oval(int x1, int y1, int x2, int y2, int depth) {
        super(x1, y1, x2, y2, depth);
    }

    @Override
    protected List<Port> initPorts() {
        return List.of(
            new Port(this, width / 2, 0         ), // 上
            new Port(this, width,     height / 2), // 右
            new Port(this, width / 2, height    ), // 下
            new Port(this, 0,         height / 2)  // 左
        );
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(fillColor);
        g.fillOval(x, y, width, height);
        g.setColor(Color.BLACK);
        g.drawOval(x, y, width, height);
        drawCenteredLabel(g);
        drawPorts(g);
    }

    @Override
    public boolean contains(int mx, int my) {
        if (width == 0 || height == 0) return false;
        double dx = (mx - getCenterX()) / (width  / 2.0);
        double dy = (my - getCenterY()) / (height / 2.0);
        return dx * dx + dy * dy <= 1.0;
    }
}