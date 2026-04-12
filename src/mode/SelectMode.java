package mode;

import java.awt.event.MouseEvent;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import element.*;
import window.Canvas;

public class SelectMode implements Mode {
    private static final int MIN_RESIZE_SIZE = 40;
    private final Canvas canvas;
    private final List<Element> selectedElements = new ArrayList<>();
    private Element anchorElement = null;
    private Point lastPoint;
    private Point dragStart;
    private boolean isBoxSelecting = false;
    private boolean isResizing = false;
    private Element resizingElement = null;
    private Resizable resizingTarget = null;
    private boolean resizeLeftEdge = false;
    private boolean resizeRightEdge = false;
    private boolean resizeTopEdge = false;
    private boolean resizeBottomEdge = false;
    private int resizeRefLeft;
    private int resizeRefTop;
    private int resizeRefRight;
    private int resizeRefBottom;

    public SelectMode(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastPoint = e.getPoint();
        dragStart = e.getPoint();
        isBoxSelecting = false;
        isResizing = false;
        resizingElement = null;
        resizingTarget = null;
        clearResizeReference();
        Element hitElement = canvas.findElementAt(e.getX(), e.getY());

        if (hitElement != null
            && hitElement instanceof Resizable
            && hitElement.isSelected()
            && selectedElements.size() == 1) {
            if (hitElement instanceof Connectable connectable) {
                Port port = connectable.getPortAt(e.getX(), e.getY());
                if (port != null) {
                    captureResizeReference(hitElement);
                    captureResizeEdges(port);
                    if (!hasResizeEdge()) {
                        clearResizeReference();
                        return;
                    }
                    isResizing = true;
                    resizingElement = hitElement;
                    resizingTarget = (Resizable) hitElement;
                    anchorElement = hitElement;
                    return;
                }
            }
        }

        if (e.isControlDown() || e.isShiftDown()) {
            if (hitElement != null) {
                canvas.toggleSelection(hitElement);
                selectedElements.clear();
                selectedElements.addAll(canvas.getSelectedElements());
                anchorElement = hitElement;
            } else {
                isBoxSelecting = true;
                canvas.setMarquee(new Rectangle(dragStart.x, dragStart.y, 0, 0));
            }
            return;
        }

        if (hitElement == null) {
            canvas.unselectAll();
            selectedElements.clear();
            anchorElement = null;
            isBoxSelecting = true;
            canvas.setMarquee(new Rectangle(dragStart.x, dragStart.y, 0, 0));
            return;
        }

        canvas.selectOnly(hitElement);
        selectedElements.clear();
        selectedElements.add(hitElement);
        anchorElement = hitElement;

        hitElement.setSelected(true);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (isBoxSelecting) {
            int x = Math.min(dragStart.x, e.getX());
            int y = Math.min(dragStart.y, e.getY());
            int width = Math.abs(e.getX() - dragStart.x);
            int height = Math.abs(e.getY() - dragStart.y);
            canvas.setMarquee(new Rectangle(x, y, width, height));
            return;
        }

        if (isResizing && resizingElement != null && resizingTarget != null) {
            resizeObjectFromPort(resizingTarget, e.getX(), e.getY());
            lastPoint = e.getPoint();
            return;
        }

        if (selectedElements.isEmpty()) {
            return;
        }

        int dx = e.getX() - lastPoint.x;
        int dy = e.getY() - lastPoint.y;
        for (Element element : selectedElements) {
            element.move(dx, dy);
        }
        lastPoint = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isBoxSelecting) {
            int x = Math.min(dragStart.x, e.getX());
            int y = Math.min(dragStart.y, e.getY());
            int width = Math.abs(e.getX() - dragStart.x);
            int height = Math.abs(e.getY() - dragStart.y);
            boolean append = e.isControlDown() || e.isShiftDown();
            canvas.selectByRectangle(new Rectangle(x, y, width, height), append);
            selectedElements.clear();
            selectedElements.addAll(canvas.getSelectedElements());
            canvas.clearMarquee();
            isBoxSelecting = false;
            return;
        }

        if (isResizing) {
            isResizing = false;
            resizingElement = null;
            resizingTarget = null;
            clearResizeReference();
            canvas.clearMarquee();
            return;
        }

        if (anchorElement != null) {
            canvas.bringToFront(anchorElement);
        }
        canvas.clearMarquee();
    }

    private void resizeObjectFromPort(Resizable object, int mouseX, int mouseY) {
        int x1 = resizeRefLeft;
        int y1 = resizeRefTop;
        int x2 = resizeRefRight;
        int y2 = resizeRefBottom;

        if (resizeLeftEdge) {
            x1 = mouseX;
        }
        if (resizeRightEdge) {
            x2 = mouseX;
        }
        if (resizeTopEdge) {
            y1 = mouseY;
        }
        if (resizeBottomEdge) {
            y2 = mouseY;
        }

        int dx = x2 - x1;
        int dy = y2 - y1;

        if (Math.abs(dx) < MIN_RESIZE_SIZE) {
            int signX = dx >= 0 ? 1 : -1;
            if (resizeLeftEdge && !resizeRightEdge) {
                x1 = x2 - signX * MIN_RESIZE_SIZE;
            } else if (resizeRightEdge && !resizeLeftEdge) {
                x2 = x1 + signX * MIN_RESIZE_SIZE;
            }
        }

        if (Math.abs(dy) < MIN_RESIZE_SIZE) {
            int signY = dy >= 0 ? 1 : -1;
            if (resizeTopEdge && !resizeBottomEdge) {
                y1 = y2 - signY * MIN_RESIZE_SIZE;
            } else if (resizeBottomEdge && !resizeTopEdge) {
                y2 = y1 + signY * MIN_RESIZE_SIZE;
            }
        }

        object.setBounds(x1, y1, x2, y2);
    }

    private void captureResizeEdges(Port port) {
        final int tolerance = 10;
        resizeLeftEdge = Math.abs(port.getX() - resizeRefLeft) <= tolerance;
        resizeRightEdge = Math.abs(port.getX() - resizeRefRight) <= tolerance;
        resizeTopEdge = Math.abs(port.getY() - resizeRefTop) <= tolerance;
        resizeBottomEdge = Math.abs(port.getY() - resizeRefBottom) <= tolerance;
    }

    private boolean hasResizeEdge() {
        return resizeLeftEdge || resizeRightEdge || resizeTopEdge || resizeBottomEdge;
    }

    private void captureResizeReference(Element object) {
        resizeRefLeft = object.getLeft();
        resizeRefTop = object.getTop();
        resizeRefRight = object.getRight();
        resizeRefBottom = object.getBottom();
    }

    private void clearResizeReference() {
        resizeRefLeft = 0;
        resizeRefTop = 0;
        resizeRefRight = 0;
        resizeRefBottom = 0;
        resizeLeftEdge = false;
        resizeRightEdge = false;
        resizeTopEdge = false;
        resizeBottomEdge = false;
    }
}