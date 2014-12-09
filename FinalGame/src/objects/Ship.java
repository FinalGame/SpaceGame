package objects;

import java.awt.*;

/**
 * A ship. Note that most of the information you need may be available
 * in the <CODE>Player</CODE> object of the owner of this ship.
 * <P>
 *
 * If you write a user defined client, you should not try change this
 * object by calling any of it's setter methods, as it will make
 * your view of the game different from all the other players' views.
 * <P>
 *
 * Also note that for clients, not all getter methods return usable
 * results, as the server doesn't send everything across. The following
 * information is, however, updated and usable for this class:
 * <UL>
 *   <LI>Location</LI>
 *   <LI>Direction</LI>
 *   <LI>Color</LI>
 * </UL>
 *
 */
public final class Ship
extends DrawableGameObject {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private Color   col;
    private Polygon shape;
    private Polygon current;  /* shape with current rotation */
    private Player  owner;
    private int     textDelta;
    private int     phaserOffset;
    private Color   nameColor;

    private final void updateCurrent() {
        int    q, newx, newy;
        double co, si;

        co = Math.cos(-dir);
        si = Math.sin(-dir);
        current = new Polygon();
        for (q = 0; q < shape.npoints; q++) {
            newx = (int) (shape.xpoints[q] * co - shape.ypoints[q] * si + 0.5);
            newy = (int) (shape.xpoints[q] * si + shape.ypoints[q] * co + 0.5);
            current.addPoint(loc.x + newx, loc.y + newy);
        }
        bounds = current.getBounds();
    }

/*-----------------------------------------------------------------------+
 |  INHERITANTS' INTERFACE                                               |
 +----------------------------------------------------------------------*/
    protected final void updateBounds() {
        updateCurrent();
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    /**
     * Constructs a new ship with the given owner.
     * Also updates the bounding box of the object.
     *
     * @param      owner   the player who controls this ship.
     */
    public Ship(Player owner) {
        this.owner = owner;

        nameColor = new Color(100, 100, 255);
        shape = new Polygon();

        /* traditional asteroids shape 
        shape.addPoint(13, 0);
        shape.addPoint(-10, 7);
        shape.addPoint(-10, -7);
        textDelta = 17;
        phaserOffset = 13;*/

        /* airplane shape */
        shape.addPoint(13, 0);
        shape.addPoint(11, 1);
        shape.addPoint(3, 1);
        shape.addPoint(0, 8);
        shape.addPoint(-5, 10);
        shape.addPoint(-4, 3);
        shape.addPoint(-10, 3);
        shape.addPoint(-10, -3);
        shape.addPoint(-4, -3);
        shape.addPoint(-5, -10);
        shape.addPoint(0, -8);
        shape.addPoint(3, -1);
        shape.addPoint(11, -2);
        textDelta = 17;
        phaserOffset = 13;

        setColor(Color.red);
        updateCurrent();
    }

    /**
     * Sets the main color used when drawing this object.
     *
     * @param      col     the color.
     */
    public final void setColor(Color col) {
        this.col = col;
    }

    /**
     * Returns the main color used for drawing this object.
     *
     * @return     the color.
     */
    public final Color getColor() {
        return col;
    }

    /**
     * Returns the distance from the center of the ship to the cannon.
     * Used by the server when creating phasers and bombs fired by
     * this ship.
     *
     * @return     the offset in pixels.
     */
    public final int getPhaserOffset() {
        return phaserOffset;
    }

    /**
     * Checks if a point is within this ship.
     *
     * @param      x       the x-coordinate.
     * @param      y       the y-coordinate.
     * @return     <CODE>true</CODE> if the point is considered to
     *             be inside the ship, <CODE>false</CODE> otherwise.
     */
    public final boolean isHitBy(int x, int y) {
        return (bounds.contains(x, y) && current.contains(x, y));
    }

    /**
     * Checks if a point is within this ship.
     *
     * @param      p       the point.
     * @return     <CODE>true</CODE> if the point is considered to
     *             be inside the ship, <CODE>false</CODE> otherwise.
     */
    public final boolean isHitBy(Point p) {
        return isHitBy(p.x, p.y);
    }

    /* DrawableGameObject ***********************************************/
    /**
     * @see        DrawableGameObject
     */
    public final void draw(Graphics g) {
        int       q;
        Polygon   p = new Polygon();
        Rectangle rect;
        String    name;
        int       charWidth;

        g.setColor(col);
        g.fillPolygon(current);

        g.setColor(nameColor);
        name = owner.getName();
        charWidth = g.getFontMetrics().charWidth('n');
        g.drawString(owner.getName(),
                     loc.x - (charWidth * name.length()) / 2,
                     loc.y - textDelta);
    }
}
