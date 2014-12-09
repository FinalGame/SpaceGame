package netgame;

import java.io.*;
import java.net.*;

public abstract class TCPGameClient
extends TCPCommunicator
implements Runnable {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    Thread thread;
    private boolean done;

/*-----------------------------------------------------------------------+
 |  INHERITANTS' INTERFACE                                               |
 +----------------------------------------------------------------------*/
    protected String host;
    protected int port;

    protected abstract boolean readIncoming();

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public TCPGameClient(String host, int port)
    throws IOException {
        this.host = host;
        this.port = port;

        done = false;
        thread = new Thread(this);

        setupSocketToUse(new Socket(host, port));
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        done = true;
        thread.interrupt();
    }

    /* Runnable *********************************************************/
    public void run() {
        if (!isConnected())
            return;

        while (!done && readIncoming())
            ;
        close();
    }
}
