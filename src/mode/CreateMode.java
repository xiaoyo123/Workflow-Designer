package mode;

import java.awt.event.MouseEvent;

import canvas.*;

import java.awt.Point;

public class CreateMode implements Mode {
    private final Canvas canvas;
    private final ShapeFactory shapeFactory;
    private Point startPoint;
    private final boolean oneShot;
    private final Runnable onComplete;

    public CreateMode(Canvas canvas, ShapeFactory shapeFactory) {
        this(canvas, shapeFactory, false, null);
    }

    public CreateMode(Canvas canvas, ShapeFactory shapeFactory, boolean oneShot, Runnable onComplete) {
        this.canvas = canvas;
        this.shapeFactory = shapeFactory;
        this.oneShot = oneShot;
        this.onComplete = onComplete;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        startPoint = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int depth = canvas.getFrontDepth() - 1;
        Point endPoint = e.getPoint();

        // Supports press-on-toolbar then release-on-canvas flow by using release point.
        Point anchor = (startPoint != null) ? startPoint : endPoint;

        Element newShape = shapeFactory.create(anchor.x, anchor.y, endPoint.x, endPoint.y, depth);

        canvas.addElement(newShape);

        if (oneShot && onComplete != null) {
            onComplete.run();
        }

        startPoint = null;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

}

