package objects;

import java.awt.*;

/**
 * This is a targeting bomb.
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
 *   <LI>Owner</LI>
 *   <LI>Color</LI>
 * </UL>
 */
public class Bomb
extends DrawableGameObject {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private static int diameter = 7;

    private Player  owner;
    private Color   col;
    private boolean flip;

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    /**
     * Constructs a new tareting bomb, with the given initial
     * settings. Also updates the bounding box of the object.
     *
     * @param      id      the ID of this object.
     * @param      owner   the player that fired this bomb.
     * @param      x       x-coordinate in the world.
     * @param      y       y-coordinate.
     * @param      dir     direction angle in radians.
     * @param      col     bomb color.
     */
    public Bomb(short id, Player owner, int x, int y, double dir, Color col) {
        super(id, x, y, dir);
        setOwner(owner);
        bounds.width = diameter;
        bounds.height = diameter;
        setColor(col);
    }

    /**
     * Constructs a new tareting bomb, with the given initial
     * settings. This is used for objects that
     * are created on server demand, as the server sends the direction
     * encoded as a <CODE>short</CODE> to save some bandwidth.
     *
     * @param      id      the ID of this object.
     * @param      owner   the player that fired this bomb.
     * @param      x       x-coordinate in the world.
     * @param      y       y-coordinate.
     * @param      dir     direction encoded as a <CODE>short</CODE>.
     * @param      col     bomb color.
     */
    public Bomb(short id, Player owner, int x, int y, short dir, Color col) {
        this(id, owner, x, y, 0.0, col);
        setDirectionFromShort(dir);
    }

    /**
     * Sets the owner of this bomb, that is the player who fired it.
     *
     * @param      owner   the player who owns the bomb.
     */
    public final void setOwner(Player owner) {
        this.owner = owner;
    }

    /**
     * Finds the player who fired this bomb.
     *
     * @return     the player who owns this bomb.
     */
    public final Player getOwner() {
        return owner;
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

    /* DrawableGameObject ***********************************************/
    /**
     * @see        DrawableGameObject
     */
    public final void draw(Graphics g) {
        int r;

        g.setColor(Color.yellow);
        r = (diameter - 2) / 2;
        if (flip) {
            flip = false;
            g.drawLine(loc.x - r - 1, loc.y, loc.x + r + 1, loc.y);
            g.drawLine(loc.x, loc.y - r - 1, loc.x, loc.y + r + 1);
        } else {
            flip = true;
            g.drawLine(loc.x - r - 1, loc.y - r - 1,
                       loc.x + r + 1, loc.y + r + 1);
            g.drawLine(loc.x - r - 1, loc.y + r + 1,
                       loc.x + r + 1, loc.y - r - 1);
        }
        g.setColor(col);
        g.fillOval(loc.x - r, loc.y - r, diameter - 2, diameter - 2);
    }
}
