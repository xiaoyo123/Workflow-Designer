package element;

import java.awt.*;
import java.util.*;
import java.util.List;

import element.object.BasicObject;

public class Composite extends Element{
    private final List<Element> members;

    public Composite(List<Element> elements, int depth) {
        super(calcLeft(elements), calcTop(elements), depth);
        this.members = new ArrayList<>(elements);
    }

    // ── bounding box 動態計算，不需要維護 x1,y1,x2,y2 ──

    public int getWidth() {
        return calcRight(members) - getX();
    }

    public int getHeight() {
        return calcBottom(members) - getY();
    }

    private static int calcLeft(List<Element> els) {
        return els.stream().mapToInt(Element::getX).min().orElse(0);
    }

    private static int calcTop(List<Element> els) {
        return els.stream().mapToInt(Element::getY).min().orElse(0);
    }

    private static int calcRight(List<Element> els) {
        return els.stream()
            .mapToInt(e -> {
                if (e instanceof BasicObject o) return o.getX() + o.getWidth();
                if (e instanceof Composite   c) return c.getX() + c.getWidth();
                return e.getX();
            })
            .max().orElse(0);
    }

    private static int calcBottom(List<Element> els) {
        return els.stream()
            .mapToInt(e -> {
                if (e instanceof BasicObject o) return o.getY() + o.getHeight();
                if (e instanceof Composite   c) return c.getY() + c.getHeight();
                return e.getY();
            })
            .max().orElse(0);
    }

    // ── move：移動所有 member，自身 x,y 跟著更新 ──

    @Override
    public void move(int dx, int dy) {
        members.forEach(e -> e.move(dx, dy));
        x += dx;
        y += dy;
    }

    // ── collectMovedElements：展開所有子元素 ──

    @Override
    public void collectMovedElements(Set<Element> out) {
        out.add(this);
    }

    // ── contains：只有點到任一成員本體才算命中 ──

    @Override
    public boolean contains(int mx, int my) {
        return members.stream().anyMatch(member -> member.contains(mx, my));
    }

    // ── isContainedIn：框選時判斷整個 bounding box 是否完全在內 ──

    @Override
    public boolean isContainedIn(int left, int top, int right, int bottom) {
        return x >= left && y >= top &&
               x + getWidth()  <= right &&
               y + getHeight() <= bottom;
    }

    // ── draw ──

    @Override
    public void draw(Graphics g) {
        members.stream()
            .sorted(Comparator.comparingInt(Element::getDepth).reversed())
            .forEach(e -> e.draw(g));

        if (isSelected || isHovered) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Color.BLACK);
            float[] dash = {6f, 4f};
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
                         BasicStroke.JOIN_MITER, 10f, dash, 0f));
            int pad = 6;
            g2.drawRect(x - pad, y - pad, getWidth() + pad * 2, getHeight() + pad * 2);
            g2.dispose();
        }
    }

    // ── Selectable ──

    @Override
    public boolean isSelectable()  { return true; }
    @Override
    public boolean isComposite()   { return true; }

    public List<Element> getMembers() {
        return Collections.unmodifiableList(members);
    }
}