package main;

import java.io.*;

import server.*;
import client.*;
import gui.*;

public final class Main {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    public static final int MAX_BOARD_WIDTH = 600;
    public static final int MAX_BOARD_HEIGHT = 600;

    private static       int boardWidth = MAX_BOARD_WIDTH;
    private static       int boardHeight = MAX_BOARD_HEIGHT;

    private boolean      dedicatedServer = false;
    private String       myName = "Player";
    private String       remoteServerName = "localhost";
    private int          remoteServerPort = 9998;
    private int          localServerPort = 9998;
    private int          locX, locY;
    private String       userClientClassName;
    private boolean      connectImmediately = false;

    private String getSettingsFilename() {
        String filename;
        String sep;

        sep = System.getProperty("file.separator", "/");
        filename = System.getProperty("user.home", "");
        if (filename.length() > 0 && !filename.endsWith(sep))
            filename += sep;
        filename += ".shhspace";
        return filename;
    }

    private void saveSettings() {
        try {
            FileOutputStream fout;
            DataOutputStream out;

            fout = new FileOutputStream(getSettingsFilename());
            out = new DataOutputStream(fout);

            out.writeByte(2);  /* version of this file */
            out.writeUTF(myName);
            out.writeUTF(remoteServerName);
            out.writeInt(remoteServerPort);
            out.writeInt(localServerPort);
            out.writeInt(locX);
            out.writeInt(locY);

            out.flush();
            fout.close();
        } catch (IOException e) {
            /* nobody dies if this fails. */
        }
    }

    private void loadSettings() {
        try {
            FileInputStream fin;
            DataInputStream in;
            byte            version;

            fin = new FileInputStream(getSettingsFilename());
            in = new DataInputStream(fin);

            version = in.readByte();
            if (version > 2) {
                System.err.println("unknown version of settings file. "
                                   + "contents discarded.");
            } else {
                myName           = in.readUTF();
                remoteServerName = in.readUTF();
                remoteServerPort = in.readInt();
                localServerPort  = in.readInt();
                if (version > 1) {
                    locX = in.readInt();
                    locY = in.readInt();
                }
            }

            fin.close();
        } catch (IOException e) {
            /* nobody dies if this fails. */
        }
    }

    public int parseCommandLineArgument(String[] args, int n) {
        if (args[n].equals("-port") || args[n].equals("--port")) {
            if (n == args.length - 1) {
                System.err.println("missing parameter of -port");
                System.exit(1);
            }
            localServerPort = remoteServerPort
                = Integer.valueOf(args[++n]).intValue();
        } else if (args[n].equals("-host") || args[n].equals("--host")) {
            if (n == args.length - 1) {
                System.err.println("missing parameter of -host");
                System.exit(1);
            }
            remoteServerName = args[++n];
            connectImmediately = true;
        } else if (args[n].equals("-server") || args[n].equals("--server")) {
            dedicatedServer = true;
        } else if (args[n].equals("-hack") || args[n].equals("--hack")) {
            if (n == args.length - 1) {
                System.err.println("missing parameter of -hack");
                System.exit(1);
            }
            userClientClassName = args[++n];
        } else if (args[n].equals("-scale") || args[n].equals("--scale")) {
            if (n == args.length - 1) {
                System.err.println("missing parameter of -scale");
                System.exit(1);
            }
            int scale;

            scale = Integer.valueOf(args[++n]).intValue();
            if (scale < 30 || scale > 100) {
                System.err.println("argument to -scale must "
                                   + "be >= 30 and <= 100");
                System.exit(1);
            }
            boardWidth = (boardWidth * scale) / 100;
            boardHeight = (boardHeight * scale) / 100;
        } else if (args[n].equals("-help") || args[n].equals("--help")) {
            System.out.println(  "usage: java SpaceGame "
                                 + "[-host hostname] "
                                 + "[-port port] "
                                 + "[-server] "
                                 + "[-scale resize-percent] "
                                 + "[-hack class-name]");
            System.exit(0);
        } else {
            System.err.println("unknown argument `" + args[n] + "'");
            System.exit(1);
        }
        return n + 1;
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public Main() {
        loadSettings();
    }

    public void main(String args[]) {
        int q;

        for (q = 0; q < args.length; )
            q = parseCommandLineArgument(args, q);

        if (dedicatedServer) {
            Server server;

            try {
                server = new Server(localServerPort);
                server.start();
                System.out.println("SpaceGame: dedicated server running on "
                                   + "port " + localServerPort);
            } catch (IOException e) {
                System.err.println("SpaceGame: failed to set up server on "
                                   + "port " + localServerPort + ": "
                                   + e.getMessage());
                System.exit(1);;
            }
        } else {
            MainWin win;

            win = new MainWin(this);
            win.pack();
            win.show();
        }
    }

    public void save() {
        saveSettings();
    }

    public void setLocationX(int x) {
        locX = x;
    }

    public int getLocationX() {
        return locX;
    }

    public void setLocationY(int y) {
        locY = y;
    }

    public int getLocationY() {
        return locY;
    }

    public int getBoardWidth() {
        return boardWidth;
    }

    public int getBoardHeight() {
        return boardHeight;
    }

    public void setMyName(String name) {
        this.myName = name;
    }

    public String getMyName() {
        return myName;
    }

    public void setRemoteServerName(String name) {
        this.remoteServerName = name;
    }

    public String getRemoteServerName() {
        return remoteServerName;
    }

    public void setRemoteServerPort(int port) {
        remoteServerPort = port;
    }

    public int getRemoteServerPort() {
        return remoteServerPort;
    }

    public void setLocalServerPort(int port) {
        localServerPort = port;
    }

    public int getLocalServerPort() {
        return localServerPort;
    }

    public String getUserClientClassName() {
        return userClientClassName;
    }

    public boolean getConnectImmediately() {
        return connectImmediately;
    }
}
