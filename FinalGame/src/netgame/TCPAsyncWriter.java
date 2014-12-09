package netgame;

import java.util.*;
import java.io.*;

import no.shhsoft.net.*;

public class TCPAsyncWriter
implements Runnable {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private Thread thread;
    private boolean done;
    private DataOutputStream out;
    private Vector messages;
    private Vector exceptions;

    private void throwExceptionIfAny()
    throws IOException {
        synchronized (exceptions) {
            if (exceptions.size() > 0) {
                IOException e;

                e = (IOException) exceptions.elementAt(0);
                exceptions.removeElementAt(0);
                throw e;
            }
        }
    }

/*-----------------------------------------------------------------------+
 |  INHERITANTS' INTERFACE                                               |
 +----------------------------------------------------------------------*/
    protected void addMessage(Message m)
    throws IOException {
        throwExceptionIfAny();
        messages.addElement(m);
    }

    protected void sendAll()
    throws IOException {
        throwExceptionIfAny();
        synchronized (messages) {
            messages.notify();
        }
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public TCPAsyncWriter(DataOutputStream out) {
        this.out = out;
        messages = new Vector();
        exceptions = new Vector();
        done = false;
        thread = new Thread(this);
        thread.setDaemon(true);
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
        Vector tosend;
        int q;
        Message m;
        byte[] buff;

        while (!done) {
            synchronized (messages) {
                while (!done && messages.size() == 0)
                    try {
                        messages.wait();
                    } catch (InterruptedException e) {
                    }
                if (done)
                    break;
                tosend = new Vector();
                while (messages.size() > 0) {
                    tosend.addElement(messages.elementAt(0));
                    messages.removeElementAt(0);
                }
            }
            for (q = 0; !done && q < tosend.size(); q++) {
                m = (Message) tosend.elementAt(q);
                buff = m.getBuffer();
                try {
                    out.writeShort(buff.length);
                    out.write(buff, 0, buff.length);
                } catch (IOException e) {
                    exceptions.addElement(e);
                }
            }
            try {
                out.flush();
            } catch (IOException e) {
                exceptions.addElement(e);
            }
        }
    }
}
