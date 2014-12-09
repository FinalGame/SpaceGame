package connection;

import java.io.*;

import main.*;
import server.*;


public final class Connect {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/

    private MainServer       main;
    private Server     server;

    private boolean startServer(int port) {
        stopServer();
        System.out.println("Conectado al puerto "+port);
        try {
            server = new Server(port);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
        server.start();
        return true;
    }

    private void stopServer() {
        if (server == null)
            return;

        server.stop();
        server = null;
    }

    private void doLocal() {
       int port=main.getLocalServerPort();
            main.setLocalServerPort(port);

            main.save();

            startServer(main.getLocalServerPort());
    }
/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public Connect(MainServer main) {
        this.main = main;
        doLocal();
    }
}
