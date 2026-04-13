package element.object;

import element.Port;
import java.awt.*;
import java.util.List;

public class Rect extends BasicObject {

    public Rect(int x, int y, int depth) {
        super(x, y, x + 100, y + 100, depth);
    }

    public Rect(int x1, int y1, int x2, int y2, int depth) {
        super(x1, y1, x2, y2, depth);
    }

    @Override
    protected List<Port> initPorts() {
        return List.of(
            new Port(this, 0,         0         ), // 左上
            new Port(this, width / 2, 0         ), // 上中
            new Port(this, width,     0         ), // 右上
            new Port(this, width,     height / 2), // 右中
            new Port(this, width,     height    ), // 右下
            new Port(this, width / 2, height    ), // 下中
            new Port(this, 0,         height    ), // 左下
            new Port(this, 0,         height / 2)  // 左中
        );
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(fillColor);
        g.fillRect(x, y, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
        drawCenteredLabel(g);
        drawPorts(g);
    }

    @Override
    public boolean contains(int mx, int my) {
        return mx >= x && mx <= x + width &&
               my >= y && my <= y + height;
    }
}