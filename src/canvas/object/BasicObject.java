package canvas.object;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import canvas.CanvasElement;

public abstract class BasicObject extends CanvasElement {
    protected String labelName = "";
    protected Color labelColor = new Color(240, 240, 240);

    public BasicObject(int x1, int y1, int x2, int y2, int depth) {
        super(x1, y1, x2, y2, depth);
    }

    protected void drawPorts(Graphics g) {
        if (!isSelected()) return;

        g.setColor(Color.BLACK);
        for (Point port : getPorts()) {
            g.fillRect(port.x - 3, port.y - 3, 6, 6);
        }
    }

    protected void drawCenteredLabel(Graphics g) {
        if (labelName == null || labelName.isEmpty()) {
            return;
        }

        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(labelName);
        int textX = getCenterX() - textWidth / 2;
        int textY = getCenterY() + (fm.getAscent() - fm.getDescent()) / 2;
        g.setColor(Color.BLACK);
        g.drawString(labelName, textX, textY);
    }

    @Override
    public List<Point> getPorts() {
        List<Point> ports = new ArrayList<>();
        int left = getLeft();
        int top = getTop();
        int right = getRight();
        int bottom = getBottom();
        int centerX = getCenterX();
        int centerY = getCenterY();

        ports.add(new Point(left, top));
        ports.add(new Point(centerX, top));
        ports.add(new Point(right, top));
        ports.add(new Point(right, centerY));
        ports.add(new Point(right, bottom));
        ports.add(new Point(centerX, bottom));
        ports.add(new Point(left, bottom));
        ports.add(new Point(left, centerY));
        return ports;
    }

    @Override
    public boolean canResize() {
        return true;
    }

    @Override
    public int getHandleAt(Point p) {
        int left = getLeft();
        int top = getTop();
        int right = getRight();
        int bottom = getBottom();
        int centerX = getCenterX();
        int centerY = getCenterY();
        int tolerance = 10;

        if (isNear(p.x, p.y, left, top, tolerance)) return 0;
        if (isNear(p.x, p.y, centerX, top, tolerance)) return 1;
        if (isNear(p.x, p.y, right, top, tolerance)) return 2;
        if (isNear(p.x, p.y, right, centerY, tolerance)) return 3;
        if (isNear(p.x, p.y, right, bottom, tolerance)) return 4;
        if (isNear(p.x, p.y, centerX, bottom, tolerance)) return 5;
        if (isNear(p.x, p.y, left, bottom, tolerance)) return 6;
        if (isNear(p.x, p.y, left, centerY, tolerance)) return 7;

        return -1;
    }

    private boolean isNear(int x, int y, int targetX, int targetY, int tolerance) {
        return Math.abs(x - targetX) <= tolerance && Math.abs(y - targetY) <= tolerance;
    }

    @Override
    public void setBounds(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName != null ? labelName : "";
    }

    public Color getFillColor() {
        return labelColor;
    }

    public void setFillColor(Color fillColor) {
        this.labelColor = fillColor != null ? fillColor : this.labelColor;
    }
}