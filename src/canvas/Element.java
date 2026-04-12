package canvas;

import java.awt.*;
import java.util.Set;

public abstract class Element {
    protected int x1, x2, y1, y2;
    protected int depth;
    protected boolean isSelected = false;

    public Element(int x1, int y1, int x2, int y2, int depth){
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.depth = depth;
    }

    public abstract void draw(Graphics g);

    public boolean isInside(int x, int y) {
        return false;
    }

    
    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }
    
    public boolean isSelected() {
        return isSelected;
    }
    
    public void move(int dx, int dy) {
        this.x1 += dx;
        this.y1 += dy;
        this.x2 += dx;
        this.y2 += dy;
    }

    public boolean canResize() {
        return false;
    }

    public int getHandleAt(Point p) {
        return -1;
    }

    public void setBounds(int x1, int y1, int x2, int y2) {
        // Default: shape does not support direct resize.
    }

    public Port getPortAt(int x, int y) {
        return null;
    }

    public int getPortIndex(Port port) {
        return -1;
    }

    public boolean isSelectable() {
        return false;
    }

    public void collectMovedElements(Set<Element> out) {
        out.add(this);
    }


    public int getLeft() { return Math.min(x1, x2); }
    public int getTop() { return Math.min(y1, y2); }
    public int getRight() { return Math.max(x1, x2); }
    public int getBottom() { return Math.max(y1, y2); }
    public int getWidth() { return Math.abs(x2 - x1); }
    public int getHeight() { return Math.abs(y2 - y1); }
    public int getCenterX() { return (x1 + x2) / 2; }
    public int getCenterY() { return (y1 + y2) / 2; }
    public int getX1() { return x1; }
    public int getY1() { return y1; }
    public int getX2() { return x2; }
    public int getY2() { return y2; }

    public int getDepth(){ return this.depth; }
    public void setDepth(int depth){ this.depth = depth;}
}
