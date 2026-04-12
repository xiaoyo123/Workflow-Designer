package element;

import java.awt.Point;

public class Port {
    private final Element owner;
    private int x;
    private int y;

    public Port(int x, int y) {
        this.owner = null;
        this.x = x;
        this.y = y;
    }

    public Port(Point point) {
        this.owner = null;
        this.x = point != null ? point.x : 0;
        this.y = point != null ? point.y : 0;
    }

    public Port(Element owner, int x, int y) {
        this.owner = owner;
        this.x = x;
        this.y = y;
    }

    public Port(Element owner, Point point) {
        this.owner = owner;
        this.x = point != null ? point.x : 0;
        this.y = point != null ? point.y : 0;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Element getOwner() {
        return owner;
    }

    public void setPoint(Point point) {
        if (point != null) {
            setX(point.x);
            setY(point.y);
        }
    }

    public void move(int dx, int dy) {
        setX(getX() + dx);
        setY(getY() + dy);
    }

    public Point getPoint() {
        return new Point(getX(), getY());
    }
}
