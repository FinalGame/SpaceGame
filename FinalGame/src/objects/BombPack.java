package objects;

import java.awt.*;

/**
 * This is a bomb pack, any left over weapons from a killed player.
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
 *   <LI>Color</LI>
 * </UL>
 */
public class BombPack
extends DrawableGameObject {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private static int diameter = 7;

    private Color col;

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    /**
     * Constructs a new bomb pack, with the given initial
     * settings. Also updates the bounding box of the object.
     *
     * @param      id      the ID of this object.
     * @param      x       x-coordinate in the world.
     * @param      y       y-coordinate.
     * @param      dir     direction angle in radians.
     * @param      col     bomb color.
     */
    public BombPack(short id, int x, int y, double dir, Color col) {
        super(id, x, y, dir);
        bounds.width = 3;
        bounds.height = 3;
        setColor(col);
    }

    /**
     * Constructs a new bomb pack, with the given initial
     * settings. This is used for objects that
     * are created on server demand, as the server sends the direction
     * encoded as a <CODE>short</CODE> to save some bandwidth.
     *
     * @param      id      the ID of this object.
     * @param      x       x-coordinate in the world.
     * @param      y       y-coordinate.
     * @param      dir     direction encoded as a <CODE>short</CODE>.
     * @param      col     bomb color.
     */
    public BombPack(short id, int x, int y, short dir, Color col) {
        this(id, x, y, 0.0, col);
        setDirectionFromShort(dir);
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

    /* DrawableGameObjet ************************************************/
    /**
     * @see        DrawableGameObject
     */
    public final void draw(Graphics g) {
        int r, d2;

        d2 = diameter - 2;
        r = d2 / 2;
        g.setColor(col);
        g.fillOval(loc.x - d2, loc.y - d2, d2, d2);
        g.fillOval(loc.x, loc.y - d2, d2, d2);
        g.fillOval(loc.x - r, loc.y, d2, d2);
    }
}
