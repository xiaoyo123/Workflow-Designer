package mode;

import java.awt.event.MouseEvent;

import canvas.*;
import canvas.object.Oval;
import canvas.object.Rect;
import window.*;

import java.awt.Point;

public class CreateMode implements Mode {
    private final Canvas canvas;
    private final String type;
    private Point startPoint;
    private final boolean oneShot;
    private final Runnable onComplete;

    public CreateMode(Canvas canvas, String type) {
        this(canvas, type, false, null);
    }

    public CreateMode(Canvas canvas, String type, boolean oneShot, Runnable onComplete) {
        this.canvas = canvas;
        this.type = type;
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
        CanvasElement newShape;
        Point endPoint = e.getPoint();

        // Supports press-on-toolbar then release-on-canvas flow by using release point.
        Point anchor = (startPoint != null) ? startPoint : endPoint;

        if ("Oval".equals(type)) {
            newShape = new Oval(anchor.x, anchor.y, endPoint.x, endPoint.y, depth);
        } else {
            newShape = new Rect(anchor.x, anchor.y, endPoint.x, endPoint.y, depth);
        }

        canvas.addShape(newShape);

        if (oneShot && onComplete != null) {
            onComplete.run();
        }

        startPoint = null;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

}

