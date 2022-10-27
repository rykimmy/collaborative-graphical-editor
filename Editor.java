import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

import javax.swing.*;

/**
 * Client-server graphical editor
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 * @author Ryan Kim (TA: Caroline Hall), 22W, completed PS6
 * @author Anand McCoole (TA: Jason Pak) 22W, completed PS6
 */

public class Editor extends JFrame {
	private static String serverIP = "localhost";			// IP address of sketch server
	// "localhost" for your own machine;
	// or ask a friend for their IP address

	private static final int width = 800, height = 800;		// canvas size

	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}
	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add
	private Color color = Color.black;			// current drawing color

	// Drawing state
	// these are remnants of my implementation; take them as possible suggestions or ignore them
	private Shape curr = null;					// current shape (if any) being drawn
	private Sketch sketch;						// holds and handles all the completed objects
	private int movingId = -1;					// current shape id (if any; else -1) being moved
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged


	// Communication
	private EditorCommunicator comm;			// communication with the sketch server

	public Editor() {
		super("Graphical Editor");

		sketch = new Sketch();

		// Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};

		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				handleRelease();
			}
		});

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});

		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Getter for the sketch instance variable
	 */
	public Sketch getSketch() {
		return sketch;
	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
		// TODO: YOUR CODE HERE
		for (Integer i: sketch.sketchID.keySet()) {
			sketch.sketchID.get(i).draw(g);
		}
		if (curr != null) {
			curr.draw(g);
		}
	}

	// Helpers for event handlers
	public void setShape(Shape shape) {
		curr = shape;
	}

	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, (request to) start dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */
	private void handlePress(Point p) {
		// TODO: YOUR CODE HERE
		drawFrom = p;

		// DRAW MODE
		if (mode == Mode.DRAW) {
			if (Objects.equals(shapeType, "ellipse")) {
				setShape(new Ellipse(drawFrom.x, drawFrom.y, color));
			} else if (Objects.equals(shapeType, "freehand")) {
				setShape(new Polyline(drawFrom.x, drawFrom.y, color));
			} else if (Objects.equals(shapeType, "rectangle")) {
				setShape(new Rectangle(drawFrom.x, drawFrom.y, color));
			} else if (Objects.equals(shapeType, "segment")) {
				setShape(new Segment(drawFrom.x, drawFrom.y, color));
			}

		// MOVE MODE
		} else if (mode == Mode.MOVE) {
			movingId = getSketch().locateShapeID(p.x, p.y);
			if (movingId != -1) {
				moveFrom = p;
			}

		// RECOLOR MODE
		} else if (mode == Mode.RECOLOR) {
			movingId = getSketch().locateShapeID(p.x, p.y);
			if (movingId != -1) {
				comm.commRecolor(movingId, color);
			}

		// DELETE MODE
		} else if (mode == Mode.DELETE) {
			movingId = getSketch().locateShapeID(p.x, p.y);
			if (movingId != -1) {
				comm.commDelete(movingId);
			}
		}

		repaint();
	}

	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 */
	private void handleDrag(Point p) {
		// TODO: YOUR CODE HERE

		if (mode == Mode.DRAW) {
			if (Objects.equals(shapeType, "ellipse")) {
				((Ellipse)curr).setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
			} else if (Objects.equals(shapeType, "freehand")) {
				((Polyline)curr).addPoint(p);
			} else if (Objects.equals(shapeType, "rectangle")) {
				((Rectangle)curr).setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
			} else if (Objects.equals(shapeType, "segment")) {
				((Segment)curr).setEnd(p.x, p.y);
			}
		} else if (mode == Mode.MOVE) {
			if (movingId != -1) {
				int dx = p.x - moveFrom.x;
				int dy = p.y - moveFrom.y;
				moveFrom = p;

				comm.commMove(movingId, dx, dy);
			}
		}

		repaint();
	}

	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object request on to the server;
	 * in moving mode, release it
	 */
	private void handleRelease() {
		// TODO: YOUR CODE HERE
		if (mode == Mode.DRAW) {
			comm.commDraw(curr.toString());
		} else if (mode == Mode.MOVE) {
			moveFrom = null;
			movingId = -1;
		}

		curr = null;
		repaint();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Editor();
			}
		});
	}
}

/*
Check
- Does the editor file send to the EditorCommunicator or the SketchServerCommunicator?
	- What are the differences? (Get overview of how these work together)
- Is it okay to hard code the spline[]
- How does synchronized work?
- Exceptions --> IO or generic? (look at sketch class)
- Using getSketch() vs .sketch (EditorCommunicator + SketchServerCommunicator) || using setShape(shape) vs curr = [shape] (this file)
 */
