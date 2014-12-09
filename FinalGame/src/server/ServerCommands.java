package server;

/**
 * Commands understood by game server.
 */
public final class ServerCommands {
/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public static final byte LOGIN        =  0;
    public static final byte SET_NAME     =  1;
    public static final byte SET_TURN     =  2;
    public static final byte SET_THRUST   =  3;
    public static final byte FIRE_PHASER  =  4;
    public static final byte FIRE_BOMB    =  5;
    public static final byte RESURRECT_ME =  6;
    public static final byte SAY          =  7;
}
