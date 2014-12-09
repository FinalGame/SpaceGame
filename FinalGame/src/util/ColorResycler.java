package util;

import java.awt.*;

/**
 * A pool of bright colors, and methods to retrieve and resycle the
 * colors so that the colors that are in use are quite different.
 */
public final class ColorResycler {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private static final int numDifferentColors = 10;

    private Color[] color;
    private int[]   count;

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public ColorResycler() {
        int   q;
        float hue;

        color = new Color[numDifferentColors];
        count = new int[numDifferentColors];

        for (hue = 0.0f, q = 0; q < numDifferentColors; q++) {
            count[q] = 0;
            color[q] = Color.getHSBColor(hue, 1.0f, 1.0f);
            if ((hue += 0.7) >= 1.0)
                hue -= 1.0;
        }
    }

    public Color getColor() {
        int q, lowestIndex;

        lowestIndex = 0;
        for (q = 1; q < numDifferentColors; q++)
            if (count[q] < count[lowestIndex])
                lowestIndex = q;
            else if (count[q] == count[lowestIndex] && Math.random() < 0.5)
                lowestIndex = q;
        ++count[lowestIndex];
        return color[lowestIndex];
    }

    public void resycleColor(Color col) {
        int q;

        for (q = 0; q < numDifferentColors; q++)
            if (color[q].equals(col)) {
                if (count[q] > 0)
                    --count[q];
                break;
            }
    }
}
