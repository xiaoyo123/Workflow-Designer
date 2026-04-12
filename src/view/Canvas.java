package view;

import javax.swing.*;

import controller.Mode;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Comparator;
import java.util.Set;

import model.Shape;
import model.link.Link;
import model.object.Linkable;
import model.object.Object;
import model.object.Composite;

public class Canvas extends JPanel {
    private Mode currentMode;
    private List<Shape> shapes = new ArrayList<>();
    private List<Shape> selectedShapes = new ArrayList<>();
    private Rectangle marquee = null;
    private Link previewLink = null;

    public Canvas() {
        setBackground(Color.WHITE);

        setupEvents();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        shapes.sort(Comparator.comparingInt(Shape::getDepth).reversed());

        for (Shape shape : shapes) {
            shape.draw(g);
        }

        if (marquee != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(60, 120, 220));
            float[] dash = {4f, 4f};
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
            g2.drawRect(marquee.x, marquee.y, marquee.width, marquee.height);
            g2.dispose();
        }

        if (previewLink != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2f));
            previewLink.draw(g2);
            g2.dispose();
        }
    }

    public void addShape(Shape s) {
        shapes.add(s);
        repaint();
    }

    public List<Shape> getShapes() {
        return new ArrayList<>(shapes);
    }

    public void setMode(Mode mode) {
        this.currentMode = mode;
    }

    public int getFrontDepth() {
        return shapes.stream().mapToInt(Shape::getDepth).min().orElse(0);
    }

    public int getBackDepth() {
        return shapes.stream().mapToInt(Shape::getDepth).max().orElse(0);
    }

    public void bringToFront(Shape shape) {
        if (shape == null) {
            return;
        }

        shape.setDepth(getFrontDepth() - 1);
        repaint();
    }

    public void sendToBack(Shape shape) {
        if (shape == null) {
            return;
        }

        shape.setDepth(getBackDepth() + 1);
        repaint();
    }

    public void unselectAll() {
        for (Shape shape : shapes) {
            if (shape instanceof Object) {
                ((Object) shape).setSelected(false);
            } else if (shape instanceof Composite) {
                ((Composite) shape).setSelected(false);
            }
        }
        selectedShapes.clear();
        repaint();
    }

    public void selectOnly(Shape shape) {
        unselectAll();
        if (shape instanceof Object) {
            ((Object) shape).setSelected(true);
        } else if (shape instanceof Composite) {
            ((Composite) shape).setSelected(true);
        }
        selectedShapes.add(shape);
        repaint();
    }

    public void toggleSelection(Shape shape) {
        if (shape instanceof Object) {
            Object basicObject = (Object) shape;
            boolean selected = !basicObject.isSelected();
            basicObject.setSelected(selected);
            if (selected) {
                selectedShapes.add(shape);
            } else {
                selectedShapes.remove(shape);
            }
        } else if (shape instanceof Composite) {
            Composite composite = (Composite) shape;
            boolean selected = !composite.isSelected();
            composite.setSelected(selected);
            if (selected) {
                selectedShapes.add(shape);
            } else {
                selectedShapes.remove(shape);
            }
        }
        repaint();
    }

    public List<Shape> getSelectedShapes() {
        return new ArrayList<>(selectedShapes);
    }

    public void setMarquee(Rectangle rect) {
        this.marquee = rect;
        repaint();
    }

    public void clearMarquee() {
        this.marquee = null;
        repaint();
    }

    public void selectByRectangle(Rectangle rect, boolean append) {
        if (!append) {
            unselectAll();
        }

        for (Shape shape : shapes) {
            Rectangle bounds = new Rectangle(shape.getLeft(), shape.getTop(), shape.getWidth(), shape.getHeight());
            if (rect.intersects(bounds)) {
                if (shape instanceof Object) {
                    ((Object) shape).setSelected(true);
                } else if (shape instanceof Composite) {
                    ((Composite) shape).setSelected(true);
                }
                if (!selectedShapes.contains(shape)) {
                    selectedShapes.add(shape);
                }
            }
        }
        repaint();
    }

    public void groupSelectedShapes() {
        if (selectedShapes.size() < 2) {
            return;
        }

        List<Shape> grouped = new ArrayList<>(selectedShapes);
        includeInternalLinks(grouped);

        for (Shape shape : grouped) {
            clearSelectionFlag(shape);
        }
        int groupDepth = grouped.stream().mapToInt(Shape::getDepth).min().orElse(0);
        shapes.removeAll(grouped);
        shapes.add(new Composite(grouped, groupDepth));
        unselectAll();
        repaint();
    }

    private void includeInternalLinks(List<Shape> grouped) {
        Set<Shape> groupedSet = new HashSet<>(grouped);
        Set<Shape> groupedMembers = new HashSet<>();

        for (Shape shape : grouped) {
            shape.collectMovedShapes(groupedMembers);
        }

        for (Shape shape : shapes) {
            if (!(shape instanceof Link link) || groupedSet.contains(shape)) {
                continue;
            }

            if (groupedMembers.contains(link.getStartShape()) && groupedMembers.contains(link.getEndShape())) {
                grouped.add(link);
                groupedSet.add(link);
            }
        }
    }

    public void ungroupSelectedShape() {
        if (selectedShapes.size() != 1) {
            return;
        }

        Shape selected = selectedShapes.get(0);
        if (!(selected instanceof Composite)) {
            return;
        }

        Composite composite = (Composite) selected;
        shapes.remove(composite);
        shapes.addAll(composite.getMembers());
        unselectAll();
        repaint();
    }

    public Object findPortOwnerAt(int x, int y) {
        List<Shape> ordered = new ArrayList<>(shapes);
        ordered.sort(Comparator.comparingInt(Shape::getDepth));

        for (Shape shape : ordered) {
            if (shape instanceof Linkable linkable && linkable.getPortAt(x, y) != null) {
                return (Object) shape;
            }
        }
        return null;
    }

    private void setupEvents() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (currentMode != null) {
                    currentMode.mousePressed(e);
                    repaint();
                }
            }

            @Override public void mouseReleased(MouseEvent e) {
                if (currentMode != null) {
                    currentMode.mouseReleased(e);
                    repaint();
                }
            }

            @Override public void mouseDragged(MouseEvent e) {
                if (currentMode != null) {
                    currentMode.mouseDragged(e);
                    repaint();
                }
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        setFocusable(true);
    }

    public Shape findShapeAt(int x, int y) {
        return shapes.stream()
                     .filter(s -> s.isInside(x, y))
                     .min(Comparator.comparingInt(Shape::getDepth))
                     .orElse(null);
    }

    private void clearSelectionFlag(Shape shape) {
        if (shape instanceof Object) {
            ((Object) shape).setSelected(false);
            return;
        }

        if (shape instanceof Composite) {
            ((Composite) shape).setSelected(false);
            for (Shape child : ((Composite) shape).getMembers()) {
                clearSelectionFlag(child);
            }
        }
    }

    public void setPreviewLink(Link previewLink) {
        this.previewLink = previewLink;
        repaint();
    }

    public void clearPreviewLink() {
        this.previewLink = null;
        repaint();
    }

}