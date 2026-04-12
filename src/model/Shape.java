package model;

import java.awt.*;
import java.util.Set;

public abstract class Shape {
    protected int x1, x2, y1, y2;
    protected int depth;

    public Shape(int x1, int y1, int x2, int y2, int depth){
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.depth = depth;
    }

    public abstract void draw(Graphics g);
    public abstract boolean isInside(int x, int y);

    public int getX1() { return x1; }
    public int getY1() { return y1; }
    public int getX2() { return x2; }
    public int getY2() { return y2; }

    public int getDepth(){ return this.depth; }
    public void setDepth(int depth){ this.depth = depth;}

    public int getLeft() { return Math.min(x1, x2); }
    public int getTop() { return Math.min(y1, y2); }
    public int getRight() { return Math.max(x1, x2); }
    public int getBottom() { return Math.max(y1, y2); }
    public int getWidth() { return Math.abs(x2 - x1); }
    public int getHeight() { return Math.abs(y2 - y1); }
    public int getCenterX() { return (x1 + x2) / 2; }
    public int getCenterY() { return (y1 + y2) / 2; }

    public void collectMovedShapes(Set<Shape> out) {
        out.add(this);
    }

    public boolean shouldDeleteWith(Set<Shape> selectedShapes) {
        return false;
    }
}
