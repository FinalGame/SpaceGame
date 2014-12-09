package objects;

import java.awt.*;

/**
 * This is an explosion object. An explosion has a current and a
 * maximum level. The levels are used to determine the diameter of
 * the drawn object. The level increases to the maximum, then
 * moves down to zero. The server provides the animation by sending
 * new levels at fixed intervals.
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
 *   <LI>Current level (size)</LI>
 *   <LI>Max level (size)</LI>
 * </UL>
 */
public class Explosion
extends DrawableGameObject {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private byte level;    /* how far has it got? */
    private byte maxLevel; /* how far can i get? */

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    /**
     * Constructs a new explosion, with the given initial
     * settings. Also updates the bounding box of the object.
     *
     * @param      id      the ID of this object.
     * @param      x       x-coordinate in the world.
     * @param      y       y-coordinate.
     * @param      level   the current level (relative to diameter).
     * @param      maxLevel
     *                     the maximum level.
     */
    public Explosion(short id, int x, int y, byte level, byte maxLevel) {
        super(id, x, y, 0.0);
        bounds.width = 1;
        bounds.height = 1;
        setLevel(level);
        setMaxLevel(maxLevel);
    }

    /**
     * Sets the current level of this explosion.
     *
     * @param      level   the level.
     */
    public final void setLevel(byte level) {
        this.level = level;
    }

    /**
     * Returns the current level of this explosion.
     *
     * @return     the level.
     */
    public final byte getLevel() {
        return level;
    }

    /**
     * Sets the maximum level of this explosion.
     *
     * @param      level   the level.
     */
    public final void setMaxLevel(byte level) {
        maxLevel = level;
    }

    /**
     * Returns the maximum level of this explosion.
     *
     * @return     the level.
     */
    public final byte getMaxLevel() {
        return maxLevel;
    }

    /* DrawableGameObject ***********************************************/
    /**
     * @see        DrawableGameObject
     */
    public final void draw(Graphics g) {
        int l, ml, width, width2;

        l  = level + 3;
        ml = maxLevel + 3;
        if (l <= ml / 2) {
            /* increasing size */
            width = l * 4;
        } else {
            /* decreasing size */
            width = (ml - l + 1) * 4;
        }
        width2 = width / 2;
        g.setColor(Color.orange);
        g.fillOval(loc.x - width2, loc.y - width2, width, width);
        width -= 7;
        if (width > 0) {
            width2 = width / 2;
            g.setColor(Color.red);
            g.fillOval(loc.x - width2, loc.y - width2, width, width);
        }
    }
}
