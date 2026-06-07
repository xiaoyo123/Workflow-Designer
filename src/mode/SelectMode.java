package mode;

import controller.CanvasController;
import element.Element;
import element.Port;
import element.object.BasicObject;
import java.awt.Rectangle;
import java.util.EnumSet;

public class SelectMode implements Mode {
    private static final int EDGE_TOLERANCE = 10;

    private final CanvasController controller;

    private enum DragState { IDLE, MOVING, AREA_SELECT, RESIZING }

    private enum ResizeEdge { LEFT, RIGHT, TOP, BOTTOM }

    private DragState dragState = DragState.IDLE;
    private int startX, startY;
    private int lastX, lastY;

    private Element draggingElement;
    private BasicObject resizingObject;
    private EnumSet<ResizeEdge> resizeEdges = EnumSet.noneOf(ResizeEdge.class);
    private Rectangle resizeStartBounds;

    public SelectMode(CanvasController controller) {
        this.controller = controller;
    }

    @Override
    public void mousePressed(int x, int y) {
        startX = x;
        startY = y;
        lastX = x;
        lastY = y;

        Element target = controller.getTopElementAt(x, y).orElse(null);

        if (tryStartResize(target, x, y)) {
            return;
        }

        if (target != null) {
            if (!controller.getSelectedElements().contains(target)) {
                controller.selectAt(x, y);
            }
            draggingElement = target;
            dragState = DragState.MOVING;
        } else {
            controller.clearSelection();
            dragState = DragState.AREA_SELECT;
        }
    }

    @Override
    public void mouseDragged(int x, int y) {
        switch (dragState) {
            case MOVING -> {
                int dx = x - lastX;
                int dy = y - lastY;
                controller.moveSelected(dx, dy);
            }
            case AREA_SELECT -> {}
            case RESIZING -> resizeFromHandle(x, y);
            case IDLE -> {}
        }
        lastX = x;
        lastY = y;
    }

    @Override
    public void mouseReleased(int x, int y) {
        switch (dragState) {
            case AREA_SELECT -> controller.areaSelect(startX, startY, x, y);
            case MOVING -> controller.bringToFront(draggingElement);
            case RESIZING, IDLE -> {}
        }

        dragState = DragState.IDLE;
        draggingElement = null;
        resizingObject = null;
        resizeEdges.clear();
        resizeStartBounds = null;
    }

    private boolean tryStartResize(Element target, int x, int y) {
        if (!(target instanceof BasicObject obj)
                || !target.isSelected()
                || controller.getSelectedElements().size() != 1) {
            return false;
        }

        Port port = obj.getPortAt(x, y);
        if (port == null) {
            return false;
        }

        EnumSet<ResizeEdge> edges = detectResizeEdges(obj, port);
        if (edges.isEmpty()) {
            return false;
        }

        resizingObject = obj;
        resizeEdges = edges;
        resizeStartBounds = new Rectangle(obj.getX(), obj.getY(), obj.getWidth(), obj.getHeight());
        dragState = DragState.RESIZING;
        return true;
    }

    private EnumSet<ResizeEdge> detectResizeEdges(BasicObject obj, Port port) {

        EnumSet<ResizeEdge> edges = EnumSet.noneOf(ResizeEdge.class);
        if (port.getOffsetX() == 0) edges.add(ResizeEdge.LEFT);
        if (port.getOffsetX() == obj.getWidth()) edges.add(ResizeEdge.RIGHT);
        if (port.getOffsetY() == 0) edges.add(ResizeEdge.TOP);
        if (port.getOffsetY() == obj.getHeight()) edges.add(ResizeEdge.BOTTOM);

        return edges;
    }

    private void resizeFromHandle(int mx, int my) {
        if (resizingObject == null || resizeEdges.isEmpty() || resizeStartBounds == null) {
            return;
        }

        int x1 = resizeStartBounds.x;
        int y1 = resizeStartBounds.y;
        int x2 = resizeStartBounds.x + resizeStartBounds.width;
        int y2 = resizeStartBounds.y + resizeStartBounds.height;

        boolean resizeLeft = resizeEdges.contains(ResizeEdge.LEFT);
        boolean resizeRight = resizeEdges.contains(ResizeEdge.RIGHT);
        boolean resizeTop = resizeEdges.contains(ResizeEdge.TOP);
        boolean resizeBottom = resizeEdges.contains(ResizeEdge.BOTTOM);

        if (resizeLeft) x1 = mx;
        if (resizeRight) x2 = mx;
        if (resizeTop) y1 = my;
        if (resizeBottom) y2 = my;

        if (Math.abs(x2 - x1) < BasicObject.MIN_SIZE) {
            int sign = x2 >= x1 ? 1 : -1;
            if (resizeLeft && !resizeRight) {
                x1 = x2 - sign * BasicObject.MIN_SIZE;
            } else {
                x2 = x1 + sign * BasicObject.MIN_SIZE;
            }
        }
        if (Math.abs(y2 - y1) < BasicObject.MIN_SIZE) {
            int sign = y2 >= y1 ? 1 : -1;
            if (resizeTop && !resizeBottom) {
                y1 = y2 - sign * BasicObject.MIN_SIZE;
            } else {
                y2 = y1 + sign * BasicObject.MIN_SIZE;
            }
        }

        resizingObject.setBounds(x1, y1, x2, y2);
        if (canvas() != null) canvas().repaint();
    }

    private ui.Canvas canvas() {
        return controller.getCanvas();
    }
}
