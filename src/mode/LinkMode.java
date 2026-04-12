package mode;

import java.awt.Point;
import java.awt.event.MouseEvent;

import canvas.Canvas;
import canvas.Connectable;
import canvas.Element;
import canvas.Port;
import canvas.link.BasicLink;

public class LinkMode implements Mode {
    @FunctionalInterface
    public interface LinkCreator {
        BasicLink create(Port startPort, Port endPort, int depth);
    }

    private final Canvas canvas;
    private final LinkCreator linkCreator;
    private Element startElement;
    private Port startPort;
    private BasicLink previewLink;

    public LinkMode(Canvas canvas, LinkCreator linkCreator) {
        this.canvas = canvas;
        this.linkCreator = linkCreator;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        startElement = canvas.findPortOwnerAt(e.getX(), e.getY());
        startPort = getPort(startElement, e.getX(), e.getY());
        
        if (startPort != null) {
            previewLink = linkCreator.create(startPort, new Port(e.getX(), e.getY()), 0);
            canvas.setPreviewLink(previewLink);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (startPort != null) {
            previewLink = linkCreator.create(startPort, new Port(e.getX(), e.getY()), 0);
            canvas.setPreviewLink(previewLink);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        canvas.clearPreviewLink();
        previewLink = null;
        
        if (startElement == null || startPort == null) {
            return;
        }

        Element endElement = canvas.findPortOwnerAt(e.getX(), e.getY());
        Port endPort = getPort(endElement, e.getX(), e.getY());
        if (endElement == null || endPort == null) {
            return;
        }

        // Prevent self-linking
        if (startElement == endElement) {
            startElement = null;
            startPort = null;
            return;
        }

        int depth = canvas.getBackDepth() + 1;
        BasicLink link = linkCreator.create(startPort, endPort, depth);
        if (link == null) {
            startElement = null;
            startPort = null;
            return;
        }
        canvas.addElement(link);
        
        startElement = null;
        startPort = null;
    }

    private Port getPort(Element element, int x, int y) {
        if (!(element instanceof Connectable connectable)) {
            return null;
        }
        return connectable.getPortAt(x, y);
    }
}