package no.shhsoft.awt.event;

import java.awt.event.KeyEvent;
public interface KeyPressedReleasedListener {
/*-----------------------------------------------------------------------+
|  PUBLIC INTERFACE                                                      |
+-----------------------------------------------------------------------*/
    public void keyPressed(KeyEvent event);

    public void keyReleased(KeyEvent event);
}
