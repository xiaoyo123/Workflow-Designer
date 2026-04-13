package mode;

import controller.CanvasController;
import element.Port;
import element.link.LinkType;

public class LinkMode implements Mode {
    private final CanvasController controller;
    private final LinkType type;
    private Port startPort;

    public LinkMode(CanvasController controller, LinkType type) {
        this.controller = controller;
        this.type       = type;
    }

    @Override
    public void mousePressed(int x, int y) {
        startPort = controller.getPortAt(x, y);
        if (startPort != null) {
            controller.setPreviewLink(
                startPort.getX(), startPort.getY(), x, y);
        }
    }

    @Override
    public void mouseDragged(int x, int y) {
        if (startPort != null) {
            controller.setPreviewLink(
                startPort.getX(), startPort.getY(), x, y);
        }
    }

    @Override
    public void mouseReleased(int x, int y) {
        controller.clearPreviewLink();
        if (startPort == null) return;
        Port endPort = controller.getPortAt(x, y);
        controller.createLink(startPort, endPort, type);
        startPort = null;
    }
}