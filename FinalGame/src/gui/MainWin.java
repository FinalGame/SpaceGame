package gui;

import java.io.*;
import java.awt.*;
import java.awt.event.*;

import main.*;
import server.*;
import client.*;
import objects.*;


public final class MainWin
extends Frame
implements MainFuncProvider,
           ActionListener, ComponentListener, WindowListener {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private static final String title = "Noche de estrellas";

    private Main       main;
    private Board      board;
    private World      world;
    private Server     server;
    private Client     client;
    private TextField  chatInput;
    private Point      locationOnScreen;

    private Menu     menuGame       = new Menu("Juego");
    private MenuItem menuGameIdent  = new MenuItem("Nombre");
    private MenuItem menuGameExit   = new MenuItem("Salir");

    private void stopServer() {
        if (server == null)
            return;

        server.stop();
        server = null;
    }

    private void connectToServer(String host, int port) {
        try {
            client = new Client(this, host, port, world, board,
                                main.getUserClientClassName());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return;
        }

        client.start();
        client.startUpdater();
        client.sendLogin(main.getMyName());
        client.flush();
    }

    private void disconnectFromServer() {
        if (client != null) {
            client.stopUpdater();
            client.stop();
            client = null;
        }
    }

    private MenuBar createMenuBar() {
        MenuBar ret;

        ret = new MenuBar();

        /* game menu */
        ret.add(menuGame);
        menuGame.addSeparator();
        menuGame.add(menuGameIdent);
        menuGameIdent.addActionListener(this);
        menuGame.addSeparator();
        menuGame.add(menuGameExit);
        menuGameExit.addActionListener(this);


        return ret;
    }

    private void setupWindow() {
        setLayout(new BorderLayout());
        addComponentListener(this);
        addWindowListener(this);

        setMenuBar(createMenuBar());

        world = new World();
        board = new Board(this, world,
                          main.getBoardWidth(), main.getBoardHeight());
        add(board, BorderLayout.CENTER);

        chatInput = new TextField();
        //      chatInput.setFont(new Font("Courier", Font.PLAIN, 12));
        chatInput.setBackground(Color.black);
        chatInput.setForeground(Color.green);
        chatInput.addActionListener(this);
        add(chatInput, BorderLayout.SOUTH);

        board.requestFocus();
    }

    private void doRemote() {
        RemoteServerDialog d;

        d = new RemoteServerDialog(this, main.getRemoteServerName(),
                                   main.getRemoteServerPort());
        if (!d.isCancelled()) {
            /* TODO: this disabling is temporary (I hope). */
            main.setRemoteServerName(d.getHost());
            main.setRemoteServerPort(d.getPort());

            main.save();

            connectToServer(main.getRemoteServerName(),
                            main.getRemoteServerPort());
        }
    }

    private void doIdentity() {
        IdentityDialog d;

        d = new IdentityDialog(this, main.getMyName());
        if (!d.isCancelled()) {
            main.setMyName(d.getName());

            main.save();

            if (client != null) {
                client.sendSetName(main.getMyName());
                client.flush();
            }
        }
    }

    private void doReadme() {
        new ReadmeDialog(this);
    }

    private void doAbout() {
        new AboutDialog(this);
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public MainWin(Main main) {
        this.main = main;

        locationOnScreen = new Point(main.getLocationX(), main.getLocationY());

        setupWindow();
        setTitle(title);
        setLocation(locationOnScreen);
        pack();
        show();
        doRemote();
    }

    /* MainFuncProvider *************************************************/
    /* called by client.Board */
    public void invokeChatLine() {
        if (chatInput != null)
            chatInput.requestFocus();
    }

    /* called by client.Client */
    public void doQuit() {
        main.save();

        disconnectFromServer();
        stopServer();

        dispose();

        /* TODO: I don't want exit here. I want to cleanly shut down
         * all threads, then the program will die automatically. */
        System.exit(0);
    }

    /* ComponentListener ************************************************/
    public void componentResized(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
        /* This doesn't work properly on my Linux box, probably due to
         * a bug in the Java implementation. The location reported is
         * 2 pixels to the left, and 24 pixels above the correct
         * location. */
        try {
            locationOnScreen = getLocationOnScreen();
            main.setLocationX(locationOnScreen.x);
            main.setLocationY(locationOnScreen.y);
        } catch (IllegalComponentStateException ex) {
        }
    }

    public void componentShown(ComponentEvent e) {
        board.requestFocus();
    }

    public void componentHidden(ComponentEvent e) {
    }

    /* WindowListener ***************************************************/
    public void windowOpened(WindowEvent e) {
        /* TODO/HERE: this doesn't work */
        if (main.getConnectImmediately())
            connectToServer(main.getRemoteServerName(),
                            main.getRemoteServerPort());
    }

    public void windowClosing(WindowEvent e) {
        doQuit();
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    /* ActionListener ***************************************************/
    public void actionPerformed(ActionEvent e) {
        Object src;

        src = e.getSource();
        if (src == menuGameIdent)
            doIdentity();
        else if (src == menuGameExit)
            doQuit();
        else if (e.getSource() == chatInput) {
            String msg;

            msg = chatInput.getText();
            if (msg.length() > 0 && client != null) {
                client.sendSay(msg);
                client.flush();
            }
            chatInput.setText("");
            board.requestFocus();
        }
    }
}
