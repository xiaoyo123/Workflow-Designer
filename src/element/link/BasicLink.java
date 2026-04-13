package element.link;

import element.Element;
import element.Port;
import java.awt.*;

public abstract class BasicLink extends Element {
    private final Port     startPort;
    private final Port     endPort;
    private final LinkType type;

    public BasicLink(Port startPort, Port endPort, LinkType type, int depth) {
        super(startPort.getX(), startPort.getY(), depth);
        this.startPort = startPort;
        this.endPort   = endPort;
        this.type      = type;
    }

    @Override
    public void draw(Graphics g) {
        int x1 = startPort.getX(), y1 = startPort.getY();
        int x2 = endPort.getX(),   y2 = endPort.getY();

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(x1, y1, x2, y2);

        double angle = Math.atan2(y2 - y1, x2 - x1);
        type.drawArrow(g2, x1, y1, x2, y2, angle);
    }

    // Link 位置完全由 Port 決定，move 不需要做任何事
    @Override
    public void move(int dx, int dy) {}

    // 規格未定義點擊 Link 的行為，永遠回傳 false
    @Override
    public boolean contains(int mx, int my) { return false; }

    // Link 不參與框選
    @Override
    public boolean isContainedIn(int left, int top, int right, int bottom) {
        return false;
    }

    @Override
    public boolean isSelectable() { return false; }

    public Port getStartPort() { return startPort; }
    public Port getEndPort()   { return endPort; }
}