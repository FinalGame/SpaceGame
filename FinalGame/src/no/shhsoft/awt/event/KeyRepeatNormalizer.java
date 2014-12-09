package no.shhsoft.awt.event;

import java.util.*;
import java.lang.reflect.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Note that <code>KeyTyped</code> events are discarded.
 */
public final class KeyRepeatNormalizer
implements KeyListener, Runnable {
/*-----------------------------------------------------------------------+
|  PRIVATE PART                                                          |
+-----------------------------------------------------------------------*/
    /** max number of milliseconds between a KeyReleased and a
     *  KeyPressed to consider the key to be auto repeating. */
    private long msDiffUpDown = 20;  /* milliseconds */
    private boolean[] pressed;
    private boolean[] pressedRecently;
    private Stack[] releasedEvents;
    private int[] keyIndexesWaiting;
    private int numKeyIndexesWaiting;
    private Thread thread;
    private boolean threadStop;
    private int[] vkCodeLookup;
    private EventPusher pusher;
    private int[] vkCodes = {
        KeyEvent.VK_ENTER, KeyEvent.VK_BACK_SPACE, KeyEvent.VK_TAB,
        KeyEvent.VK_CANCEL, KeyEvent.VK_CLEAR, KeyEvent.VK_SHIFT,
        KeyEvent.VK_CONTROL, KeyEvent.VK_ALT, KeyEvent.VK_PAUSE,
        KeyEvent.VK_CAPS_LOCK, KeyEvent.VK_ESCAPE, KeyEvent.VK_SPACE,
        KeyEvent.VK_PAGE_UP, KeyEvent.VK_PAGE_DOWN, KeyEvent.VK_END,
        KeyEvent.VK_HOME, KeyEvent.VK_LEFT, KeyEvent.VK_UP,
        KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_COMMA,
        KeyEvent.VK_MINUS, KeyEvent.VK_PERIOD, KeyEvent.VK_SLASH,
        KeyEvent.VK_0, KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3,
        KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7,
        KeyEvent.VK_8, KeyEvent.VK_9, KeyEvent.VK_SEMICOLON,
        KeyEvent.VK_EQUALS, KeyEvent.VK_A, KeyEvent.VK_B, KeyEvent.VK_C,
        KeyEvent.VK_D, KeyEvent.VK_E, KeyEvent.VK_F, KeyEvent.VK_G,
        KeyEvent.VK_H, KeyEvent.VK_I, KeyEvent.VK_J, KeyEvent.VK_K,
        KeyEvent.VK_L, KeyEvent.VK_M, KeyEvent.VK_N, KeyEvent.VK_O,
        KeyEvent.VK_P, KeyEvent.VK_Q, KeyEvent.VK_R, KeyEvent.VK_S,
        KeyEvent.VK_T, KeyEvent.VK_U, KeyEvent.VK_V, KeyEvent.VK_W,
        KeyEvent.VK_X, KeyEvent.VK_Y, KeyEvent.VK_Z, KeyEvent.VK_OPEN_BRACKET,
        KeyEvent.VK_BACK_SLASH, KeyEvent.VK_CLOSE_BRACKET,
        KeyEvent.VK_NUMPAD0, KeyEvent.VK_NUMPAD1, KeyEvent.VK_NUMPAD2,
        KeyEvent.VK_NUMPAD3, KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD5,
        KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD7, KeyEvent.VK_NUMPAD8,
        KeyEvent.VK_NUMPAD9, KeyEvent.VK_MULTIPLY, KeyEvent.VK_ADD,
        KeyEvent.VK_SEPARATER, KeyEvent.VK_SUBTRACT, KeyEvent.VK_DECIMAL,
        KeyEvent.VK_DIVIDE, KeyEvent.VK_DELETE, KeyEvent.VK_NUM_LOCK,
        KeyEvent.VK_SCROLL_LOCK, KeyEvent.VK_F1, KeyEvent.VK_F2,
        KeyEvent.VK_F3, KeyEvent.VK_F4, KeyEvent.VK_F5, KeyEvent.VK_F6,
        KeyEvent.VK_F7, KeyEvent.VK_F8, KeyEvent.VK_F9, KeyEvent.VK_F10,
        KeyEvent.VK_F11, KeyEvent.VK_F12, KeyEvent.VK_F13, KeyEvent.VK_F14,
        KeyEvent.VK_F15, KeyEvent.VK_F16, KeyEvent.VK_F17, KeyEvent.VK_F18,
        KeyEvent.VK_F19, KeyEvent.VK_F20, KeyEvent.VK_F21, KeyEvent.VK_F22,
        KeyEvent.VK_F23, KeyEvent.VK_F24, KeyEvent.VK_PRINTSCREEN,
        KeyEvent.VK_INSERT, KeyEvent.VK_HELP, KeyEvent.VK_META,
        KeyEvent.VK_BACK_QUOTE, KeyEvent.VK_QUOTE, KeyEvent.VK_KP_UP,
        KeyEvent.VK_KP_DOWN, KeyEvent.VK_KP_LEFT, KeyEvent.VK_KP_RIGHT,
        KeyEvent.VK_DEAD_GRAVE, KeyEvent.VK_DEAD_ACUTE,
        KeyEvent.VK_DEAD_CIRCUMFLEX, KeyEvent.VK_DEAD_TILDE,
        KeyEvent.VK_DEAD_MACRON, KeyEvent.VK_DEAD_BREVE,
        KeyEvent.VK_DEAD_ABOVEDOT, KeyEvent.VK_DEAD_DIAERESIS,
        KeyEvent.VK_DEAD_ABOVERING, KeyEvent.VK_DEAD_DOUBLEACUTE,
        KeyEvent.VK_DEAD_CARON, KeyEvent.VK_DEAD_CEDILLA,
        KeyEvent.VK_DEAD_OGONEK, KeyEvent.VK_DEAD_IOTA,
        KeyEvent.VK_DEAD_VOICED_SOUND, KeyEvent.VK_DEAD_SEMIVOICED_SOUND,
        KeyEvent.VK_AMPERSAND, KeyEvent.VK_ASTERISK, KeyEvent.VK_QUOTEDBL,
        KeyEvent.VK_LESS, KeyEvent.VK_GREATER, KeyEvent.VK_BRACELEFT,
        KeyEvent.VK_BRACERIGHT, KeyEvent.VK_AT, KeyEvent.VK_COLON,
        KeyEvent.VK_CIRCUMFLEX, KeyEvent.VK_DOLLAR, KeyEvent.VK_EURO_SIGN,
        KeyEvent.VK_EXCLAMATION_MARK, KeyEvent.VK_INVERTED_EXCLAMATION_MARK,
        KeyEvent.VK_LEFT_PARENTHESIS, KeyEvent.VK_NUMBER_SIGN,
        KeyEvent.VK_PLUS, KeyEvent.VK_RIGHT_PARENTHESIS,
        KeyEvent.VK_UNDERSCORE, KeyEvent.VK_FINAL, KeyEvent.VK_CONVERT,
        KeyEvent.VK_NONCONVERT, KeyEvent.VK_ACCEPT, KeyEvent.VK_MODECHANGE,
        KeyEvent.VK_KANA, KeyEvent.VK_KANJI, KeyEvent.VK_ALPHANUMERIC,
        KeyEvent.VK_KATAKANA, KeyEvent.VK_HIRAGANA, KeyEvent.VK_FULL_WIDTH,
        KeyEvent.VK_HALF_WIDTH, KeyEvent.VK_ROMAN_CHARACTERS,
        KeyEvent.VK_ALL_CANDIDATES, KeyEvent.VK_PREVIOUS_CANDIDATE,
        KeyEvent.VK_CODE_INPUT, KeyEvent.VK_JAPANESE_KATAKANA,
        KeyEvent.VK_JAPANESE_HIRAGANA, KeyEvent.VK_JAPANESE_ROMAN,
        KeyEvent.VK_KANA_LOCK, KeyEvent.VK_INPUT_METHOD_ON_OFF,
        KeyEvent.VK_CUT, KeyEvent.VK_COPY, KeyEvent.VK_PASTE,
        KeyEvent.VK_UNDO, KeyEvent.VK_AGAIN, KeyEvent.VK_FIND,
        KeyEvent.VK_PROPS, KeyEvent.VK_STOP, KeyEvent.VK_COMPOSE,
        KeyEvent.VK_ALT_GRAPH, KeyEvent.VK_UNDEFINED
    };

    private void appendWaitingKey(int vkIdx) {
        synchronized (keyIndexesWaiting) {
            keyIndexesWaiting[numKeyIndexesWaiting++] = vkIdx;
            if (numKeyIndexesWaiting == 1)
                keyIndexesWaiting.notify();
        }
    }

    private void removeFirstWaitingKey() {
        int q;

        synchronized (keyIndexesWaiting) {
            for (q = 0; q < numKeyIndexesWaiting - 1; q++)
                keyIndexesWaiting[q] = keyIndexesWaiting[q + 1];
            --numKeyIndexesWaiting;
        }
    }

    private void createVKCodeLookup() {
        int q, w, idx, val, n;
        String name;

        /* find number of virtual key codes. */
        n = vkCodes.length;

        /* build a sorted lookup table for all key codes. */
        vkCodeLookup = new int[n];
        idx = 0;
        for (q = vkCodes.length - 1; q >= 0; q--) {
            val = vkCodes[q];
            for (w = 0; w < idx; w++)
                if (vkCodeLookup[w] >= val)
                    break;
            if (w < idx)
                System.arraycopy(vkCodeLookup, w,
                                 vkCodeLookup, w + 1, idx - w);
            vkCodeLookup[w] = val;
            ++idx;
        }
    }

    private int lookupVKCodeIdx(int code) {
        int from, to, mid, val;

        /* binary search to map a key code to an array index. */
        from = 0;
        to = vkCodeLookup.length - 1;
        while (from <= to) {
            mid = from + (to - from) / 2;
            val = vkCodeLookup[mid];
            if (val == code)
                return mid;
            if (val < code)
                from = mid + 1;
            else /* val > code */
                to = mid - 1;
        }
        return -1;
    }

    private class EventPusher
    implements Runnable {
        KeyPressedReleasedListener handler;
        Stack events;
        boolean threadStop;
        Thread thread;

        EventPusher(KeyPressedReleasedListener handler) {
            this.handler = handler;
            events = new Stack();
            start();
        }

        void pushEvent(KeyEvent event) {
            synchronized (events) {
                events.push(event);
                events.notify();
            }
        }

        public void start() {
            stop();
            threadStop = false;
            thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

        void stop() {
            if (thread == null || threadStop)
                return;
            threadStop = true;
            thread.interrupt();
            thread = null;
        }

        /* Runnable *****************************************************/
        public void run() {
            KeyEvent event;

            while (!threadStop) {
                try {
                    synchronized (events) {
                        while (events.empty())
                            events.wait();
                        event = (KeyEvent) events.pop();
                    }
                    switch (event.getID()) {
                      case KeyEvent.KEY_PRESSED:
                        handler.keyPressed(event);
                        break;
                      case KeyEvent.KEY_RELEASED:
                        handler.keyReleased(event);
                        break;
                    }
                } catch (InterruptedException e) {
                    System.err.println("interrupted");
                }
            }
        }
    }

/*-----------------------------------------------------------------------+
|  PUBLIC INTERFACE                                                      |
+-----------------------------------------------------------------------*/
    public KeyRepeatNormalizer(KeyPressedReleasedListener l) {
        pusher = new EventPusher(l);

        createVKCodeLookup();

        pressed = new boolean[vkCodeLookup.length];
        pressedRecently = new boolean[vkCodeLookup.length];
        releasedEvents = new Stack[vkCodeLookup.length];
        keyIndexesWaiting = new int[vkCodeLookup.length];

        start();
    }

    public void start() {
        int q;

        stop();
        for (q = pressed.length - 1; q >= 0; q--) {
            pressed[q] = false;
            pressedRecently[q] = false;
            releasedEvents[q] = new Stack();
        }
        numKeyIndexesWaiting = 0;

        threadStop = false;
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        if (thread == null || threadStop)
            return;
        threadStop = true;
        thread.interrupt();
        thread = null;
    }

    /* Runnable *********************************************************/
    public void run() {
        int vkIdx;
        long now, delay;
        KeyEvent releasedEvent;

        while (!threadStop) {
            try {
                synchronized (keyIndexesWaiting) {
                    while (numKeyIndexesWaiting == 0)
                        keyIndexesWaiting.wait();
                    vkIdx = keyIndexesWaiting[0];
                    removeFirstWaitingKey();
                }
                releasedEvent = (KeyEvent) releasedEvents[vkIdx].pop();
                if (pressed[vkIdx]) {
                    now = System.currentTimeMillis();
                    delay = releasedEvent.getWhen() + msDiffUpDown - now;
                    if (delay > 0)
                        Thread.sleep(delay);
                    synchronized (pressedRecently) {
                        if (!pressedRecently[vkIdx]) {
                            pusher.pushEvent(releasedEvent);
                            pressed[vkIdx] = false;
                        }
                    }
                }
            } catch (InterruptedException e) {
                System.err.println("interrupted");
            }
        }
    }

    /* KeyListener ******************************************************/
    public void keyTyped(KeyEvent e) {
        /* only keys producing ASCII (unicode) output. */
        /* ignored */
    }

private long lastWhen, when;

    public void keyPressed(KeyEvent e) {
        /* all keys.  called repeatedly when keys are repeating. */
        int code;
        int vkIdx;

        code = e.getKeyCode();
when = e.getWhen();
//System.out.println("down " + code + " after " + (when - lastWhen));
lastWhen = when;
        if ((vkIdx = lookupVKCodeIdx(code)) < 0)
            return;
        synchronized (pressed) {
            synchronized (pressedRecently) {
                pressedRecently[vkIdx] = true;
                if (pressed[vkIdx])
                    return;
                pressed[vkIdx] = true;
            }
        }
        pusher.pushEvent(e);
    }

    public void keyReleased(KeyEvent e) {
        /* all keys.  on Windows, this is called only when keys are
           released.  on X11, called repeatedly when keys are
           repeating. */
        int code;
        int vkIdx;

        code = e.getKeyCode();
when = e.getWhen();
//System.out.println("up   " + code + " after " + (when - lastWhen));
lastWhen = when;
        if ((vkIdx = lookupVKCodeIdx(code)) < 0)
            return;
        synchronized (pressed) {
            if (!pressed[vkIdx])
                return;
        }
        synchronized (pressedRecently) {
            pressedRecently[vkIdx] = false;
        }
        releasedEvents[vkIdx].push(e);
        appendWaitingKey(vkIdx);
    }
}
