package element.object;

import java.awt.Graphics;

import element.Port;

import java.awt.Color;

public class Oval extends BasicObject {
    public Oval(int x, int y, int depth) {
        super(x, y, x + 100, y + 60, depth);
    }

    public Oval(int x1, int y1, int x2, int y2, int depth) {
        super(x1, y1, x2, y2, depth);
    }

    @Override
    protected void initializePorts() {
        if (!ports.isEmpty()) {
            return;
        }

        for (int index = 0; index < 4; index++) {
            ports.add(new Port(this, 0, 0));
        }
        refreshPorts();
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(fillColor);
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
    protected void refreshPorts() {
        if (ports.size() < 4) {
            initializePorts();
            return;
        }

        // 直接取得外框與中心座標
        int left = getLeft();
        int top = getTop();
        int right = getRight();
        int bottom = getBottom();
        int centerX = getCenterX();
        int centerY = getCenterY();

        // 依序定義 Oval 的四個 Port: top, right, bottom, left
        int[][] coordinates = {
            {centerX, top},    // 0: top
            {right, centerY},  // 1: right
            {centerX, bottom}, // 2: bottom
            {left, centerY}    // 3: left
        };

        // 使用迴圈一次更新所有 Port 的座標
        for (int i = 0; i < coordinates.length; i++) {
            ports.get(i).setX(coordinates[i][0]);
            ports.get(i).setY(coordinates[i][1]);
        }
    }

    @Override
    public int getPortIndex(Port port) {
        // Map oval's 4 ports (top, right, bottom, left) to rect's handle indices
        // Oval: 0=top, 1=right, 2=bottom, 3=left
        // Rect: 0=top-left, 1=top-center, 2=top-right, 3=right-center, 4=bottom-right, 5=bottom-center, 6=bottom-left, 7=left-center
        // Map to: 1=top-center, 3=right-center, 5=bottom-center, 7=left-center
        int ovalIndex = ports.indexOf(port);
        if (ovalIndex == -1) return -1;
        return 1 + (ovalIndex * 2); // 0→1, 1→3, 2→5, 3→7
    }
}
