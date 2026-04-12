package model.link;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;

import model.Shape;
import model.object.Linkable;

public abstract class Link extends Shape {
    private final Shape startShape;
    private final Shape endShape;
    private ConnectPoint startConnectPoint;
    private ConnectPoint endConnectPoint;
    private int startPortIndex = -1;
    private int endPortIndex = -1;

    public Link(Shape startShape, Point startPoint, Shape endShape, Point endPoint, int depth) {
        super(startPoint.x, startPoint.y, endPoint.x, endPoint.y, depth);
        this.startShape = startShape;
        this.endShape = endShape;
        this.startConnectPoint = new ConnectPoint(startPoint);
        this.endConnectPoint = new ConnectPoint(endPoint);
    }

    @Override
    public abstract void draw(Graphics g);

    @Override
    public boolean isInside(int x, int y) {
        Point start = getStartPoint();
        Point end = getEndPoint();
        return distanceToSegment(x, y, start.x, start.y, end.x, end.y) <= 5;
    }

    protected Graphics2D prepareGraphics(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2f));
        return g2;
    }

    protected int distanceToSegment(int px, int py, int x1, int y1, int x2, int y2) {
        double lengthSquared = Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2);
        if (lengthSquared == 0) {
            return (int) Math.hypot(px - x1, py - y1);
        }
        double t = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / lengthSquared;
        t = Math.max(0, Math.min(1, t));
        double projX = x1 + t * (x2 - x1);
        double projY = y1 + t * (y2 - y1);
        return (int) Math.hypot(px - projX, py - projY);
    }

    public Shape getStartShape() {
        return startShape;
    }

    public Shape getEndShape() {
        return endShape;
    }

    public void setPortBinding(int startPortIndex, int endPortIndex) {
        this.startPortIndex = startPortIndex;
        this.endPortIndex = endPortIndex;
        syncConnectPoints();
    }

    private void syncConnectPoints() {
        if (startShape instanceof Linkable startLinkable && startPortIndex >= 0) {
            List<Point> ports = startLinkable.getPorts();
            if (startPortIndex < ports.size()) {
                startConnectPoint.setPoint(ports.get(startPortIndex));
            }
        }

        if (endShape instanceof Linkable endLinkable && endPortIndex >= 0) {
            List<Point> ports = endLinkable.getPorts();
            if (endPortIndex < ports.size()) {
                endConnectPoint.setPoint(ports.get(endPortIndex));
            }
        }
    }

    @Override
    public boolean shouldDeleteWith(java.util.Set<Shape> selectedShapes) {
        return selectedShapes.contains(startShape) || selectedShapes.contains(endShape);
    }

    public Point getStartPoint() {
        syncConnectPoints();
        return startConnectPoint.getPoint();
    }

    public Point getEndPoint() {
        syncConnectPoints();
        return endConnectPoint.getPoint();
    }

    @Override
    public int getX1() {
        return getStartPoint().x;
    }

    @Override
    public int getY1() {
        return getStartPoint().y;
    }

    @Override
    public int getX2() {
        return getEndPoint().x;
    }

    @Override
    public int getY2() {
        return getEndPoint().y;
    }

    @Override
    public int getLeft() {
        return Math.min(getX1(), getX2());
    }

    @Override
    public int getTop() {
        return Math.min(getY1(), getY2());
    }

    @Override
    public int getRight() {
        return Math.max(getX1(), getX2());
    }

    @Override
    public int getBottom() {
        return Math.max(getY1(), getY2());
    }

    @Override
    public int getWidth() {
        return Math.abs(getX2() - getX1());
    }

    @Override
    public int getHeight() {
        return Math.abs(getY2() - getY1());
    }

    @Override
    public int getCenterX() {
        return (getX1() + getX2()) / 2;
    }

    @Override
    public int getCenterY() {
        return (getY1() + getY2()) / 2;
    }
}