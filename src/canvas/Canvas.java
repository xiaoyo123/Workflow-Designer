package canvas;

import javax.swing.*;

import canvas.link.BasicLink;
import canvas.object.Composite;
import mode.Mode;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Comparator;
import java.util.Set;

public class Canvas extends JPanel {
    private Mode currentMode;
    private List<CanvasElement> shapes = new ArrayList<>();
    private List<CanvasElement> selectedShapes = new ArrayList<>();
    private Rectangle marquee = null;
    private BasicLink previewLink = null;

    public Canvas() {
        setBackground(Color.WHITE);

        setupEvents();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        shapes.sort(Comparator.comparingInt(CanvasElement::getDepth).reversed());

        for (CanvasElement shape : shapes) {
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

    public void addShape(CanvasElement s) {
        shapes.add(s);
        repaint();
    }

    public List<CanvasElement> getShapes() {
        return new ArrayList<>(shapes);
    }

    public void setMode(Mode mode) {
        this.currentMode = mode;
    }

    public int getFrontDepth() {
        return shapes.stream().mapToInt(CanvasElement::getDepth).min().orElse(0);
    }

    public int getBackDepth() {
        return shapes.stream().mapToInt(CanvasElement::getDepth).max().orElse(0);
    }

    public void bringToFront(CanvasElement shape) {
        if (shape == null) {
            return;
        }

        shape.setDepth(getFrontDepth() - 1);
        repaint();
    }

    public void sendToBack(CanvasElement shape) {
        if (shape == null) {
            return;
        }

        shape.setDepth(getBackDepth() + 1);
        repaint();
    }

    public void unselectAll() {
        for (CanvasElement shape : shapes) {
            shape.setSelected(false);
        }
        selectedShapes.clear();
        repaint();
    }

    public void selectOnly(CanvasElement shape) {
        unselectAll();
        shape.setSelected(true);
        selectedShapes.add(shape);
        repaint();
    }

    public void toggleSelection(CanvasElement shape) {
        boolean selected = !shape.isSelected();
        shape.setSelected(selected);
        if (selected) {
            selectedShapes.add(shape);
        } else {
            selectedShapes.remove(shape);
        }
        repaint();
    }

    public List<CanvasElement> getSelectedShapes() {
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

        for (CanvasElement shape : shapes) {
            Rectangle bounds = new Rectangle(shape.getLeft(), shape.getTop(), shape.getWidth(), shape.getHeight());
            if (rect.intersects(bounds)) {
                shape.setSelected(true);
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

        List<CanvasElement> grouped = new ArrayList<>(selectedShapes);
        includeInternalLinks(grouped);

        for (CanvasElement shape : grouped) {
            clearSelectionFlag(shape);
        }
        int groupDepth = grouped.stream().mapToInt(CanvasElement::getDepth).min().orElse(0);
        shapes.removeAll(grouped);
        shapes.add(new Composite(grouped, groupDepth));
        unselectAll();
        repaint();
    }

    private void includeInternalLinks(List<CanvasElement> grouped) {
        Set<CanvasElement> groupedSet = new HashSet<>(grouped);
        Set<CanvasElement> groupedMembers = new HashSet<>();

        for (CanvasElement shape : grouped) {
            shape.collectMovedShapes(groupedMembers);
        }

        for (CanvasElement shape : shapes) {
            if (!(shape instanceof BasicLink link) || groupedSet.contains(shape)) {
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

        CanvasElement selected = selectedShapes.get(0);
        if (!(selected instanceof Composite)) {
            return;
        }

        Composite composite = (Composite) selected;
        shapes.remove(composite);
        shapes.addAll(composite.getMembers());
        unselectAll();
        repaint();
    }

    public CanvasElement findPortOwnerAt(int x, int y) {
        List<CanvasElement> ordered = new ArrayList<>(shapes);
        ordered.sort(Comparator.comparingInt(CanvasElement::getDepth));

        for (CanvasElement shape : ordered) {
            if (shape.getPortAt(x, y) != null) {
                return shape;
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

    public CanvasElement findShapeAt(int x, int y) {
        return shapes.stream()
                     .filter(s -> s.isInside(x, y))
                     .min(Comparator.comparingInt(CanvasElement::getDepth))
                     .orElse(null);
    }

    private void clearSelectionFlag(CanvasElement shape) {
        shape.setSelected(false);
        if (shape instanceof Composite composite) {
            for (CanvasElement child : composite.getMembers()) {
                clearSelectionFlag(child);
            }
        }
    }

    public void setPreviewLink(BasicLink previewLink) {
        this.previewLink = previewLink;
        repaint();
    }

    public void clearPreviewLink() {
        this.previewLink = null;
        repaint();
    }

}