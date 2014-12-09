
package util;

import java.net.*;
import java.awt.*;

public class ImageCanvas
extends Canvas {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private int   width;
    private int   height;
    private Image img;

    private void waitForImage(Image img) {
        MediaTracker mt;

        mt = new MediaTracker(this);
        mt.addImage(img, 0);
        try {
            mt.waitForID(0);
        } catch (InterruptedException e) {
        }
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public ImageCanvas(Image img, int width, int height) {
        this.width = width;
        this.height = height;
        this.img = img;
        waitForImage(img);
    }

    public ImageCanvas(String resource, int width, int height) {
        URL url;

        if ((url = getClass().getResource(resource)) == null) {
            System.err.println("unable to get resource `" + resource + "'");
            return;
        }
        this.width = width;
        this.height = height;
        img = Toolkit.getDefaultToolkit().getImage(url);
        waitForImage(img);
    }

    public ImageCanvas(URL url, int width, int height) {
        this.width = width;
        this.height = height;
        img = Toolkit.getDefaultToolkit().getImage(url);
        waitForImage(img);
    }

    /* Canvas ***********************************************************/
    public void paint(Graphics g) {
        if (img != null)
            g.drawImage(img, 0, 0, this);
    }

    public Dimension getMinimumSize() {
        return new Dimension(width, height);
    }

    public Dimension getPreferredSize() {
        return getMinimumSize();
    }
}
