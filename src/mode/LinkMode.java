package mode;

import java.awt.Point;
import java.awt.event.MouseEvent;

import canvas.Canvas;
import canvas.CanvasElement;
import canvas.link.BasicLink;

public class LinkMode implements Mode {
    @FunctionalInterface
    public interface LinkCreator {
        BasicLink create(CanvasElement start, Point startPoint, CanvasElement end, Point endPoint, int depth);
    }

    private final Canvas canvas;
    private final LinkCreator linkCreator;
    private CanvasElement startObject;
    private Point startPort;
    private int startPortIndex = -1;
    private BasicLink previewLink;

    public LinkMode(Canvas canvas, LinkCreator linkCreator) {
        this.canvas = canvas;
        this.linkCreator = linkCreator;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        startObject = canvas.findPortOwnerAt(e.getX(), e.getY());
        startPort = startObject != null ? startObject.getPortAt(e.getX(), e.getY()) : null;
        startPortIndex = startObject != null ? startObject.getPortIndexAt(e.getX(), e.getY()) : -1;
        
        if (startPort != null) {
            previewLink = linkCreator.create(startObject, startPort, null, new Point(e.getX(), e.getY()), 0);
            canvas.setPreviewLink(previewLink);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (startPort != null) {
            previewLink = linkCreator.create(startObject, startPort, null, new Point(e.getX(), e.getY()), 0);
            canvas.setPreviewLink(previewLink);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        canvas.clearPreviewLink();
        previewLink = null;
        
        if (startObject == null || startPort == null) {
            return;
        }

        CanvasElement endObject = canvas.findPortOwnerAt(e.getX(), e.getY());
        Point endPort = endObject != null ? endObject.getPortAt(e.getX(), e.getY()) : null;
        if (endObject == null || endPort == null) {
            return;
        }

        // Prevent self-linking
        if (startObject == endObject) {
            startObject = null;
            startPort = null;
            return;
        }

        int depth = canvas.getBackDepth() + 1;
        int endPortIndex = endObject.getPortIndexAt(e.getX(), e.getY());
        BasicLink link = linkCreator.create(startObject, startPort, endObject, endPort, depth);
        if (link == null) {
            startObject = null;
            startPort = null;
            return;
        }
        link.setPortBinding(startPortIndex, endPortIndex);
        canvas.addShape(link);
        
        startObject = null;
        startPort = null;
        startPortIndex = -1;
    }
}