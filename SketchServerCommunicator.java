import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 * @author Ryan Kim (TA: Caroline Hall), 22W, completed PS6
 * @author Anand McCoole (TA: Jason Pak) 22W, completed PS6
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;					// to talk with client
	private BufferedReader in;				// from client
	private PrintWriter out;				// to client
	private SketchServer server;			// handling communication for

	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
	}

	/**
	 * Sends a message to the client
	 * @param msg
	 */
	public void send(String msg) {
		out.println(msg);
	}
	
	/**
	 * Keeps listening for and handling (your code) messages from the client
	 */
	public void run() {
		try {
			System.out.println("someone connected");
			
			// Communication channel
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// Tell the client the current state of the world
			// TODO: YOUR CODE HERE
			Sketch sketch = server.getSketch();

			// Draw2 --> indicates that we will be using the passed ID
			// (helps address the ID discrepancy between newly joined clients)
			for (Integer i: sketch.sketchID.navigableKeySet()) {
				Shape shape = sketch.getShape(i);
				send("draw2 " + i + " " + shape.toString());
			}

			// Keep getting and handling messages from the client
			// TODO: YOUR CODE HERE
			String line;
			while ((line = in.readLine()) != null) {
				String[] spline = line.split(" ");
				String mode = spline[0];
				System.out.println(line);

				// DRAW Mode
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
						int x1 = Integer.parseInt(spline[2]);
						int y1 = Integer.parseInt(spline[3]);
						int x2 = Integer.parseInt(spline[4]);
						int y2 = Integer.parseInt(spline[5]);
						Color color = new Color(Integer.parseInt(spline[6]));

						sketch.setShape(new Segment(x1, y1, x2, y2, color));
					}

				// MOVE Mode
				} else if (Objects.equals(mode, "move")) {
					int id = Integer.parseInt(spline[1]);
					int dx = Integer.parseInt(spline[2]);
					int dy = Integer.parseInt(spline[3]);
					sketch.getShape(id).moveBy(dx, dy);

				// RECOLOR Mode
				} else if (Objects.equals(mode, "recolor")) {
					int id = Integer.parseInt(spline[1]);
					Color color = new Color(Integer.parseInt(spline[2]));
					sketch.getShape(id).setColor(color);

				// DELETE Mode
				} else if (Objects.equals(mode, "delete")) {
					int id = Integer.parseInt(spline[1]);
					sketch.deleteShape(id);
				}

				server.broadcast(line);
			}

			// Clean up -- note that also remove self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
