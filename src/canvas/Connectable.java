package canvas;

public interface Connectable {
    Port getPortAt(int x, int y);
    int getPortIndex(Port port);
}
