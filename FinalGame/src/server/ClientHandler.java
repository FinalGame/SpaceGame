package server;

import java.io.*;
import java.net.*;
import java.awt.*;

import no.shhsoft.net.*;

import netgame.*;
import client.*;
import objects.*;
final class ClientHandler
extends TCPGameServerClientHandler {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private static final byte protocolVersion = 11; /* also update in
                                                     * client/Client.java */
    private boolean        cont; /* false indicates "stop client" */
    private World          world;
    private UpdatingPlayer me;

    private void handleException(IOException e) {
        /* will be called when there is an error, but also when the
         * client disconnects. */
        if (me != null) {
            world.removePlayer(me);
            ((Server) server).colorResycler
                .resycleColor(me.getShip().getColor());
            ((Server) server).sendRemovePlayer(me);
        }
        cont = false;
    }

    private final void receiveLogin(Message m)
    throws IOException {
        byte           version;
        String         name;
        UpdatingPlayer player;

        version = m.getByte();
        name    = m.getString();

        if (version != protocolVersion) {
            sendGetLost("Server says: Wrong protocol version: "
                        + "You want #" + version
                        + ", I talk #" + protocolVersion + ".\n    "
                        + (version < protocolVersion
                           ? "You're outdated, son."
                           : "I'm too old for this shit."));
            flush();
            stop();
            return;
        }

        player = new UpdatingPlayer(this, UpdatingPlayer.getNextId(), name);
        player.getShip().setColor(((Server) server).colorResycler.getColor());
        me = player;
        player.setLocation(world.findGoodLocation());
        player.setDirection(Math.random() * 1.99999 * Math.PI);

        sendSetYourId(player.getId());
        sendWorld();

        world.addPlayer(player);
        ((Server) server).sendNewPlayer(player);

        me.setBombsLeft(5);
        sendSetPlayerStatus();
        flush();
    }

    private final void receiveSetName(Message m)
    throws IOException {
        String name;

        name = m.getString();

        me.setName(name);
        ((Server) server).sendSetPlayerName(me);
    }

    private final void receiveSetTurn(Message m)
    throws IOException {
        byte turn;

        turn = m.getByte();

        me.setTurn(turn);
    }

    private final void receiveSetThrust(Message m)
    throws IOException {
        byte thrust;

        thrust = m.getByte();

        me.setThrust(thrust);
    }

    private final void receiveFirePhaser(Message m)
    throws IOException {
        UpdatingPhaser p;
        int            offs;
        Point          loc;
        Color          col;
        double         dir;
        Ship           ship;

        if (me.isPhaserOverheated() || !me.getNewPhaserOk())
            return;

        ship = me.getShip();
        offs = ship.getPhaserOffset();
        col = Color.white;
        loc = new Point(me.getLocation());
        dir = me.getDirection();
        loc.x += (int) (offs * Math.cos(dir) + 0.5);
        loc.y -= (int) (offs * Math.sin(dir) + 0.5);

        p = new UpdatingPhaser((Server) server, world,
                               UpdatingPhaser.getNextId(), me,
                               loc.x, loc.y, dir, col);

        world.addPhaser(p);

        me.incPhaserHeat((int) (Math.random() * 3) + 1);
        me.setNewPhaserOk(false);
        if (me.getPhaserHeat() > 75)
            me.setPhaserHeat(100);
        sendSetPlayerStatus();
        flush();

        ((Server) server).sendNewPhaser(p);
        ((Server) server).flush();
    }

    private final void receiveFireBomb(Message m)
    throws IOException {
        UpdatingBomb b;
        int          offs;
        Point        loc;
        Color        col;
        double       dir;
        Ship         ship;

        if (me.getBombsLeft() == 0)
            return;

        ship = me.getShip();
        offs = ship.getPhaserOffset();
        col = ship.getColor();
        loc = new Point(me.getLocation());
        dir = me.getDirection();
        loc.x += (int) (offs * Math.cos(dir) + 0.5);
        loc.y -= (int) (offs * Math.sin(dir) + 0.5);

        b = new UpdatingBomb((Server) server, world, UpdatingBomb.getNextId(),
                             me, loc.x, loc.y, dir, col);

        world.addBomb(b);

        me.decBombsLeft(1);
        sendSetPlayerStatus();
        flush();

        ((Server) server).sendNewBomb(b);
        ((Server) server).flush();
    }

    private final void receiveResurrectMe(Message m)
    throws IOException {
        me.setLocation(world.findGoodLocation());
        me.setDirection(Math.random() * 1.99999 * Math.PI);
        me.setAlive(true);
        me.setDamage(0);
        me.setPhaserHeat(0);
        me.setBombsLeft(5);

        sendSetPlayerStatus();
        flush();
        ((Server) server).sendPlayerResurrects(me);
        ((Server) server).flush();
    }

    private final void receiveSay(Message m)
    throws IOException {
        String msg;

        msg = m.getString();

        ((Server) server).sendPlayerSays(me, msg);
        ((Server) server).flush();
    }

/*-----------------------------------------------------------------------+
 |  INHERITANTS' INTERFACE                                               |
 +----------------------------------------------------------------------*/
    /* TCPGameServerClientHandler ***************************************/
    protected final boolean readIncoming() {
        Message m;

        try {
            m = receiveMessage();
            switch (m.getType()) {
              case ServerCommands.LOGIN:
                receiveLogin(m);
                break;
              case ServerCommands.SET_NAME:
                receiveSetName(m);
                break;
              case ServerCommands.SET_TURN:
                receiveSetTurn(m);
                break;
              case ServerCommands.SET_THRUST:
                receiveSetThrust(m);
                break;
              case ServerCommands.FIRE_PHASER:
                receiveFirePhaser(m);
                break;
              case ServerCommands.FIRE_BOMB:
                receiveFireBomb(m);
                break;
              case ServerCommands.RESURRECT_ME:
                receiveResurrectMe(m);
                break;
              case ServerCommands.SAY:
                receiveSay(m);
                break;
              default:
                System.err.println("server: got unknown command "
                                   + m.getType() + " from " + getPeerName());
            }
        } catch (IOException e) {
            handleException(e);
        }
        return cont;
    }

/*-----------------------------------------------------------------------+
 |  PACKAGE LOCAL PART                                                   |
 +----------------------------------------------------------------------*/
    final synchronized void sendGetLost(String msg) {
        try {
            Message m = new Message(ClientCommands.GET_LOST);
            m.putString(msg);
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendSetYourId(short id) {
        try {
            Message m = new Message(ClientCommands.SET_YOUR_ID);
            m.putShort(id);
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendNewPlayer(UpdatingPlayer p) {
        Point loc;

        try {
            loc = p.getLocation();

            Message m = new Message(ClientCommands.NEW_PLAYER);
            m.putShort(p.getId());
            m.putString(p.getName());
            m.putShort((short) loc.x);
            m.putShort((short) loc.y);
            m.putShort(p.getDirectionAsShort());
            m.putInt(p.getShip().getColor().getRGB());
            /* since this function is used when sending the state to
             * new players, we also tell them wether the player is
             * alive or not. */
            m.putBoolean(p.isAlive());
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendRemovePlayer(UpdatingPlayer p) {
        Point loc;

        try {
            Message m = new Message(ClientCommands.REMOVE_PLAYER);
            m.putShort(p.getId());
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendSetPlayerName(UpdatingPlayer p) {
        Ship  ship;
        Point loc;

        try {
            Message m = new Message(ClientCommands.SET_PLAYER_NAME);
            m.putShort(p.getId());
            m.putString(p.getName());
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendSetPlayerPosition(UpdatingPlayer p) {
        Point loc;

        try {
            loc = p.getLocation();
            Message m = new Message(ClientCommands.SET_PLAYER_POSITION);
            m.putShort(p.getId());
            m.putShort((short) loc.x);
            m.putShort((short) loc.y);
            m.putShort(p.getDirectionAsShort());
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendSetPlayerScore(UpdatingPlayer p) {
        Ship  ship;
        Point loc;

        try {
            Message m = new Message(ClientCommands.SET_PLAYER_SCORE);
            m.putShort(p.getId());
            m.putShort(p.getScore());
            m.putShort(p.getAntiScore());
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendSetPlayerStatus() {
        Ship  ship;
        Point loc;

        try {
            Message m = new Message(ClientCommands.SET_PLAYER_STATUS);
            m.putByte(me.getDamage());
            m.putByte(me.getPhaserHeat());
            m.putByte(me.getBombsLeft());
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendPlayerHit(UpdatingPlayer p,
                                          UpdatingPlayer hitter,
                                          byte weapon) {
        try {
            Message m = new Message(ClientCommands.PLAYER_HIT);
            m.putShort(p.getId());
            m.putShort(hitter != null? hitter.getId() : -1);
            m.putByte(weapon);
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendPlayerDies(UpdatingPlayer p,
                                           UpdatingPlayer killer,
                                           byte weapon) {
        try {
            Message m = new Message(ClientCommands.PLAYER_DIES);
            m.putShort(p.getId());
            m.putShort(killer != null? killer.getId() : -1);
            m.putByte(weapon);
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendPlayerResurrects(UpdatingPlayer p) {
        Point loc;

        try {
            loc = p.getLocation();
            Message m = new Message(ClientCommands.PLAYER_RESURRECTS);
            m.putShort(p.getId());
            m.putShort((short) loc.x);
            m.putShort((short) loc.y);
            m.putShort(p.getDirectionAsShort());
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendNewStar(Star s) {
        Point loc;

        try {
            Message m = new Message(ClientCommands.NEW_STAR);
            loc = s.getLocation();
            m.putShort((short) loc.x);
            m.putShort((short) loc.y);
            m.putInt(s.getColor().getRGB());
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendNewPhaser(UpdatingPhaser p) {
        Point loc;

        try {
            Message m = new Message(ClientCommands.NEW_PHASER);
            m.putShort(p.getId());
            m.putShort(p.getOwner().getId());
            loc = p.getLocation();
            m.putShort((short) loc.x);
            m.putShort((short) loc.y);
            m.putShort(p.getDirectionAsShort());
            m.putInt(p.getColor().getRGB());
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendRemovePhaser(UpdatingPhaser p) {
        try {
            Message m = new Message(ClientCommands.REMOVE_PHASER);
            m.putShort(p.getId());
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendSetPhaserPosition(UpdatingPhaser p) {
        Point loc;

        try {
            loc = p.getLocation();
            Message m = new Message(ClientCommands.SET_PHASER_POSITION);
            m.putShort(p.getId());
            m.putShort((short) loc.x);
            m.putShort((short) loc.y);
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendNewBomb(UpdatingBomb b) {
        Point loc;

        try {
            Message m = new Message(ClientCommands.NEW_BOMB);
            m.putShort(b.getId());
            m.putShort(b.getOwner().getId());
            loc = b.getLocation();
            m.putShort((short) loc.x);
            m.putShort((short) loc.y);
            m.putShort(b.getDirectionAsShort());
            m.putInt(b.getColor().getRGB());
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendRemoveBomb(UpdatingBomb b) {
        try {
            Message m = new Message(ClientCommands.REMOVE_BOMB);
            m.putShort(b.getId());
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendSetBombPosition(UpdatingBomb b) {
        Point loc;

        try {
            loc = b.getLocation();
            Message m = new Message(ClientCommands.SET_BOMB_POSITION);
            m.putShort(b.getId());
            m.putShort((short) loc.x);
            m.putShort((short) loc.y);
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendNewBombPack(UpdatingBombPack bp) {
        Point loc;

        try {
            Message m = new Message(ClientCommands.NEW_BOMB_PACK);
            m.putShort(bp.getId());
            loc = bp.getLocation();
            m.putShort((short) loc.x);
            m.putShort((short) loc.y);
            m.putShort(bp.getDirectionAsShort());
            m.putInt(bp.getColor().getRGB());
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendRemoveBombPack(UpdatingBombPack bp) {
        try {
            Message m = new Message(ClientCommands.REMOVE_BOMB_PACK);
            m.putShort(bp.getId());
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendSetBombPackPosition(UpdatingBombPack bp) {
        Point loc;

        try {
            loc = bp.getLocation();
            Message m = new Message(ClientCommands.SET_BOMB_PACK_POSITION);
            m.putShort(bp.getId());
            m.putShort((short) loc.x);
            m.putShort((short) loc.y);
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    final synchronized void sendNewExplosion(UpdatingExplosion e) {
        Point loc;

        try {
            Message m = new Message(ClientCommands.NEW_EXPLOSION);
            m.putShort(e.getId());
            loc = e.getLocation();
            m.putShort((short) loc.x);
            m.putShort((short) loc.y);
            m.putByte(e.getLevel());
            m.putByte(e.getMaxLevel());
            sendMessageNoFlush(m);
        } catch (IOException ioe) {
            handleException(ioe);
        }
    }

    final synchronized void sendRemoveExplosion(UpdatingExplosion e) {
        try {
            Message m = new Message(ClientCommands.REMOVE_EXPLOSION);
            m.putShort(e.getId());
            sendMessageNoFlush(m);
        } catch (IOException ioe) {
            handleException(ioe);
        }
    }

    final synchronized void sendSetExplosionLevel(UpdatingExplosion e) {
        try {
            Message m = new Message(ClientCommands.SET_EXPLOSION_LEVEL);
            m.putShort(e.getId());
            m.putByte(e.getLevel());
            sendMessageNoFlush(m);
        } catch (IOException ioe) {
            handleException(ioe);
        }
    }

    final synchronized void sendPlayerSays(UpdatingPlayer p, String msg) {
        try {
            Message m = new Message(ClientCommands.PLAYER_SAYS);
            m.putShort(p.getId());
            m.putString(msg);
            sendMessageNoFlush(m);
        } catch (IOException ioe) {
            handleException(ioe);
        }
    }

    final synchronized void sendWorld() {
        int         q;
        Star[]      stars;
        Explosion[] explosions;
        Phaser[]    phasers;
        Bomb[]      bombs;
        BombPack[]  bombPacks;
        Player[]    players;

        /* send the stars decorating the background */
        stars = world.getStars();
        for (q = 0; q < stars.length; q++)
            sendNewStar(stars[q]);

        /* send the explosions currently taking place */
        explosions = world.getExplosions();
        for (q = 0; q < explosions.length; q++)
            sendNewExplosion((UpdatingExplosion) explosions[q]);

        /* send the phasers */
        phasers = world.getPhasers();
        for (q = 0; q < phasers.length; q++)
            sendNewPhaser((UpdatingPhaser) phasers[q]);

        /* send the bombs */
        bombs = world.getBombs();
        for (q = 0; q < bombs.length; q++)
            sendNewBomb((UpdatingBomb) bombs[q]);

        /* send the bomb packs */
        bombPacks = world.getBombPacks();
        for (q = 0; q < bombPacks.length; q++)
            sendNewBombPack((UpdatingBombPack) bombPacks[q]);

        /* send the players (my player is sent later) */
        players = world.getPlayers();
        for (q = 0; q < players.length; q++) {
            sendNewPlayer((UpdatingPlayer) players[q]);
            sendSetPlayerScore((UpdatingPlayer) players[q]);
        }

        flush();
    }

    final void flush() {
        try {
            flushOut();
        } catch (IOException ioe) {
            handleException(ioe);
        }
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public ClientHandler(Server server, Socket sock, World world) {
        super(server, sock);
        this.world = world;
        cont = true;
        me = null;
    }

    public World getWorld() {
        return world;
    }
}
