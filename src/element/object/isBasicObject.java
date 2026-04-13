package element.object;

import java.awt.*;

import element.Port;

public interface isBasicObject {
    // Connect
    Port getPortAt(int x, int y);
    int getPortIndex(Port port);
    // Resize
    void setBounds(int x1, int y1, int x2, int y2);
    // Label & fill Color
    String getLabelName();
    void setLabelName(String name);
    Color getFillColor();
    void setFillColor(Color color);
}
