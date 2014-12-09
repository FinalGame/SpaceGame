package objects;

import java.awt.*;

/**
 * This is a single phaser shot.
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
 *   <LI>Owner</LI>
 *   <LI>Color</LI>
 * </UL>
 */
public class Phaser
extends DrawableGameObject {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private Player owner;
    private Color  col;

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    /**
     * Constructs a new phaser "bullet", with the given initial
     * settings. Also updates the bounding box of the object.
     *
     * @param      id      the ID of this object.
     * @param      owner   the player that fired this phaser.
     * @param      x       x-coordinate in the world.
     * @param      y       y-coordinate.
     * @param      dir     direction angle in radians.
     * @param      col     bomb color.
     */
    public Phaser(short id, Player owner,
                  int x, int y, double dir, Color col) {
        super(id, x, y, dir);
        setOwner(owner);
        bounds.width = 3;
        bounds.height = 3;
        setColor(col);
    }

    /**
     * Constructs a new phaser "bullet", with the given initial
     * settings. This is used for objects that
     * are created on server demand, as the server sends the direction
     * encoded as a <CODE>short</CODE> to save some bandwidth.
     *
     * @param      id      the ID of this object.
     * @param      owner   the player that fired this phaser.
     * @param      x       x-coordinate in the world.
     * @param      y       y-coordinate.
     * @param      dir     direction encoded as a <CODE>short</CODE>.
     * @param      col     bomb color.
     */
    public Phaser(short id, Player owner,
                  int x, int y, short dir, Color col) {
        super(id, x, y, dir);
        setOwner(owner);
        bounds.width = 3;
        bounds.height = 3;
        setColor(col);
    }

    /**
     * Sets the owner of this phaser shot, that is the player who fired it.
     *
     * @param      owner   the player who owns the phaser.
     */
    public final void setOwner(Player owner) {
        this.owner = owner;
    }

    /**
     * Finds the player who fired this phaser shot.
     *
     * @return     the player who owns this phaser.
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
        g.setColor(col);
        g.drawLine(loc.x - 1, loc.y, loc.x + 1, loc.y);
        g.drawLine(loc.x, loc.y - 1, loc.x, loc.y + 1);
    }
}
