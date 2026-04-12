package canvas.object;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import canvas.Element;
import canvas.Port;

public abstract class BasicObject extends Element {
    protected String labelName = "";
    protected Color fillColor = new Color(240, 240, 240);
    protected final List<Port> ports = new ArrayList<>();

    public BasicObject(int x1, int y1, int x2, int y2, int depth) {
        super(x1, y1, x2, y2, depth);
        initializePorts();
    }

    protected void initializePorts() {
        if (!ports.isEmpty()) {
            return;
        }

        for (int index = 0; index < 8; index++) {
            ports.add(new Port(this, 0, 0));
        }
        refreshPorts();
    }

    protected void refreshPorts() {
        if (ports.size() < 8) {
            initializePorts();
            return;
        }

        int left = getLeft();
        int top = getTop();
        int right = getRight();
        int bottom = getBottom();
        int centerX = getCenterX();
        int centerY = getCenterY();

        ports.get(0).setX(left);
        ports.get(0).setY(top);
        ports.get(1).setX(centerX);
        ports.get(1).setY(top);
        ports.get(2).setX(right);
        ports.get(2).setY(top);
        ports.get(3).setX(right);
        ports.get(3).setY(centerY);
        ports.get(4).setX(right);
        ports.get(4).setY(bottom);
        ports.get(5).setX(centerX);
        ports.get(5).setY(bottom);
        ports.get(6).setX(left);
        ports.get(6).setY(bottom);
        ports.get(7).setX(left);
        ports.get(7).setY(centerY);
    }

    protected void drawPorts(Graphics g) {
        if (!isSelected()) return;

        g.setColor(Color.BLACK);
        for (Port port : ports) {
            g.fillRect(port.getX() - 3, port.getY() - 3, 6, 6);
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
    public Port getPortAt(int x, int y) {
        for (Port port : ports) {
            if (Math.abs(port.getX() - x) <= 10 && Math.abs(port.getY() - y) <= 10) {
                return port;
            }
        }
        return null;
    }

    @Override
    public int getPortIndex(Port port) {
        return ports.indexOf(port);
    }

    @Override
    public void move(int dx, int dy) {
        super.move(dx, dy);
        refreshPorts();
    }

    @Override
    public boolean canResize() {
        return true;
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public void setBounds(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        refreshPorts();
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName != null ? labelName : "";
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor != null ? fillColor : this.fillColor;
    }
}