package model.link;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import model.Shape;

public class Association extends Link {
	public Association(Shape startShape, Point startPoint, Shape endShape, Point endPoint, int depth) {
		super(startShape, startPoint, endShape, endPoint, depth);
	}

	@Override
	public void draw(Graphics g) {
		Graphics2D g2 = prepareGraphics(g);
		Point start = getStartPoint();
		Point end = getEndPoint();
		g2.drawLine(start.x, start.y, end.x, end.y);

		double angle = Math.atan2(end.y - start.y, end.x - start.x);
		int size = 10;
		int x1 = (int) (end.x - size * Math.cos(angle - Math.PI / 6));
		int y1 = (int) (end.y - size * Math.sin(angle - Math.PI / 6));
		int x2 = (int) (end.x - size * Math.cos(angle + Math.PI / 6));
		int y2 = (int) (end.y - size * Math.sin(angle + Math.PI / 6));

		g2.drawLine(end.x, end.y, x1, y1);
		g2.drawLine(end.x, end.y, x2, y2);
	}
}
