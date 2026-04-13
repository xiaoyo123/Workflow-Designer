package element.object;

import element.*;
import java.awt.*;
import java.util.List;

public abstract class BasicObject extends Element
        implements isBasicObject {

    protected int width, height;
    protected String labelName = "";
    protected Color  fillColor = new Color(240, 240, 240);
    protected List<Port> ports;

    private static final int MIN_SIZE = 20;

    public BasicObject(int x1, int y1, int x2, int y2, int depth) {
        super(Math.min(x1, x2), Math.min(y1, y2), depth);
        this.width  = Math.max(Math.abs(x2 - x1), MIN_SIZE);
        this.height = Math.max(Math.abs(y2 - y1), MIN_SIZE);
        this.ports  = initPorts();
    }

    // 由子類別定義各自的 port 位置（存相對偏移量）
    protected abstract List<Port> initPorts();

    // ── click port to handle connect and resize ──

    @Override
    public Port getPortAt(int mx, int my) {
        return ports.stream()
            .filter(p -> p.isInRange(mx, my))
            .findFirst()
            .orElse(null);
    }

    // ── Resizable ──

    @Override
    public void setBounds(int x1, int y1, int x2, int y2) {
        this.x      = Math.min(x1, x2);
        this.y      = Math.min(y1, y2);
        this.width  = Math.max(Math.abs(x2 - x1), MIN_SIZE);
        this.height = Math.max(Math.abs(y2 - y1), MIN_SIZE);

        // Keep existing Port objects so links that reference them remain attached.
        List<Port> updatedPorts = initPorts();
        if (ports == null || ports.size() != updatedPorts.size()) {
            this.ports = updatedPorts;
            return;
        }

        for (int i = 0; i < ports.size(); i++) {
            Port current = ports.get(i);
            Port updated = updatedPorts.get(i);
            current.setOffset(updated.getOffsetX(), updated.getOffsetY());
        }
    }

    // ── Selectable ──

    @Override
    public boolean isSelectable() { return true; }

    @Override
    public boolean isContainedIn(int left, int top, int right, int bottom) {
        return x >= left && y >= top &&
               x + width  <= right &&
               y + height <= bottom;
    }

    // ── Labelable ──

    @Override
    public String getLabelName()             { return labelName; }
    @Override
    public void setLabelName(String name)    { this.labelName = name != null ? name : ""; }
    @Override
    public Color getFillColor()              { return fillColor; }
    @Override
    public void setFillColor(Color color)    { this.fillColor = color != null ? color : this.fillColor; }


    // ── 共用繪製 ──

    protected void drawPorts(Graphics g) {
        if (!isSelected() && !isHovered()) return;
        g.setColor(Color.BLACK);
        int size = Port.VISUAL_SIZE;
        int half = size / 2;
        for (Port p : ports) {
            g.fillRect(p.getX() - half, p.getY() - half, size, size);
        }
    }

    protected void drawCenteredLabel(Graphics g) {
        if (labelName == null || labelName.isEmpty()) return;
        FontMetrics fm = g.getFontMetrics();
        int tx = x + (width  - fm.stringWidth(labelName)) / 2;
        int ty = y + (height + fm.getAscent() - fm.getDescent()) / 2;
        g.setColor(Color.BLACK);
        g.drawString(labelName, tx, ty);
    }

    // ── Element abstract ──


    // getters
    public int getWidth()  { return width; }
    public int getHeight() { return height; }
    public int getRight()  { return x + width; }
    public int getBottom() { return y + height; }
    public int getCenterX(){ return x + width  / 2; }
    public int getCenterY(){ return y + height / 2; }
}