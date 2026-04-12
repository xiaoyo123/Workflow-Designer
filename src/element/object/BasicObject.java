package element.object;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import element.Connectable;
import element.Element;
import element.Labelable;
import element.Port;
import element.Resizable;

public abstract class BasicObject extends Element implements Connectable, Labelable, Resizable {
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

        // 將 8 個方位座標依序存入二維陣列中
        int[][] coordinates = {
            {left, top},       // 0: top-left
            {centerX, top},    // 1: top-center
            {right, top},      // 2: top-right
            {right, centerY},  // 3: right-center
            {right, bottom},   // 4: bottom-right
            {centerX, bottom}, // 5: bottom-center
            {left, bottom},    // 6: bottom-left
            {left, centerY}    // 7: left-center
        };

        // 使用迴圈一次更新所有 Port 的座標
        for (int i = 0; i < coordinates.length; i++) {
            ports.get(i).setX(coordinates[i][0]);
            ports.get(i).setY(coordinates[i][1]);
        }
    }

    protected void drawPorts(Graphics g) {
        if (!isSelected() && !isHovered()) return;

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