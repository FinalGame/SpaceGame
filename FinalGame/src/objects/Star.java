package objects;

import java.awt.*;

/**
 * A star. For user defined clients, stars are probably unimportant.
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
public final class Star
extends DrawableGameObject {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private Color col;

    private final void setRandomColor() {
        float hue, saturation, value;

        hue        = (float) (Math.random() * 0.1875); /* red to yellow */
        saturation = (float) (0.5 + Math.random() * 0.5);
        value      = (float) (0.5 + Math.random() * 0.5);
        col        = Color.getHSBColor(hue, saturation, value);
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    /**
     *  Constructs a new star with a random color, and no position.
     */
    public Star() {
        bounds.width = 1;
        bounds.height = 1;
        setRandomColor();
    }

    /**
     * Constructs a new star with random color, and the given position.
     *
     * @param      x       the x-coordinate.
     * @param      y       the y-coordinate.
     */
    public Star(int x, int y) {
        this();
        setLocation(x, y);
    }

    /**
     * Constructs a new star with the given position and color.
     *
     * @param      x       the x-coordinate.
     * @param      y       the y-coordinate.
     * @param      col     the color.
     */
    public Star(int x, int y, Color col) {
        this(x, y);
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
        g.drawLine(loc.x, loc.y, loc.x, loc.y);
    }
}
