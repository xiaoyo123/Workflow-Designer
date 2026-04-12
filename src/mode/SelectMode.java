package mode;

import java.awt.event.MouseEvent;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import canvas.*;

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
    private int resizeHandle = -1;
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
        resizeHandle = -1;
        clearResizeReference();
        Element hitElement = canvas.findElementAt(e.getX(), e.getY());

        if (hitElement != null
            && hitElement.canResize()
            && hitElement.isSelected()
            && selectedElements.size() == 1) {
            Port port = hitElement.getPortAt(e.getX(), e.getY());
            if (port != null) {
                int handle = hitElement.getPortIndex(port);
                if (handle != -1) {
                    isResizing = true;
                    resizingElement = hitElement;
                    resizeHandle = handle;
                    captureResizeReference(resizingElement);
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

        if (isResizing && resizingElement != null) {
            resizeObjectFromHandle(resizingElement, resizeHandle, e.getX(), e.getY());
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
            resizeHandle = -1;
            clearResizeReference();
            canvas.clearMarquee();
            return;
        }

        if (anchorElement != null) {
            canvas.bringToFront(anchorElement);
        }
        canvas.clearMarquee();
    }

    private void resizeObjectFromHandle(Element object, int handle, int mouseX, int mouseY) {
        int x1;
        int y1;
        int x2;
        int y2;

        switch (handle) {
            case 0: // top-left
                x1 = mouseX;
                y1 = mouseY;
                x2 = resizeRefRight;
                y2 = resizeRefBottom;
                break;
            case 1: // top-center
                x1 = resizeRefLeft;
                y1 = mouseY;
                x2 = resizeRefRight;
                y2 = resizeRefBottom;
                break;
            case 2: // top-right
                x1 = resizeRefLeft;
                y1 = mouseY;
                x2 = mouseX;
                y2 = resizeRefBottom;
                break;
            case 3: // right-center
                x1 = resizeRefLeft;
                y1 = resizeRefTop;
                x2 = mouseX;
                y2 = resizeRefBottom;
                break;
            case 4: // bottom-right
                x1 = resizeRefLeft;
                y1 = resizeRefTop;
                x2 = mouseX;
                y2 = mouseY;
                break;
            case 5: // bottom-center
                x1 = resizeRefLeft;
                y1 = resizeRefTop;
                x2 = resizeRefRight;
                y2 = mouseY;
                break;
            case 6: // bottom-left
                x1 = mouseX;
                y1 = resizeRefTop;
                x2 = resizeRefRight;
                y2 = mouseY;
                break;
            case 7: // left-center
                x1 = mouseX;
                y1 = resizeRefTop;
                x2 = resizeRefRight;
                y2 = resizeRefBottom;
                break;
            default:
                return;
        }

        int dx = x2 - x1;
        int dy = y2 - y1;

        if (Math.abs(dx) < MIN_RESIZE_SIZE) {
            int signX = dx >= 0 ? 1 : -1;
            if (handle == 0 || handle == 6 || handle == 7) {
                x1 = x2 - signX * MIN_RESIZE_SIZE;
            } else if (handle == 2 || handle == 3 || handle == 4) {
                x2 = x1 + signX * MIN_RESIZE_SIZE;
            }
        }

        if (Math.abs(dy) < MIN_RESIZE_SIZE) {
            int signY = dy >= 0 ? 1 : -1;
            if (handle == 0 || handle == 1 || handle == 2) {
                y1 = y2 - signY * MIN_RESIZE_SIZE;
            } else if (handle == 4 || handle == 5 || handle == 6) {
                y2 = y1 + signY * MIN_RESIZE_SIZE;
            }
        }

        object.setBounds(x1, y1, x2, y2);
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
    }
}