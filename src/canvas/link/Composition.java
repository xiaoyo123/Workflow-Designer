package canvas.link;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

import canvas.Port;

public class Composition extends BasicLink {
	public Composition(Port startPort, Port endPort, int depth) {
		super(startPort, endPort, depth);
	}

	@Override
	public void draw(Graphics g) {
		Graphics2D g2 = prepareGraphics(g);
		Port start = getStartPort();
		Port end = getEndPort();

		g2.drawLine(start.getX(), start.getY(), end.getX(), end.getY());

		double arrowAngle = Math.atan2(end.getY() - start.getY(), end.getX() - start.getX());
		AffineTransform tx = g2.getTransform();
		int x = end.getX();
		int y = end.getY();
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
