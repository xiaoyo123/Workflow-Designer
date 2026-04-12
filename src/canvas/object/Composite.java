package canvas.object;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import canvas.Element;
import canvas.Groupable;

public class Composite extends Element implements Groupable {
    private final List<Element> members = new ArrayList<>();

    public Composite(List<Element> elements, int depth) {
        super(0, 0, 0, 0, depth);
        members.addAll(elements);
        updateBounds();
    }

    public List<Element> getMembers() {
        return new ArrayList<>(members);
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    public void addMember(Element element) {
        members.add(element);
        updateBounds();
    }

    @Override
    public void collectMovedElements(java.util.Set<Element> out) {
        for (Element member : members) {
            member.collectMovedElements(out);
        }
    }

    private void updateBounds() {
        if (members.isEmpty()) {
            return;
        }

        List<Element> boundTargets = members.stream().toList();

        if (boundTargets.isEmpty()) {
            boundTargets = members;
        }

        x1 = boundTargets.stream().mapToInt(Element::getLeft).min().orElse(0);
        y1 = boundTargets.stream().mapToInt(Element::getTop).min().orElse(0);
        x2 = boundTargets.stream().mapToInt(Element::getRight).max().orElse(0);
        y2 = boundTargets.stream().mapToInt(Element::getBottom).max().orElse(0);
    }

    @Override
    public void draw(Graphics g) {
        members.stream()
               .sorted(Comparator.comparingInt(Element::getDepth).reversed())
             .forEach(element -> element.draw(g));

        if (isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Color.BLACK);
            float[] dash = {6f, 4f};
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));

            int padding = 6;
            g2.drawRect(getLeft() - padding, getTop() - padding, getWidth() + padding * 2, getHeight() + padding * 2);
            g2.dispose();
        }
    }

    @Override
    public boolean isInside(int x, int y) {
        return members.stream().anyMatch(element -> element.isInside(x, y));
    }

    @Override
    public void move(int dx, int dy) {
        for (Element element : members) {
            element.move(dx, dy);
        }
        updateBounds();
    }

    @Override
    public List<Element> getGroupMembers() {
        return new ArrayList<>(members);
    }
}