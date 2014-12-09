package server;

import java.io.*;
import java.net.*;
import java.awt.*;

import util.*;
import netgame.*;
import objects.*;

public final class Server
extends TCPGameServer {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private World   world;
    private Updater updater;

    private void setupWorld() {
        double numPixels;
        int    numStars;
        Star   star;

        numPixels = (double) world.getWidth() * (double) world.getHeight();
        numStars = (int) (numPixels / 35000.0);
        for (int q = 0; q < numStars; q++) {
            star = new Star((int) (Math.random() * (world.getWidth() - 1)),
                            (int) (Math.random() * (world.getHeight() - 1)));
            world.addStar(star);
        }
    }

/*-----------------------------------------------------------------------+
 |  PACKAGE LOCAL PART                                                   |
 +----------------------------------------------------------------------*/
    ColorResycler colorResycler;

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public Server(int port)
    throws IOException {
        super(port);
        colorResycler = new ColorResycler();
        world = new World();
        setupWorld();
        updater = new Updater(this, world);
        updater.start();
    }

    public void stop() {
        updater.stop();
        updater = null;
        super.stop();
    }

    public final synchronized void sendNewPlayer(UpdatingPlayer p) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            handler = player.getClientHandler();
            handler.sendNewPlayer(p);
        }
    }

    public final synchronized void sendRemovePlayer(UpdatingPlayer p) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            handler = player.getClientHandler();
            handler.sendRemovePlayer(p);
        }
    }

    public final synchronized void sendSetPlayerName(UpdatingPlayer p) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            handler = player.getClientHandler();
            handler.sendSetPlayerName(p);
        }
    }

    public final synchronized void sendSetPlayerPosition(UpdatingPlayer p) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            handler = player.getClientHandler();
            handler.sendSetPlayerPosition(p);
        }
    }

    public final synchronized void sendSetPlayerScore(UpdatingPlayer p) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            handler = player.getClientHandler();
            handler.sendSetPlayerScore(p);
        }
    }

    public final synchronized void sendPlayerHit(UpdatingPlayer p,
                                                 UpdatingPlayer hitter,
                                                 byte weapon) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            handler = player.getClientHandler();
            handler.sendPlayerHit(p, hitter, weapon);
        }
    }

    public final synchronized void sendPlayerDies(UpdatingPlayer p,
                                                  UpdatingPlayer killer,
                                                  byte weapon) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            handler = player.getClientHandler();
            handler.sendPlayerDies(p, killer, weapon);
        }
    }

    public final synchronized void sendPlayerResurrects(UpdatingPlayer p) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            handler = player.getClientHandler();
            handler.sendPlayerResurrects(p);
        }
    }

    public final synchronized void sendNewPhaser(UpdatingPhaser p) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            handler = player.getClientHandler();
            handler.sendNewPhaser(p);
        }
    }

    public final synchronized void sendRemovePhaser(UpdatingPhaser p) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            handler = player.getClientHandler();
            handler.sendRemovePhaser(p);
        }
    }

    public final synchronized void sendSetPhaserPosition(UpdatingPhaser p) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            if (!player.isInView(p))
                continue;
            handler = player.getClientHandler();
            handler.sendSetPhaserPosition(p);
        }
    }

    public final synchronized void sendNewBomb(UpdatingBomb b) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            handler = player.getClientHandler();
            handler.sendNewBomb(b);
        }
    }

    public final synchronized void sendRemoveBomb(UpdatingBomb b) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            handler = player.getClientHandler();
            handler.sendRemoveBomb(b);
        }
    }

    public final synchronized void sendSetBombPosition(UpdatingBomb b) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            if (!player.isInView(b))
                continue;
            handler = player.getClientHandler();
            handler.sendSetBombPosition(b);
        }
    }

    public final synchronized void sendNewBombPack(UpdatingBombPack bp) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            handler = player.getClientHandler();
            handler.sendNewBombPack(bp);
        }
    }

    public final synchronized void sendRemoveBombPack(UpdatingBombPack bp) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            handler = player.getClientHandler();
            handler.sendRemoveBombPack(bp);
        }
    }

    public final synchronized void
    sendSetBombPackPosition(UpdatingBombPack bp) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            handler = player.getClientHandler();
            handler.sendSetBombPackPosition(bp);
        }
    }

    public final synchronized void sendNewExplosion(UpdatingExplosion e) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            handler = player.getClientHandler();
            handler.sendNewExplosion(e);
        }
    }

    public final synchronized void sendRemoveExplosion(UpdatingExplosion e) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            handler = player.getClientHandler();
            handler.sendRemoveExplosion(e);
        }
    }

    public final synchronized void sendSetExplosionLevel(UpdatingExplosion e) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            handler = player.getClientHandler();
            handler.sendSetExplosionLevel(e);
        }
    }

    public final synchronized void
    sendPlayerSays(UpdatingPlayer p, String msg) {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            handler = player.getClientHandler();
            handler.sendPlayerSays(p, msg);
        }
    }

    public final synchronized void flush() {
        int            q, n;
        ClientHandler  handler;
        Player[]       players;
        UpdatingPlayer player;

        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            handler = player.getClientHandler();
            handler.flush();
        }
    }

    /* TCPGameServer ****************************************************/
    public TCPGameServerClientHandler newClient(Socket sock) {
        return new ClientHandler(this, sock, world);
    }
}
