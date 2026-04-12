package model.link;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

import model.Shape;

public class Composition extends Link {
	public Composition(Shape startShape, Point startPoint, Shape endShape, Point endPoint, int depth) {
		super(startShape, startPoint, endShape, endPoint, depth);
	}

	@Override
	public void draw(Graphics g) {
		Graphics2D g2 = prepareGraphics(g);
		Point start = getStartPoint();
		Point end = getEndPoint();

		double angle = Math.atan2(end.y - start.y, end.x - start.x);
		int arrowDepth = 20;
		int adjustedEndX = (int) (end.x - arrowDepth * Math.cos(angle));
		int adjustedEndY = (int) (end.y - arrowDepth * Math.sin(angle));
		g2.drawLine(start.x, start.y, adjustedEndX, adjustedEndY);

		double arrowAngle = Math.atan2(end.y - start.y, end.x - start.x);
		AffineTransform tx = g2.getTransform();
		int x = end.x;
		int y = end.y;
		int size = 10;
		g2.translate(x, y);
		g2.rotate(arrowAngle - Math.PI / 2);
		Path2D.Double arrowHead = new Path2D.Double();
		arrowHead.moveTo(0, 0);
		arrowHead.lineTo(-size, -size);
		arrowHead.lineTo(0, -2 * size);
		arrowHead.lineTo(size, -size);
		arrowHead.closePath();
		g2.setColor(Color.WHITE);
		g2.fill(arrowHead);
		g2.setColor(Color.BLACK);
		g2.draw(arrowHead);
		g2.setTransform(tx);
	}
}
