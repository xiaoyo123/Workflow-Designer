package canvas.link;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import canvas.Element;
import canvas.Port;

public abstract class BasicLink extends Element {
    private final Port startPort;
    private final Port endPort;

    public BasicLink(Port startPort, Port endPort, int depth) {
        super(startPort != null ? startPort.getX() : 0,
              startPort != null ? startPort.getY() : 0,
              endPort != null ? endPort.getX() : 0,
              endPort != null ? endPort.getY() : 0,
              depth);
        this.startPort = startPort != null ? startPort : new Port(0, 0);
        this.endPort = endPort != null ? endPort : new Port(0, 0);
    }

    @Override
    public abstract void draw(Graphics g);

    protected Graphics2D prepareGraphics(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2f));
        return g2;
    }

    public Port getStartPort() {
        return startPort;
    }

    public Port getEndPort() {
        return endPort;
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public int getX1() {
        return getStartPort().getX();
    }

    @Override
    public int getY1() {
        return getStartPort().getY();
    }

    @Override
    public int getX2() {
        return getEndPort().getX();
    }

    @Override
    public int getY2() {
        return getEndPort().getY();
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