package mode;

import controller.CanvasController;
import element.Element;
import element.Port;
import element.object.BasicObject;
import element.object.isBasicObject;

public class SelectMode implements Mode {
    private static final int MIN_SIZE = 20;
    private final CanvasController controller;

    private enum DragState { IDLE, MOVING, AREA_SELECT, RESIZING }
    private DragState dragState = DragState.IDLE;

    private int startX, startY;
    private int lastX,  lastY;

    // resize 用
    private Element draggingElement;
    private Port    draggingPort;
    private int resizeRefX1, resizeRefY1, resizeRefX2, resizeRefY2;
    private boolean resizeLeft, resizeRight, resizeTop, resizeBottom;

    public SelectMode(CanvasController controller) {
        this.controller = controller;
    }

    @Override
    public void mousePressed(int x, int y) {
        startX = x; startY = y;
        lastX  = x; lastY  = y;

        Element target = controller.getTopElementAt(x, y).orElse(null);

        // 已選取的 BasicObject，點到 port → resize
        if (target instanceof isBasicObject obj
                && target.isSelected()
                && controller.getSelectedElements().size() == 1) {
            Port port = obj.getPortAt(x, y);
            if (port != null) {
                captureResizeRef((BasicObject) target);
                captureResizeEdges(port);
                if (hasResizeEdge()) {
                    draggingPort    = port;
                    draggingElement = target;
                    dragState       = DragState.RESIZING;
                    return;
                }
            }
        }

        if (target != null) {
            // 點到物件本體 → 移動
            if (!controller.getSelectedElements().contains(target)) {
                controller.selectAt(x, y);
            }
            draggingElement = target;
            dragState       = DragState.MOVING;
        } else {
            // 點到空白 → 框選
            controller.clearSelection();
            controller.setPreviewRect(x, y, x, y);
            dragState = DragState.AREA_SELECT;
        }
    }

    @Override
    public void mouseDragged(int x, int y) {
        switch (dragState) {
            case MOVING -> {
                int dx = x - lastX, dy = y - lastY;
                controller.moveSelected(dx, dy);
            }
            case AREA_SELECT ->
                controller.setPreviewRect(startX, startY, x, y);
            case RESIZING ->
                resizeFromPort(x, y);
            case IDLE -> {}
        }
        lastX = x;
        lastY = y;
    }

    @Override
    public void mouseReleased(int x, int y) {
        switch (dragState) {
            case AREA_SELECT -> {
                controller.clearPreviewRect();
                controller.areaSelect(startX, startY, x, y);
            }
            case MOVING ->
                controller.bringToFront(draggingElement);
            case RESIZING, IDLE -> {}
        }
        dragState       = DragState.IDLE;
        draggingElement = null;
        draggingPort    = null;
        clearResizeRef();
    }

    // ── resize 輔助 ──

    private void resizeFromPort(int mx, int my) {
        if (!(draggingElement instanceof isBasicObject obj)) return;

        int x1 = resizeRefX1, y1 = resizeRefY1;
        int x2 = resizeRefX2, y2 = resizeRefY2;

        if (resizeLeft)   x1 = mx;
        if (resizeRight)  x2 = mx;
        if (resizeTop)    y1 = my;
        if (resizeBottom) y2 = my;

        // 最小尺寸限制
        if (Math.abs(x2 - x1) < MIN_SIZE) {
            if (resizeLeft && !resizeRight) x1 = x2 - Integer.signum(x2-x1) * MIN_SIZE;
            else                            x2 = x1 + Integer.signum(x2-x1) * MIN_SIZE;
        }
        if (Math.abs(y2 - y1) < MIN_SIZE) {
            if (resizeTop && !resizeBottom) y1 = y2 - Integer.signum(y2-y1) * MIN_SIZE;
            else                            y2 = y1 + Integer.signum(y2-y1) * MIN_SIZE;
        }

        obj.setBounds(x1, y1, x2, y2);
        if (canvas() != null) canvas().repaint();
    }

    private void captureResizeRef(BasicObject obj) {
        resizeRefX1 = obj.getX();
        resizeRefY1 = obj.getY();
        resizeRefX2 = obj.getX() + obj.getWidth();
        resizeRefY2 = obj.getY() + obj.getHeight();
    }

    private void captureResizeEdges(Port port) {
        final int tol = 10;
        resizeLeft   = Math.abs(port.getX() - resizeRefX1) <= tol;
        resizeRight  = Math.abs(port.getX() - resizeRefX2) <= tol;
        resizeTop    = Math.abs(port.getY() - resizeRefY1) <= tol;
        resizeBottom = Math.abs(port.getY() - resizeRefY2) <= tol;
    }

    private boolean hasResizeEdge() {
        return resizeLeft || resizeRight || resizeTop || resizeBottom;
    }

    private void clearResizeRef() {
        resizeRefX1 = resizeRefY1 = resizeRefX2 = resizeRefY2 = 0;
        resizeLeft = resizeRight = resizeTop = resizeBottom = false;
    }

    private ui.Canvas canvas() {
        return controller.getCanvas();
    }
}