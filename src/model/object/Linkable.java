package model.object;

import java.awt.Point;
import java.util.List;

public interface Linkable {
    List<Point> getPorts();

    Point getPortAt(int x, int y);

    default int getPortIndexAt(int x, int y) {
        List<Point> ports = getPorts();
        for (int index = 0; index < ports.size(); index++) {
            Point port = ports.get(index);
            if (Math.abs(port.x - x) <= 8 && Math.abs(port.y - y) <= 8) {
                return index;
            }
        }
        return -1;
    }
}