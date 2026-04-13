package element;

import java.awt.*;
import java.util.Set;

public abstract class Element {
    protected int x, y;
    protected int depth;
    protected boolean isSelected = false;
    protected boolean isHovered  = false;

    public Element(int x, int y, int depth) {
        this.x     = x;
        this.y     = y;
        this.depth = depth;
    }

    public abstract void draw(Graphics g);
    public abstract boolean contains(int x, int y);
    public abstract boolean isContainedIn(int left, int top, int right, int bottom);

    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    public void collectMovedElements(Set<Element> out) {
        out.add(this);
    }

    public boolean isSelectable()              { return false; }
    public boolean isComposite()               { return false; }

    public void setSelected(boolean selected)  { this.isSelected = selected; }
    public boolean isSelected()                { return isSelected; }
    public void setHovered(boolean hovered)    { this.isHovered = hovered; }
    public boolean isHovered()                 { return isHovered; }
    public int getX()                          { return x; }
    public int getY()                          { return y; }
    public int getDepth()                      { return depth; }
    public void setDepth(int depth)            { this.depth = depth; }
}