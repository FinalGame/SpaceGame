package netgame;

import java.util.*;
import java.io.*;
import java.net.*;

import no.shhsoft.net.*;

public abstract class TCPGameServerClientHandler
extends TCPCommunicator
implements Runnable {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    Thread thread;
    private boolean done;
    private TCPAsyncWriter writer;

/*-----------------------------------------------------------------------+
 |  INHERITANTS' INTERFACE                                               |
 +----------------------------------------------------------------------*/
    protected TCPGameServer server;

    protected abstract boolean readIncoming();

    protected void sendMessageNoFlush(Message m)
    throws IOException {
        writer.addMessage(m);
    }

    protected void flushOut()
    throws IOException {
        writer.sendAll();
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public TCPGameServerClientHandler(TCPGameServer server, Socket sock) {
        try {
            /* disable Nagle's algorithm (turn off time-based
             * buffering of output). this made a great speed-up on
             * Windows, but no visible difference on Unix, where
             * things ran fast from the start. */
            sock.setTcpNoDelay(true);
        } catch (SocketException e) {
        }
        this.server = server;
        setupSocketToUse(sock);
        writer = new TCPAsyncWriter(getOutputStream());
        done = false;
        thread = new Thread(this);
        thread.setDaemon(true);
    }

    public void start() {
        writer.start();
        thread.start();
    }

    public void stop() {
        writer.stop();
        done = true;
        thread.interrupt();
    }

    /* Runnable *********************************************************/
    public void run() {
        System.out.println((new Date()).toString()
                           + " connect from " + getPeerName());
        while (!done && readIncoming())
            ;
        if (!done)
            writer.stop();
        System.out.println((new Date()).toString() + " done " + getPeerName());
        server.removeHandler(this);
        close();
    }
}
