package window;

import javax.swing.*;

import canvas.Connectable;
import canvas.Element;
import canvas.Groupable;
import canvas.link.BasicLink;
import canvas.object.Composite;
import mode.Mode;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class Canvas extends JPanel {
    private Mode currentMode;
    private final List<Element> elements = new ArrayList<>();
    private final List<Element> selectedElements = new ArrayList<>();
    private Rectangle marquee = null;
    private BasicLink previewLink = null;
    private Element hoveredElement = null;

    public Canvas() {
        setBackground(Color.WHITE);

        setupEvents();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int bufferWidth = Math.max(1, getWidth());
        int bufferHeight = Math.max(1, getHeight());
        BufferedImage image = new BufferedImage(bufferWidth, bufferHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        elements.sort(Comparator.comparingInt(Element::getDepth).reversed());

        for (Element element : elements) {
            element.draw(g2);
        }

        if (marquee != null) {
            g2.setColor(new Color(60, 120, 220));
            float[] dash = {4f, 4f};
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
            g2.drawRect(marquee.x, marquee.y, marquee.width, marquee.height);
        }

        if (previewLink != null) {
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2f));
            previewLink.draw(g2);
        }

        g2.dispose();
        g.drawImage(image, 0, 0, null);
    }

    public void addElement(Element element) {
        elements.add(element);
        repaint();
    }

    public List<Element> getElements() {
        return new ArrayList<>(elements);
    }

    public void setMode(Mode mode) {
        this.currentMode = mode;
    }

    public int getFrontDepth() {
        return elements.stream().mapToInt(Element::getDepth).min().orElse(0);
    }

    public int getBackDepth() {
        return elements.stream().mapToInt(Element::getDepth).max().orElse(0);
    }

    public void bringToFront(Element element) {
        if (element == null) {
            return;
        }

        element.setDepth(getFrontDepth() - 1);
        repaint();
    }

    public void sendToBack(Element element) {
        if (element == null) {
            return;
        }

        element.setDepth(getBackDepth() + 1);
        repaint();
    }

    public void unselectAll() {
        for (Element element : elements) {
            element.setSelected(false);
        }
        selectedElements.clear();
        repaint();
    }

    public void selectOnly(Element element) {
        if (element == null || !element.isSelectable()) {
            return;
        }
        unselectAll();
        element.setSelected(true);
        selectedElements.add(element);
        repaint();
    }

    public void toggleSelection(Element element) {
        if (element == null || !element.isSelectable()) {
            return;
        }
        boolean selected = !element.isSelected();
        element.setSelected(selected);
        if (selected) {
            selectedElements.add(element);
        } else {
            selectedElements.remove(element);
        }
        repaint();
    }

    public List<Element> getSelectedElements() {
        return new ArrayList<>(selectedElements);
    }

    public void setMarquee(Rectangle rect) {
        this.marquee = rect;
        repaint();
    }

    public void clearMarquee() {
        this.marquee = null;
        repaint();
    }

    private void updateHoverState(int x, int y) {
        Element nextHovered = findElementAt(x, y);
        if (hoveredElement == nextHovered) {
            return;
        }

        if (hoveredElement != null) {
            hoveredElement.setHovered(false);
        }

        hoveredElement = nextHovered;
        if (hoveredElement != null) {
            hoveredElement.setHovered(true);
        }
    }

    private void clearHoverState() {
        if (hoveredElement != null) {
            hoveredElement.setHovered(false);
            hoveredElement = null;
        }
    }

    public void selectByRectangle(Rectangle rect, boolean append) {
        if (!append) {
            unselectAll();
        }

        for (Element element : elements) {
            if (!element.isSelectable()) {
                continue;
            }
            Rectangle bounds = new Rectangle(element.getLeft(), element.getTop(), element.getWidth(), element.getHeight());
            if (rect.intersects(bounds)) {
                element.setSelected(true);
                if (!selectedElements.contains(element)) {
                    selectedElements.add(element);
                }
            }
        }
        repaint();
    }

    public void groupSelectedElements() {
        if (selectedElements.size() < 2) {
            return;
        }

        List<Element> grouped = new ArrayList<>(selectedElements);

        for (Element element : grouped) {
            clearSelectionFlag(element);
        }
        int groupDepth = grouped.stream().mapToInt(Element::getDepth).min().orElse(0);
        elements.removeAll(grouped);
        elements.add(new Composite(grouped, groupDepth));
        unselectAll();
        repaint();
    }

    public void ungroupSelectedElement() {
        if (selectedElements.size() != 1) {
            return;
        }

        Element selected = selectedElements.get(0);
        if (!(selected instanceof Groupable groupable)) {
            return;
        }
        List<Element> members = groupable.getGroupMembers();
        if (members.isEmpty()) {
            return;
        }
        elements.remove(selected);
        elements.addAll(members);
        unselectAll();
        repaint();
    }

    public Element findPortOwnerAt(int x, int y) {
        List<Element> ordered = new ArrayList<>(elements);
        ordered.sort(Comparator.comparingInt(Element::getDepth));

        for (Element element : ordered) {
            if (element instanceof Connectable connectable && connectable.getPortAt(x, y) != null) {
                return element;
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

            @Override public void mouseMoved(MouseEvent e) {
                updateHoverState(e.getX(), e.getY());
                repaint();
            }

            @Override public void mouseExited(MouseEvent e) {
                clearHoverState();
                repaint();
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        setFocusable(true);
    }

    public Element findElementAt(int x, int y) {
        return elements.stream()
                     .filter(Element::isSelectable)
                     .filter(s -> s.isInside(x, y))
                     .min(Comparator.comparingInt(Element::getDepth))
                     .orElse(null);
    }

    private void clearSelectionFlag(Element element) {
        element.setSelected(false);
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