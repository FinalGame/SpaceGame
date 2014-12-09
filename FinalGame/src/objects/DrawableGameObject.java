package objects;

import java.awt.*;

/**
 * Superclass for game objects that are visible on the game board.
 * Note that some of the subclasses may not use all of the functionality
 * in this class: Stars, for instance, have no direction, and ships
 * have no ID (the ship ID is in the <CODE>Player</CODE> object.
 * <P>
 *
 * This class doesn't know how to draw objects, so that's left
 * to the subclass to do by overriding the abstract <CODE>draw</CODE>
 * method.
 * <P>
 *
 * <B>NOTE!</B> On the client side, all fields are not necessarily
 * updated. It is for instance not possible to query the direction
 * of a bomb. Or more correctly, you may query it, but you won't get
 * the correct answer, as the server doesn't send it to us. Consult
 * the documentation for the object you work with to see what is
 * supported.
 */
public abstract class DrawableGameObject {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private short id;

/*-----------------------------------------------------------------------+
 |  INHERITANTS' INTERFACE                                               |
 +----------------------------------------------------------------------*/
    protected Point     loc;      /* location in space */
    protected double    dir;      /* direction */
    protected Rectangle bounds;

    protected void updateBounds() {
        bounds.x = loc.x;
        bounds.y = loc.y;
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    /**
     * Constructor for object that doesn't need all the functionality
     * the other constructors provide.
     */
    public DrawableGameObject() {
        id = -1;
        loc = new Point();
        bounds = new Rectangle(0, 0, 3, 3);
    }

    /**
     * Constructs a new drawable game object, with the given initial
     * settings. Also updates the bounding box of the object.
     *
     * @param      id      the ID of this object.
     * @param      x       x-coordinate in the world.
     * @param      y       y-coordinate.
     * @param      dir     direction angle in radians.
     */
    public DrawableGameObject(short id, int x, int y, double dir) {
        setId(id);
        loc = new Point();
        bounds = new Rectangle(x, y, 3, 3);
        setLocation(x, y);
        setDirection(dir);
        updateBounds();
    }

    /**
     * Constructs a new drawable game object. This is used for objects that
     * are created on server demand, as the server sends the direction
     * encoded as a <CODE>short</CODE> to save some bandwidth.
     *
     * @param      id      the ID of this object.
     * @param      x       x-coordinate in the world.
     * @param      y       y-coordinate.
     * @param      dir     direction encoded as a <CODE>short</CODE>.
     */
    public DrawableGameObject(short id, int x, int y, short dir) {
        setId(id);
        loc = new Point();
        bounds = new Rectangle(x, y, 3, 3);
        setLocation(x, y);
        setDirectionFromShort(dir);
        updateBounds();
    }

    /**
     * Sets the ID of this object. The ID is used for identifying
     * objects when talking to the server.
     *
     * @param      id      the ID of this object.
     */
    public final void setId(short id) {
        this.id = id;
    }

    /**
     * Fetches the ID of this object. The ID is used for identifying
     * objects when talking to the server.
     *
     * @return     the ID of this object.
     */
    public final short getId() {
        return id;
    }

    /**
     * Sets the location of this object within the world.
     *
     * @param      loc     the location.
     */
    public synchronized void setLocation(Point loc) {
        this.loc.x = loc.x;
        this.loc.y = loc.y;
        updateBounds();
    }

    /**
     * Sets the location of this object within the world.
     *
     * @param      x       the x-coordinate.
     * @param      y       the y-coordinate.
     */
    public synchronized void setLocation(int x, int y) {
        loc.x = x;
        loc.y = y;
        updateBounds();
    }

    /**
     * @see        DrawableGameObject
     */
    public Point getLocation() {
        return loc;
    }

    /**
     * Sets the direction of this object as an angle in radians.
     *
     * @param      angle   the angle in radians.
     */
    public final void setDirection(double angle) {
        dir = angle;
        updateBounds();
    }

    /**
     * Returns the direction of this object in radians.
     *
     * @return     the direction.
     */
    public final double getDirection() {
        return dir;
    }

    /**
     * Sets the direction of this object, encoded as a short, as sent
     * from the game server.
     *
     * @param      angle   the angle encoded as a short.
     */
    public final void setDirectionFromShort(short angle) {
        dir = (double) angle * (2.0 * Math.PI) / 32000.0;
        updateBounds();
    }

    /**
     * Returns the direction of this object in radians, encoded in a short
     * for transfer to the game server.
     *
     * @return     the direction.
     */
    public final short getDirectionAsShort() {
        return (short) (dir * 32000.0 / (2.0 * Math.PI));
    }

    /**
     * Gets the current bounding box for this object.
     *
     * @return     the bouning box.
     */
    public final Rectangle getBounds() {
        return bounds;
    }

    /**
     * Draws the object in the given graphic context. The graphic context
     * controls an <CODE>Image</CODE> with the size of the entire world,
     * so the drawing routine doesn't need to displace it's coordinates.
     *
     * @param      g       the graphic context.
     */
    public abstract void draw(Graphics g);
}
