package netgame;

import java.io.*;
import java.net.*;
import java.util.*;

public abstract class TCPGameServer
implements Runnable {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    Thread thread;
    private boolean done;
    private Vector  handlers;

/*-----------------------------------------------------------------------+
 |  INHERITANTS' INTERFACE                                               |
 +----------------------------------------------------------------------*/
    protected int          port;
    protected ServerSocket sock;

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public TCPGameServer(int port)
    throws IOException {
        this.port = port;
        done = false;
        thread = new Thread(this);
        handlers = new Vector();
        sock = new ServerSocket(port);
    }

    public void removeHandler(TCPGameServerClientHandler handler) {
        handlers.removeElement(handler);
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        done = true;
        thread.interrupt();
    }

    public abstract TCPGameServerClientHandler newClient(Socket sock);

    /* Runable **********************************************************/
    public void run() {
        Socket                     cliSock;
        TCPGameServerClientHandler cliHandler;

        while (!done) {
            try {
                cliSock = sock.accept();
            } catch (IOException e) {
                System.err.println("server: accept failed: " + e.getMessage());
                continue;
            }
            if (done)
                break;
            cliHandler = newClient(cliSock);
            handlers.addElement(cliHandler);
            cliHandler.start();
        }
        try {
            sock.close();
        } catch (IOException e) {
        }

        synchronized (handlers) {
            int q;
            TCPGameServerClientHandler handler;

            for (q = 0; q < handlers.size(); q++) {
                handler = (TCPGameServerClientHandler) handlers.elementAt(q);
                handler.stop();
            }
        }
    }
}
