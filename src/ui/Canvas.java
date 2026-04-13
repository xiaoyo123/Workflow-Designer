package ui;

import javax.swing.*;
import controller.CanvasController;
import element.Element;
import mode.Mode;
import mode.SelectMode;
import java.awt.*;
import java.awt.event.*;
import java.util.Comparator;

public class Canvas extends JPanel {
    private final CanvasController controller;
    private Mode currentMode;

    private Rectangle previewRect   = null;
    private int[]     previewLink   = null;
    private Element   hoveredElement = null;

    public Canvas(CanvasController controller) {
        this.controller  = controller;
        this.currentMode = new SelectMode(controller);
        setBackground(Color.WHITE);
        setupEvents();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        controller.getElements().stream()
            .sorted(Comparator.comparingInt(Element::getDepth).reversed())
            .forEach(e -> e.draw(g2));

        if (previewRect != null) drawPreviewRect(g2);
        if (previewLink != null) drawPreviewLink(g2);
    }

    private void drawPreviewRect(Graphics2D g) {
        g.setColor(new Color(60, 120, 220, 50));
        g.fillRect(previewRect.x, previewRect.y,
                   previewRect.width, previewRect.height);
        g.setColor(new Color(60, 120, 220));
        float[] dash = {4f, 4f};
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10f, dash, 0f));
        g.drawRect(previewRect.x, previewRect.y,
                   previewRect.width, previewRect.height);
        g.setStroke(new BasicStroke(1f));
    }

    private void drawPreviewLink(Graphics2D g) {
        g.setColor(Color.GRAY);
        float[] dash = {5f, 5f};
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10f, dash, 0f));
        g.drawLine(previewLink[0], previewLink[1],
                   previewLink[2], previewLink[3]);
        g.setStroke(new BasicStroke(1f));
    }

    public void setMode(Mode mode)  { this.currentMode = mode; }

    public void setPreviewRect(int x1, int y1, int x2, int y2) {
        int x = Math.min(x1, x2), y = Math.min(y1, y2);
        this.previewRect = new Rectangle(x, y,
                           Math.abs(x2-x1), Math.abs(y2-y1));
        repaint();
    }

    public void clearPreviewRect() { this.previewRect = null; repaint(); }

    public void setPreviewLink(int x1, int y1, int x2, int y2) {
        this.previewLink = new int[]{x1, y1, x2, y2};
        repaint();
    }

    public void clearPreviewLink() { this.previewLink = null; repaint(); }

    private void updateHoverState(int x, int y) {
        Element next = controller.getTopElementAt(x, y).orElse(null);
        if (hoveredElement == next) return;
        if (hoveredElement != null) hoveredElement.setHovered(false);
        hoveredElement = next;
        if (hoveredElement != null) hoveredElement.setHovered(true);
        repaint();
    }

    private void clearHoverState() {
        if (hoveredElement != null) {
            hoveredElement.setHovered(false);
            hoveredElement = null;
            repaint();
        }
    }

    private void setupEvents() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                requestFocus();
                if (currentMode != null) {
                    currentMode.mousePressed(e.getX(), e.getY());
                    repaint();
                }
            }
            @Override public void mouseDragged(MouseEvent e) {
                if (currentMode != null) {
                    currentMode.mouseDragged(e.getX(), e.getY());
                    repaint();
                }
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (currentMode != null) {
                    currentMode.mouseReleased(e.getX(), e.getY());
                    repaint();
                }
            }
            @Override public void mouseMoved(MouseEvent e) {
                updateHoverState(e.getX(), e.getY());
            }
            @Override public void mouseExited(MouseEvent e) {
                clearHoverState();
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        setFocusable(true);
    }
}