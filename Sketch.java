import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Sketch class to handle Shapes
 * @author Ryan Kim (TA: Caroline Hall), 22W
 * @author Anand McCoole (TA: Jason Pak), 22W
 */

public class Sketch {
    TreeMap<Integer, Shape> sketchID;       // Map holding the ID:Shape
    int currID;                             // Current ID (Key)

    /**
     * Constructor
     */
    public Sketch() {
        sketchID = new TreeMap<>();
        currID = 0;
    }

    /**
     * Adds shape to Map
     * @param s, the shape being added
     */
    public synchronized void setShape(Shape s) {
        sketchID.put(currID, s);
        currID++;
    }

    /**
     * Adds shape to Map given an ID
     * @param s, the shape being added
     * @param id, the Key for the Map
     */
    public synchronized void setShapeWithID(Shape s, int id) {
        currID = id;
        sketchID.put(currID, s);
        currID++;
    }

    /**
     * Gets a Shape given an ID
     * @param id, the Key for to the Map holding Shapes
     * @return returns corresponding Shape to the given ID
     * @throws IOException
     */
    public synchronized Shape getShape(Integer id) throws IOException {
        if (sketchID.containsKey(id)) {
            return sketchID.get(id);
        } else {
            throw new IOException("Invalid key");
        }
    }

    /**
     * Gets shape based on given x,y
     * @param x,y; the coordinates that we'll use to check for shapes
     * @return the Shape at these coordinates, if there is one
     */
    public synchronized Shape locateShape(int x, int y) {
        for (Integer id: sketchID.descendingKeySet()) {
            if (sketchID.get(id).contains(x, y)) {
                return sketchID.get(id);
            }
        }
        return null;
    }

    /**
     * Gets shape ID based on given x,y
     * @param x,y; the coordinates that we'll use to check for shapes
     * @return the ID of the Shape at these coordinates, if there is one
     */
    public synchronized Integer locateShapeID(int x, int y) {
        for (Integer id: sketchID.descendingKeySet()) {
            if (sketchID.get(id).contains(x, y)) {
                return id;
            }
        }
        return -1;
    }

    /**
     * Deletes a Shape given its corresponding ID
     * @param id, of Shape to delete
     * @throws IOException
     */
    public synchronized void deleteShape(Integer id) throws IOException {
        if (!sketchID.containsKey(id)) {
            throw new IOException("the ID doesn't exist");
        }
        sketchID.remove(id);
    }
}
