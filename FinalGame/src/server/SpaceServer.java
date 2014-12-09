package server;


import main.*;

public final class SpaceServer {
/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public static void main(String args[]) {
        System.runFinalizersOnExit(true);
        (new MainServer()).main(args);
    }
}
