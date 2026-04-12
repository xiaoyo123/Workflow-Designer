package canvas.link;

import java.awt.Graphics;
import java.awt.Graphics2D;

import canvas.Port;

public class Association extends BasicLink {
	public Association(Port startPort, Port endPort, int depth) {
		super(startPort, endPort, depth);
	}

	@Override
	public void draw(Graphics g) {
		Graphics2D g2 = prepareGraphics(g);
		Port start = getStartPort();
		Port end = getEndPort();
		g2.drawLine(start.getX(), start.getY(), end.getX(), end.getY());

		double angle = Math.atan2(end.getY() - start.getY(), end.getX() - start.getX());
		int size = 10;
		int x1 = (int) (end.getX() - size * Math.cos(angle - Math.PI / 6));
		int y1 = (int) (end.getY() - size * Math.sin(angle - Math.PI / 6));
		int x2 = (int) (end.getX() - size * Math.cos(angle + Math.PI / 6));
		int y2 = (int) (end.getY() - size * Math.sin(angle + Math.PI / 6));

		g2.drawLine(end.getX(), end.getY(), x1, y1);
		g2.drawLine(end.getX(), end.getY(), x2, y2);
	}
}
