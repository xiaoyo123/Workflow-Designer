package mode;

import java.awt.event.MouseEvent;

public interface Mode {
    void mousePressed(MouseEvent e);
    void mouseReleased(MouseEvent e);
    void mouseDragged(MouseEvent e);
}