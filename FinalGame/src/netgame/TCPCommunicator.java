package netgame;

import java.util.*;
import java.io.*;
import java.net.*;

import no.shhsoft.net.*;
public class TCPCommunicator {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private Socket sock;
    private String peer;
    private DataOutputStream out;
    private DataInputStream in;

/*-----------------------------------------------------------------------+
 |  INHERITANTS' INTERFACE                                               |
 +----------------------------------------------------------------------*/
    protected void sendMessageNoFlush(Message m)
    throws IOException {
        byte[] buff;

        buff = m.getBuffer();
        synchronized (out) {
            out.writeShort(buff.length);
            out.write(buff, 0, buff.length);
        }
    }

    protected void sendMessage(Message m)
    throws IOException {
        synchronized (out) {
            sendMessageNoFlush(m);
            out.flush();
        }
    }

    protected void flushOut()
    throws IOException {
        synchronized (out) {
            out.flush();
        }
    }

    protected Message receiveMessage()
    throws IOException {
        Message m;
        byte[] buff;
        int len;

        synchronized (in) {
            len = in.readShort();
            buff = new byte[len];
            in.read(buff);
        }
        m = new Message();
        m.setBuffer(buff);
        return m;
    }

    protected final String getPeerName() {
        return peer;
    }

    protected final void setupSocketToUse(Socket s) {
        sock = s;
        peer = sock.getInetAddress() + ":" + sock.getPort();
        try {
            /* disable Nagle's algorithm (turn off time-based
             * buffering of output). this made a great speed-up on
             * Windows, but no visible difference on Unix, where
             * things ran fast from the start. */
            sock.setTcpNoDelay(true);
        } catch (SocketException e) {
        }
        try {
            BufferedInputStream  buffin;
            BufferedOutputStream buffout;

            buffin  = new BufferedInputStream(sock.getInputStream(), 8192);
            buffout = new BufferedOutputStream(sock.getOutputStream(), 8192);
            in  = new DataInputStream(buffin);
            out = new DataOutputStream(buffout);
        } catch (IOException e) {
            System.err.println("error setting up streams for "
                               + peer + ": " + e.getMessage());
            close();
            return;
        }
    }

    protected final boolean isConnected() {
        return sock != null;
    }

    protected final void close() {
        try {
            if (in != null) {
                in.close();
                in = null;
            }
            if (out != null) {
                out.close();
                out = null;
            }
            if (sock != null) {
                sock.close();
                sock = null;
            }
        } catch (IOException e) {
        }
    }

    protected DataOutputStream getOutputStream() {
        return out;
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public TCPCommunicator() {
    }
}
