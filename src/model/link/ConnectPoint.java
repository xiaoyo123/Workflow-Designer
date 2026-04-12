package model.link;

import java.awt.Point;

public class ConnectPoint {
    private int x;
    private int y;

    public ConnectPoint(Point point) {
        this.x = point != null ? point.x : 0;
        this.y = point != null ? point.y : 0;
    }

    public Point getPoint() {
        return new Point(x, y);
    }

    public void setPoint(Point point) {
        if (point != null) {
            this.x = point.x;
            this.y = point.y;
        }
    }

    public void translate(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }
}