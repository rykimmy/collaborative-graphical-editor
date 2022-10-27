import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 * @author Ryan Kim (TA: Caroline Hall), 22W, completed PS6, added Constructor, toString, addPoint methods
 * @author Anand McCoole (TA: Jason Pak), 22W, completed PS6, added Constructor, toString, addPoint methods
 */
public class Polyline implements Shape {
	// TODO: YOUR CODE HERE
	ArrayList<Point> points;	// Holds all the points that make up the many segments of the polyline
	Color color;				// Color of the polyline

	/**
	 * Constructor for beginning a new polyline
	 * @param x,y; the coordinates of the starting point of the polyline
	 * @param color, color of the polyline
	 */
	public Polyline(int x, int y, Color color) {
		points = new ArrayList<>();
		points.add(new Point(x, y));
		this.color = color;
	}

	/**
	 * Constructor for creating a completed polyline
	 * @param points, List of points that make up the segments that make up the polyline
	 * @param color, color of the polyline
	 */
	public Polyline(ArrayList<Point> points, Color color) {
		this.points = points;
		this.color = color;
	}

	/**
	 * Adds Point p to the list of points that make up the polyline
	 * @param p, the added Point
	 */
	public void addPoint(Point p) {
		points.add(p);
	}

	@Override
	public void moveBy(int dx, int dy) {
		for (Point p: points) {
			p.x += dx;
			p.y += dy;
		}
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}
	
	@Override
	public boolean contains(int x, int y) {
		for (int i = 0; i < points.size()-1; i++) {
			if (Segment.pointToSegmentDistance(x, y, (int) points.get(i).getX(), (int) points.get(i).getY(), (int) points.get(i+1).getX(), (int) points.get(i+1).getY()) <= 3){
				return true;
			}
		}
		return false;
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		for (int i = 0; i < points.size()-1; i++) {
			g.drawLine((int) points.get(i).getX(), (int) points.get(i).getY(), (int) points.get(i+1).getX(), (int) points.get(i+1).getY());
		}
	}

	@Override
	public String toString() {
		String pointList = "";
		for (Point p: points) {
			pointList += p.x+" "+p.y+" ";
		}

		return "polyline "+pointList+color.getRGB();
	}
}
