package mode;

import controller.CanvasController;
import element.object.ObjectType;

public class CreateMode implements Mode {
    private final CanvasController controller;
    private final ObjectType type;

    public CreateMode(CanvasController controller, ObjectType type) {
        this.controller = controller;
        this.type       = type;
    }

    @Override
    public void mousePressed(int x, int y) {}

    @Override
    public void mouseDragged(int x, int y) {}

    @Override
    public void mouseReleased(int x, int y) {
        controller.createElement(type, x, y);
    }
}