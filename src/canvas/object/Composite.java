package canvas.object;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import canvas.CanvasElement;

public class Composite extends CanvasElement {
    private final List<CanvasElement> members = new ArrayList<>();

    public Composite(List<CanvasElement> shapes, int depth) {
        super(0, 0, 0, 0, depth);
        members.addAll(shapes);
        updateBounds();
    }

    public List<CanvasElement> getMembers() {
        return new ArrayList<>(members);
    }

    public void addMember(CanvasElement shape) {
        members.add(shape);
        updateBounds();
    }

    @Override
    public void collectMovedShapes(java.util.Set<CanvasElement> out) {
        for (CanvasElement member : members) {
            member.collectMovedShapes(out);
        }
    }

    private void updateBounds() {
        if (members.isEmpty()) {
            return;
        }

        List<CanvasElement> boundTargets = members.stream()
            .filter(CanvasElement::affectsBoundingBox)
                .toList();

        if (boundTargets.isEmpty()) {
            boundTargets = members;
        }

        x1 = boundTargets.stream().mapToInt(CanvasElement::getLeft).min().orElse(0);
        y1 = boundTargets.stream().mapToInt(CanvasElement::getTop).min().orElse(0);
        x2 = boundTargets.stream().mapToInt(CanvasElement::getRight).max().orElse(0);
        y2 = boundTargets.stream().mapToInt(CanvasElement::getBottom).max().orElse(0);
    }

    @Override
    public void draw(Graphics g) {
        members.stream()
               .sorted(Comparator.comparingInt(CanvasElement::getDepth).reversed())
               .forEach(shape -> shape.draw(g));

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
        return members.stream().anyMatch(shape -> shape.isInside(x, y));
    }

    @Override
    public void move(int dx, int dy) {
        for (CanvasElement shape : members) {
            shape.move(dx, dy);
        }
        updateBounds();
    }
}