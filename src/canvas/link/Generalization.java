package canvas.link;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

import canvas.Port;

public class Generalization extends BasicLink {
	public Generalization(Port startPort, Port endPort, int depth) {
		super(startPort, endPort, depth);
	}

	@Override
	public void draw(Graphics g) {
		Graphics2D g2 = prepareGraphics(g);
		Port start = getStartPort();
		Port end = getEndPort();

		double angle = Math.atan2(end.getY() - start.getY(), end.getX() - start.getX());
		int triangleSize = 10;
		int adjustedEndX = (int) (end.getX() - triangleSize * Math.cos(angle));
		int adjustedEndY = (int) (end.getY() - triangleSize * Math.sin(angle));
		g2.drawLine(start.getX(), start.getY(), adjustedEndX, adjustedEndY);

		int size = 10;
		int x1 = (int) (end.getX() - size * Math.cos(angle - Math.PI / 6));
		int y1 = (int) (end.getY() - size * Math.sin(angle - Math.PI / 6));
		int x2 = (int) (end.getX() - size * Math.cos(angle + Math.PI / 6));
		int y2 = (int) (end.getY() - size * Math.sin(angle + Math.PI / 6));

		Polygon triangle = new Polygon();
		triangle.addPoint(end.getX(), end.getY());
		triangle.addPoint(x1, y1);
		triangle.addPoint(x2, y2);

		g2.drawPolygon(triangle);
	}
}
