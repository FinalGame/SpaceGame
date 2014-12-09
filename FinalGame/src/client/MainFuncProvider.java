package client;

/**
 * Provides the functions in MainWin.java that are called from Client.java
 * and Board.java, so an applet implementation may implement them without
 * dealing with MainWin.java.
 */
public interface MainFuncProvider {
/*-----------------------------------------------------------------------+
|  PUBLIC INTERFACE                                                      |
+-----------------------------------------------------------------------*/
    public void doQuit();
    public void invokeChatLine();
}
