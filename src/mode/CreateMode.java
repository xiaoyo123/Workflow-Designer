package mode;

import controller.CanvasController;
import element.object.ObjectType;

public class CreateMode implements Mode {
    private final CanvasController controller;
    private final ObjectType type;
    private int startX, startY;

    public CreateMode(CanvasController controller, ObjectType type) {
        this.controller = controller;
        this.type       = type;
    }

    @Override
    public void mousePressed(int x, int y) {}

    @Override
    public void mouseDragged(int x, int y) {
        controller.setPreviewRect(startX, startY, x, y);
    }

    @Override
    public void mouseReleased(int x, int y) {}
}