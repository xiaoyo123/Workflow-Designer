package ui;

import javax.swing.*;
import controller.CanvasController;
import element.Element;
import element.object.ObjectType;
import mode.CreateMode;
import mode.Mode;
import mode.SelectMode;
import java.awt.*;
import java.awt.event.*;
import java.util.Comparator;

public class Canvas extends JPanel {
    private final CanvasController controller;
    private Mode currentMode;
    private Mode previousMode;

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
    }

    public void setMode(Mode mode)  { 
        this.previousMode = this.currentMode;
        this.currentMode = mode;
    }

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
    public void beginExternalCreate(ObjectType type, Runnable onComplete){
        Mode createMode = new CreateMode(controller, type);
        setMode(createMode);

        AWTEventListener releaseGuard = new AWTEventListener(){
            @Override
            public void eventDispatched(AWTEvent event) {
                if(!(event instanceof MouseEvent me)) return;
                if(me.getID() != MouseEvent.MOUSE_RELEASED) return;

                Point p = me.getLocationOnScreen();
                SwingUtilities.convertPointFromScreen(p, Canvas.this);
                if(Canvas.this.contains(p))
                    currentMode.mouseReleased(p.x, p.y);

                Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                currentMode = previousMode != null ? previousMode : new SelectMode(controller);
                onComplete.run();
                repaint();
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(releaseGuard, AWTEvent.MOUSE_EVENT_MASK);
    }
}
