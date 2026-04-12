package mode;

import canvas.Element;

@FunctionalInterface
public interface ShapeFactory {
    Element create(int x1, int y1, int x2, int y2, int depth);

    default Element createAt(int x, int y, int depth) {
        return create(x, y, x + 100, y + 60, depth);
    }
}
