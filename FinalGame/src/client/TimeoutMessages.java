package client;

import java.util.*;
final class TimeoutMessages {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private static final int maxMsg = 80;
    private static final int defaultDuration = 3000; /* milliseconds */

    private String[] msg;
    private long[]   timeout;
    private int      numMsg;

    private void remove(int n) {
        int q;

        --numMsg;
        for (q = n; q < numMsg; q++) {
            msg[q] = msg[q + 1];
            timeout[q] = timeout[q + 1];
        }
    }

    private void housekeep() {
        int  q;
        long now;

        now = System.currentTimeMillis();
        for (q = 0; q < numMsg; q++)
            if (timeout[q] < now) {
                remove(q);
                --q;
            }
    }

/*-----------------------------------------------------------------------+
 |  PACKAGE LOCAL PART                                                   |
 +----------------------------------------------------------------------*/
    final synchronized void add(int duration, String msg) {
        long now;

        housekeep();
        if (numMsg == maxMsg)
            remove(0);

        now = System.currentTimeMillis();
        this.msg[numMsg] = msg;
        timeout[numMsg] = now + duration;
        ++numMsg;
    }

    final synchronized void add(String msg) {
        add(defaultDuration, msg);
    }

    final synchronized String[] getAll() {
        housekeep();

        int      q;
        String[] ret = new String[numMsg];

        for (q = 0; q < numMsg; q++)
            ret[q] = msg[q];

        return ret;
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public TimeoutMessages() {
        msg     = new String[maxMsg];
        timeout = new long[maxMsg];
        numMsg  = 0;
    }
}
