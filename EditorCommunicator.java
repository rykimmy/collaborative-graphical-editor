import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Handles communication to/from the server for the editor
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author Chris Bailey-Kellogg; overall structure substantially revised Winter 2014
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author Ryan Kim (TA: Caroline Hall), 22W, completed PS6
 * @author Anand McCoole (TA: Jason Pak) 22W, completed PS6
 */
public class EditorCommunicator extends Thread {
	private PrintWriter out;		// to server
	private BufferedReader in;		// from server
	protected Editor editor;		// handling communication for

	/**
	 * Establishes connection and in/out pair
	 */
	public EditorCommunicator(String serverIP, Editor editor) {
		this.editor = editor;
		System.out.println("connecting to " + serverIP + "...");
		try {
			Socket sock = new Socket(serverIP, 4242);
			out = new PrintWriter(sock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.println("...connected");
		}
		catch (IOException e) {
			System.err.println("couldn't connect");
			System.exit(-1);
		}
	}

	/**
	 * Sends message to the server
	 */
	public void send(String msg) {
		out.println(msg);
	}

	/**
	 * Keeps listening for and handling (your code) messages from the server
	 */
	public void run() {
		try {
			// Handle messages
			// TODO: YOUR CODE HERE

			Sketch sketch = editor.getSketch();
			String line;

			while ((line = in.readLine()) != null) {
				String[] spline = line.split(" ");
				String mode = spline[0];

				// DRAW Mode – regular draw mode that doesn't pass an ID
				if (Objects.equals(mode, "draw")) {

					// ELLIPSE
					if (Objects.equals(spline[1], "ellipse")) {
						int x1 = Integer.parseInt(spline[2]);
						int y1 = Integer.parseInt(spline[3]);
						int x2 = Integer.parseInt(spline[4]);
						int y2 = Integer.parseInt(spline[5]);
						Color color = new Color(Integer.parseInt(spline[6]));

						sketch.setShape(new Ellipse(x1, y1, x2, y2, color));

					// POLYLINE
					} else if (Objects.equals(spline[1], "polyline")) {
						ArrayList<Point> points = new ArrayList<>();

						// Increment by two to get every x,y value
						for (int i = 2; i < spline.length-2; i+=2) {
							Point newPoint = new Point(Integer.parseInt(spline[i]), Integer.parseInt(spline[i+1]));
							points.add(newPoint);
						}
						Color color = new Color(Integer.parseInt(spline[spline.length-1]));

						sketch.setShape(new Polyline(points, color));

					// RECTANGLE
					} else if (Objects.equals(spline[1], "rectangle")) {
						int x1 = Integer.parseInt(spline[2]);
						int y1 = Integer.parseInt(spline[3]);
						int x2 = Integer.parseInt(spline[4]);
						int y2 = Integer.parseInt(spline[5]);
						Color color = new Color(Integer.parseInt(spline[6]));

						sketch.setShape(new Rectangle(x1, y1, x2, y2, color));

					// SEGMENT
					} else if (Objects.equals(spline[1], "segment")) {
						//return "segment "+x1+" "+y1+" "+x2+" "+y2+" "+color.getRGB();
						int x1 = Integer.parseInt(spline[2]);
						int y1 = Integer.parseInt(spline[3]);
						int x2 = Integer.parseInt(spline[4]);
						int y2 = Integer.parseInt(spline[5]);
						Color color = new Color(Integer.parseInt(spline[6]));

						sketch.setShape(new Segment(x1, y1, x2, y2, color));
					}

				// DRAW2 Mode – revised draw mode that updates sketch Map with given id
				} else if (Objects.equals(mode, "draw2")) {
					int id = Integer.parseInt(spline[1]);		// Correct ID to put new Shape

					// ELLIPSE
					if (Objects.equals(spline[2], "ellipse")) {
						int x1 = Integer.parseInt(spline[3]);
						int y1 = Integer.parseInt(spline[4]);
						int x2 = Integer.parseInt(spline[5]);
						int y2 = Integer.parseInt(spline[6]);
						Color color = new Color(Integer.parseInt(spline[7]));

						sketch.setShapeWithID(new Ellipse(x1, y1, x2, y2, color), id);

					// POLYLINE
					} else if (Objects.equals(spline[2], "polyline")) {
						ArrayList<Point> points = new ArrayList<>();

						for (int i = 3; i < spline.length-2; i+=2) {
							Point newPoint = new Point(Integer.parseInt(spline[i]), Integer.parseInt(spline[i+1]));
							points.add(newPoint);
						}
						Color color = new Color(Integer.parseInt(spline[spline.length-1]));

						sketch.setShapeWithID(new Polyline(points, color), id);

					// RECTANGLE
					} else if (Objects.equals(spline[2], "rectangle")) {
						//return "rectangle "+x1+" "+y1+" "+x2+" "+y2+" "+color.getRGB();
						int x1 = Integer.parseInt(spline[3]);
						int y1 = Integer.parseInt(spline[4]);
						int x2 = Integer.parseInt(spline[5]);
						int y2 = Integer.parseInt(spline[6]);
						Color color = new Color(Integer.parseInt(spline[7]));

						sketch.setShapeWithID(new Rectangle(x1, y1, x2, y2, color), id);

					// SEGMENT
					} else if (Objects.equals(spline[2], "segment")) {
						//return "segment "+x1+" "+y1+" "+x2+" "+y2+" "+color.getRGB();
						int x1 = Integer.parseInt(spline[3]);
						int y1 = Integer.parseInt(spline[4]);
						int x2 = Integer.parseInt(spline[5]);
						int y2 = Integer.parseInt(spline[6]);
						Color color = new Color(Integer.parseInt(spline[7]));

						sketch.setShapeWithID(new Segment(x1, y1, x2, y2, color), id);
					}

				// MOVE Mode
				} else if (Objects.equals(mode, "move")) {
					// move shapeID dx dy
					int id = Integer.parseInt(spline[1]);
					int dx = Integer.parseInt(spline[2]);
					int dy = Integer.parseInt(spline[3]);
					sketch.getShape(id).moveBy(dx, dy);

					// RECOLOR Mode
				} else if (Objects.equals(mode, "recolor")) {
					// recolor shapeID color(RGB)
					int id = Integer.parseInt(spline[1]);
					Color color = new Color(Integer.parseInt(spline[2]));
					sketch.getShape(id).setColor(color);

					// DELETE Mode
				} else if (Objects.equals(mode, "delete")) {
					// delete shapeID
					int id = Integer.parseInt(spline[1]);
					sketch.deleteShape(id);
				}

				editor.repaint();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("server hung up");
		}
	}

	// Send editor requests to the server
	// TODO: YOUR CODE HERE

	/**
	 * Draw Command – sends draw request to the server with adequate information on the Shape
	 * @param shape, the Shape's toString output
	 */
	public void commDraw(String shape) {
		send("draw " + shape);
	}

	/**
	 * Move Command – sends move request to the server with necessary information on the Shape
	 * @param shapeID, ID of the Shape being moved
	 * @param dx,dy; difference in x,y used for moving purposes
	 */
	public void commMove(int shapeID, int dx, int dy) {
		send("move " + shapeID + " " + dx + " " + dy);
	}

	/**
	 * Recolor Command – sends recolor request to the server with the required information on the Shape
	 * @param shapeID, ID of the Shape being recolored
	 * @param color, new color of the Shape
	 */
	public void commRecolor(int shapeID, Color color) {
		send("recolor " + shapeID + " " + color.getRGB());
	}

	/**
	 * Delete Command – sends delete request to the server with the needed information on the Shape
	 * @param shapeID, ID of the Shape being deleted
	 */
	public void commDelete(int shapeID) {
		send("delete " + shapeID);
	}
}
