package client;

import main.*;

public final class ClientSpace {
/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public static void main(String args[]) {
        System.runFinalizersOnExit(true);
        (new Main()).main(args);
    }
}
