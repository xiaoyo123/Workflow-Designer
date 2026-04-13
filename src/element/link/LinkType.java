package element.link;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.Color;
import java.awt.Polygon;

public enum LinkType {
    ASSOCIATION {
        @Override
        public void drawArrow(Graphics g, int startX, int startY, int endX, int endY, double angle){
            int size = 10;
            int x1 = (int) (endX - size * Math.cos(angle - Math.PI / 6));
		    int y1 = (int) (endY - size * Math.sin(angle - Math.PI / 6));
		    int x2 = (int) (endX - size * Math.cos(angle + Math.PI / 6));
		    int y2 = (int) (endY - size * Math.sin(angle + Math.PI / 6));
            
            g.drawLine(endX, endY, x1, y1);
            g.drawLine(endX, endY, x2, y2);
        }
    },
    GENERALIZATION {
        @Override
        public void drawArrow(Graphics g, int startX, int startY, int endX, int endY, double angle){
            int size = 10;
            int x1 = (int) (endX - size * Math.cos(angle - Math.PI / 6));
            int y1 = (int) (endY - size * Math.sin(angle - Math.PI / 6));
            int x2 = (int) (endX - size * Math.cos(angle + Math.PI / 6));
            int y2 = (int) (endY - size * Math.sin(angle + Math.PI / 6));

            Polygon triangle = new Polygon();
            triangle.addPoint(endX, endY);
            triangle.addPoint(x1, y1);
            triangle.addPoint(x2, y2);
            g.setColor(Color.WHITE);
            g.fillPolygon(triangle);
            g.setColor(Color.BLACK);
            g.drawPolygon(triangle);
        }
    },
    COMPOSITION {
        @Override
        public void drawArrow(Graphics g, int startX, int startY, int endX, int endY, double angle){
            double arrowAngle = Math.atan2(endY - startY, endX - startX);
            Graphics2D g2 = (Graphics2D) g;
            AffineTransform tx = g2.getTransform();
            int size = 10;
            g2.translate(endX, endY);
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
    };
    public abstract void drawArrow(Graphics g, int startX, int startY, int endX, int endY, double angle);
}
