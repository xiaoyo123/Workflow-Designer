package controller;

import view.*;
import model.*;
import model.object.Object;
import model.object.Movable;
import model.object.Resizable;

import java.awt.event.MouseEvent;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class SelectMode implements Mode {
    private static final int MIN_RESIZE_SIZE = 40;
    private Canvas canvas;
    private final List<Shape> selectedShapes = new ArrayList<>();
    private Shape anchorShape = null;
    private Point lastPoint;
    private Point dragStart;
    private boolean isBoxSelecting = false;
    private boolean isResizing = false;
    private Object resizingObject = null;
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
        resizingObject = null;
        resizeHandle = -1;
        clearResizeReference();
        Shape hitShape = canvas.findShapeAt(e.getX(), e.getY());

        if (hitShape instanceof Resizable
            && hitShape instanceof Object
            && ((Object) hitShape).isSelected()
                && selectedShapes.size() == 1) {
            int handle = getHandleIndex((Object) hitShape, e.getPoint());
            if (handle != -1) {
                isResizing = true;
                resizingObject = (Object) hitShape;
                resizeHandle = handle;
                captureResizeReference(resizingObject);
                anchorShape = hitShape;
                return;
            }
        }

        if (e.isControlDown() || e.isShiftDown()) {
            if (hitShape != null) {
                canvas.toggleSelection(hitShape);
                selectedShapes.clear();
                selectedShapes.addAll(canvas.getSelectedShapes());
                anchorShape = hitShape;
            } else {
                isBoxSelecting = true;
                canvas.setMarquee(new Rectangle(dragStart.x, dragStart.y, 0, 0));
            }
            return;
        }

        if (hitShape == null) {
            canvas.unselectAll();
            selectedShapes.clear();
            anchorShape = null;
            isBoxSelecting = true;
            canvas.setMarquee(new Rectangle(dragStart.x, dragStart.y, 0, 0));
            return;
        }

        canvas.selectOnly(hitShape);
        selectedShapes.clear();
        selectedShapes.add(hitShape);
        anchorShape = hitShape;

        if (hitShape instanceof Object) {
            ((Object) hitShape).setSelected(true);
        }
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

        if (isResizing && resizingObject != null) {
            resizeObjectFromHandle(resizingObject, resizeHandle, e.getX(), e.getY());
            lastPoint = e.getPoint();
            return;
        }

        if (selectedShapes.isEmpty()) {
            return;
        }

        int dx = e.getX() - lastPoint.x;
        int dy = e.getY() - lastPoint.y;
        for (Shape shape : selectedShapes) {
            if (shape instanceof Movable movable) {
                movable.move(dx, dy);
            }
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
            selectedShapes.clear();
            selectedShapes.addAll(canvas.getSelectedShapes());
            canvas.clearMarquee();
            isBoxSelecting = false;
            return;
        }

        if (isResizing) {
            isResizing = false;
            resizingObject = null;
            resizeHandle = -1;
            clearResizeReference();
            canvas.clearMarquee();
            return;
        }

        if (anchorShape != null) {
            canvas.bringToFront(anchorShape);
        }
        canvas.clearMarquee();
    }

    private int getHandleIndex(Object object, Point p) {
        int left = object.getLeft();
        int top = object.getTop();
        int right = object.getRight();
        int bottom = object.getBottom();
        int centerX = object.getCenterX();
        int centerY = object.getCenterY();
        int tolerance = 10;

        if (isNear(p.x, p.y, left, top, tolerance)) return 0;
        if (isNear(p.x, p.y, centerX, top, tolerance)) return 1;
        if (isNear(p.x, p.y, right, top, tolerance)) return 2;
        if (isNear(p.x, p.y, right, centerY, tolerance)) return 3;
        if (isNear(p.x, p.y, right, bottom, tolerance)) return 4;
        if (isNear(p.x, p.y, centerX, bottom, tolerance)) return 5;
        if (isNear(p.x, p.y, left, bottom, tolerance)) return 6;
        if (isNear(p.x, p.y, left, centerY, tolerance)) return 7;

        return -1;
    }

    private boolean isNear(int x, int y, int targetX, int targetY, int tolerance) {
        return Math.abs(x - targetX) <= tolerance && Math.abs(y - targetY) <= tolerance;
    }

    private void resizeObjectFromHandle(Object object, int handle, int mouseX, int mouseY) {
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

    private void captureResizeReference(Object object) {
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