package model.object;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import model.Shape;
import model.link.Link;

public class Composite extends Shape implements Movable {
    private final List<Shape> members = new ArrayList<>();
    private boolean isSelected = false;

    public Composite(List<Shape> shapes, int depth) {
        super(0, 0, 0, 0, depth);
        members.addAll(shapes);
        updateBounds();
    }

    public List<Shape> getMembers() {
        return new ArrayList<>(members);
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void addMember(Shape shape) {
        members.add(shape);
        updateBounds();
    }

    @Override
    public void collectMovedShapes(java.util.Set<Shape> out) {
        for (Shape member : members) {
            member.collectMovedShapes(out);
        }
    }

    private void updateBounds() {
        if (members.isEmpty()) {
            return;
        }

        List<Shape> boundTargets = members.stream()
                .filter(shape -> !(shape instanceof Link))
                .toList();

        if (boundTargets.isEmpty()) {
            boundTargets = members;
        }

        x1 = boundTargets.stream().mapToInt(Shape::getLeft).min().orElse(0);
        y1 = boundTargets.stream().mapToInt(Shape::getTop).min().orElse(0);
        x2 = boundTargets.stream().mapToInt(Shape::getRight).max().orElse(0);
        y2 = boundTargets.stream().mapToInt(Shape::getBottom).max().orElse(0);
    }

    @Override
    public void draw(Graphics g) {
        members.stream()
               .sorted(Comparator.comparingInt(Shape::getDepth).reversed())
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
        for (Shape shape : members) {
            if (shape instanceof Object object) {
                object.move(dx, dy);
            } else if (shape instanceof Composite composite) {
                composite.move(dx, dy);
            }
        }
        updateBounds();
    }
}