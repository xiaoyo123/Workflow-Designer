package canvas.link;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;

import canvas.CanvasElement;

public class Generalization extends BasicLink {
	public Generalization(CanvasElement startShape, Point startPoint, CanvasElement endShape, Point endPoint, int depth) {
		super(startShape, startPoint, endShape, endPoint, depth);
	}

	@Override
	public void draw(Graphics g) {
		Graphics2D g2 = prepareGraphics(g);
		Point start = getStartPoint();
		Point end = getEndPoint();

		double angle = Math.atan2(end.y - start.y, end.x - start.x);
		int triangleSize = 10;
		int adjustedEndX = (int) (end.x - triangleSize * Math.cos(angle));
		int adjustedEndY = (int) (end.y - triangleSize * Math.sin(angle));
		g2.drawLine(start.x, start.y, adjustedEndX, adjustedEndY);

		int size = 10;
		int x1 = (int) (end.x - size * Math.cos(angle - Math.PI / 6));
		int y1 = (int) (end.y - size * Math.sin(angle - Math.PI / 6));
		int x2 = (int) (end.x - size * Math.cos(angle + Math.PI / 6));
		int y2 = (int) (end.y - size * Math.sin(angle + Math.PI / 6));

		Polygon triangle = new Polygon();
		triangle.addPoint(end.x, end.y);
		triangle.addPoint(x1, y1);
		triangle.addPoint(x2, y2);

		g2.drawPolygon(triangle);
	}
}
