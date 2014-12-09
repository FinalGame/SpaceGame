package client;

import java.io.*;
import java.net.*;
import java.awt.*;

import no.shhsoft.net.*;

import netgame.*;
import server.*;
import objects.*;
import gui.*;

public final class Client
extends TCPGameClient {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private static final byte protocolVersion = 11;  /* also update in
                                                      * server/Client-
                                                      * Handler.java */
    private short             myPlayerId;
    private MainFuncProvider  mainFunc;
    private World             world;
    private Board             board;
    private BoardUpdater      updater;
    private String            userClientClassName;
    private UserDefinedClient userClient;
    private Player            me;

    private void handleException(IOException e) {
        System.err.println("client: network write failed: " + e.getMessage());
        System.exit(1);
    }

    private final void receiveGetLost(Message m)
    throws IOException {
        String msg;

        msg = m.getString();
        System.err.println(msg);
        mainFunc.doQuit();
    }

    private final void receiveMyId(Message m)
    throws IOException {
        short id;

        id = m.getShort();

        myPlayerId = id;
    }

    private final void receiveNewPlayer(Message m)
    throws IOException {
        short   id;
        int     x, y;
        String  name;
        short   dir;
        int     col;
        boolean alive;
        Player  player;

        id    = m.getShort();
        name  = m.getString();
        x     = m.getShort();
        y     = m.getShort();
        dir   = m.getShort();
        col   = m.getInt();
        alive = m.getBoolean();

        player = new Player(id, name);
        player.setLocation(x, y);
        player.setDirectionFromShort(dir);
        player.getShip().setColor(new Color(col));
        player.setAlive(alive);
        world.addPlayer(player);

        if (me != null)
            board.addMessage(name + " enters the game.");
        if (id == myPlayerId) {
            /* that's me! i'm accepted! */
            me = player;
            if (userClient != null) {
                /* the server sends the entire world description
                 * before sending the new player info, so everything
                 * should be available for the user defined client at
                 * this point. */
                userClient.infoLoggedIntoServer();
            }
        }
        board.updateScoreText();

        if (userClient != null)
            userClient.infoPlayerEntered(player);
    }

    private final void receiveRemovePlayer(Message m)
    throws IOException {
        short  id;
        Player p;
        int    frags;

        id = m.getShort();

        if ((p = (Player) world.findPlayer(id)) != null) {
            frags = p.getScore();
            board.addMessage(p.getName() + " left the game with " + frags
                             + " frag" + (frags == 1 ? "" : "s"));
            world.removePlayer(p);
            board.updateScoreText();

            if (userClient != null)
                userClient.infoPlayerLeft(p);
        }
    }

    private final void receiveSetPlayerName(Message m)
    throws IOException {
        short  id;
        String name;
        Player p;

        id   = m.getShort();
        name = m.getString();

        if ((p = (Player) world.findPlayer(id)) != null) {
            p.setName(name);
            board.updateScoreText();
        }
    }

    private final void receiveSetPlayerPosition(Message m)
    throws IOException {
        short  id;
        int    x, y;
        short  dir;
        Player p;

        id  = m.getShort();
        x   = m.getShort();
        y   = m.getShort();
        dir = m.getShort();

        if ((p = (Player) world.findPlayer(id)) != null) {
            p.setLocation(x, y);
            p.setDirectionFromShort(dir);

            if (userClient != null)
                userClient.infoPlayerMoved(p);
        }
    }

    private final void receiveSetPlayerScore(Message m)
    throws IOException {
        short  id;
        int    score;
        int    antiScore;
        Player p;

        id        = m.getShort();
        score     = m.getShort();
        antiScore = m.getShort();

        if ((p = (Player) world.findPlayer(id)) != null) {
            p.setScore(score);
            p.setAntiScore(antiScore);
            board.updateScoreText();
        }
    }

    private final void receiveSetPlayerStatus(Message m)
    throws IOException {
        byte damage;
        byte phaserHeat;
        byte bombsLeft;

        damage     = m.getByte();
        phaserHeat = m.getByte();
        bombsLeft  = m.getByte();

        me.setDamage(damage);
        me.setPhaserHeat(phaserHeat);
        me.setBombsLeft(bombsLeft);
    }

    private final void receivePlayerHit(Message m)
    throws IOException {
        short  id, idHitter;
        byte   weapon;
        Player p, hitter;

        id       = m.getShort();
        idHitter = m.getShort();
        weapon   = m.getByte();

        p = (Player) world.findPlayer(id);
        hitter = (Player) world.findPlayer(idHitter);

        if (p != null && userClient != null)
            userClient.infoPlayerHit(p, hitter, weapon);

    }

    private final void receivePlayerDies(Message m)
    throws IOException {
        short  id, idKiller;
        byte   weapon;
        Player p, killer;

        id       = m.getShort();
        idKiller = m.getShort();
        weapon   = m.getByte();

        if ((p = (Player) world.findPlayer(id)) != null) {
            killer = (Player) world.findPlayer(idKiller);
            if (p == me) {
                myDeathTime = System.currentTimeMillis();
                if (updater != null)
                    updater.playerDied();
            }
            p.setAlive(false);
            world.removeCollidable(p.getShip());
            if (killer != null)
                board.addMessage(p.getName() + " was killed by "
                                 + killer.getName() + "'s "
                                 + WeaponType.getName(weapon) + ".");
            else
                board.addMessage(p.getName() + " died.");

            if (userClient != null)
                userClient.infoPlayerDied(p, killer, weapon);
        }
    }

    private final void receivePlayerResurrects(Message m)
    throws IOException {
        short  id;
        int    x, y;
        short  dir;
        Player p;

        id  = m.getShort();
        x   = m.getShort();
        y   = m.getShort();
        dir = m.getShort();

        if ((p = (Player) world.findPlayer(id)) != null) {
            p.setLocation(x, y);
            p.setDirectionFromShort(dir);
            p.setAlive(true);
            p.setDamage(0);
            p.setPhaserHeat(0);
            world.addCollidable(p.getShip());

            if (p == me && updater != null)
                updater.playerResurrected();

            if (userClient != null)
                userClient.infoPlayerResurrected(p);
        }
    }

    private final void receiveNewStar(Message m)
    throws IOException {
        int x, y, col;

        x   = m.getShort();
        y   = m.getShort();
        col = m.getInt();

        world.addStar(new Star(x, y, new Color(col)));
    }

    private final void receiveNewPhaser(Message m)
    throws IOException {
        short  id, ownerId;
        int    x, y, col;
        short  dir;
        Player owner;
        Phaser p;

        id      = m.getShort();
        ownerId = m.getShort();
        x       = m.getShort();
        y       = m.getShort();
        dir     = m.getShort();
        col     = m.getInt();

        owner = world.findPlayer(ownerId);
        p = new Phaser(id, owner, x, y, dir, new Color(col));
        world.addPhaser(p);

        if (userClient != null)
            userClient.infoPhaserCreated(p);
    }

    private final void receiveRemovePhaser(Message m)
    throws IOException {
        short  id;
        Phaser p;

        id = m.getShort();

        if ((p = (Phaser) world.findPhaser(id)) != null) {
            world.removePhaser(p);

            if (userClient != null)
                userClient.infoPhaserDestroyed(p);
        }
    }

    private final void receiveSetPhaserPosition(Message m)
    throws IOException {
        short  id;
        int    x, y;
        Phaser p;

        id = m.getShort();
        x  = m.getShort();
        y  = m.getShort();

        if ((p = (Phaser) world.findPhaser(id)) != null) {
            p.setLocation(x, y);

            if (userClient != null)
                userClient.infoPhaserMoved(p);
        }
    }

    private final void receiveNewBomb(Message m)
    throws IOException {
        short  id, ownerId;
        int    x, y, col;
        short  dir;
        Player owner;
        Bomb   b;

        id      = m.getShort();
        ownerId = m.getShort();
        x       = m.getShort();
        y       = m.getShort();
        dir     = m.getShort();
        col     = m.getInt();

        owner = world.findPlayer(ownerId);
        b = new Bomb(id, owner, x, y, dir, new Color(col));
        world.addBomb(b);

        if (userClient != null)
            userClient.infoBombCreated(b);
    }

    private final void receiveRemoveBomb(Message m)
    throws IOException {
        short id;
        Bomb  b;

        id = m.getShort();

        if ((b = (Bomb) world.findBomb(id)) != null) {
            world.removeBomb(b);

            if (userClient != null)
                userClient.infoBombDestroyed(b);
        }
    }

    private final void receiveSetBombPosition(Message m)
    throws IOException {
        short id;
        int   x, y;
        Bomb  b;

        id = m.getShort();
        x  = m.getShort();
        y  = m.getShort();

        if ((b = (Bomb) world.findBomb(id)) != null) {
            b.setLocation(x, y);

            if (userClient != null)
                userClient.infoBombMoved(b);
        }
    }

    private final void receiveNewBombPack(Message m)
    throws IOException {
        short    id;
        int      x, y, col;
        short    dir;
        BombPack bp;

        id  = m.getShort();
        x   = m.getShort();
        y   = m.getShort();
        dir = m.getShort();
        col = m.getInt();

        bp = new BombPack(id, x, y, dir, new Color(col));
        world.addBombPack(bp);

        if (userClient != null)
            userClient.infoBombPackCreated(bp);
    }

    private final void receiveRemoveBombPack(Message m)
    throws IOException {
        short    id;
        BombPack bp;

        id = m.getShort();

        if ((bp = (BombPack) world.findBombPack(id)) != null) {
            world.removeBombPack(bp);

            if (userClient != null)
                userClient.infoBombPackDestroyed(bp);
        }
    }

    private final void receiveSetBombPackPosition(Message m)
    throws IOException {
        short    id;
        int      x, y;
        BombPack bp;

        id = m.getShort();
        x  = m.getShort();
        y  = m.getShort();

        if ((bp = (BombPack) world.findBombPack(id)) != null) {
            bp.setLocation(x, y);

            if (userClient != null)
                userClient.infoBombPackMoved(bp);
        }
    }

    private final void receiveNewExplosion(Message m)
    throws IOException {
        short id;
        int   x, y;
        byte  level, maxLevel;
        int   distance;

        id       = m.getShort();
        x        = m.getShort();
        y        = m.getShort();
        level    = m.getByte();
        maxLevel = m.getByte();

        if (me != null && me.getShip() != null) {
            distance = world.getDistanceBetween(me.getShip(), x, y);
            if (distance < (board.getWidth() * 3) / 2) {
            }
        }

        world.addExplosion(new Explosion(id, x, y, level, maxLevel));
    }

    private final void receiveRemoveExplosion(Message m)
    throws IOException {
        short     id;
        Explosion e;

        id = m.getShort();

        if ((e = (Explosion) world.findExplosion(id)) != null)
            world.removeExplosion(e);
    }

    private final void receiveSetExplosionLevel(Message m)
    throws IOException {
        short     id;
        byte      level;
        Explosion e;

        id    = m.getShort();
        level = m.getByte();

        if ((e = (Explosion) world.findExplosion(id)) != null)
            e.setLevel(level);
    }

    private final void receivePlayerSays(Message m)
    throws IOException {
        short  id;
        String msg;
        Player p;

        id  = m.getShort();
        msg = m.getString();

        if ((p = (Player) world.findPlayer(id)) != null) {
            board.addChatMessage(p.getName() + ": " + msg);

            if (userClient != null)
                userClient.infoPlayerSaidSomething(p, msg);
        }
    }

/*-----------------------------------------------------------------------+
 |  PACKAGE LOCAL PART                                                   |
 +----------------------------------------------------------------------*/
    long myDeathTime;  /* used by BoardUpdater for pausing
                        * before ressurrection */

    final short getMyPlayerId() {
        return myPlayerId;
    }

/*-----------------------------------------------------------------------+
 |  INHERITANTS' INTERFACE                                               |
 +----------------------------------------------------------------------*/
    /* TCPGameClient ****************************************************/
    protected final boolean readIncoming() {
        Message m;

        try {
            m = receiveMessage();
            switch (m.getType()) {
              case ClientCommands.GET_LOST:
                receiveGetLost(m);
                break;
              case ClientCommands.SET_YOUR_ID:
                receiveMyId(m);
                break;
              case ClientCommands.NEW_PLAYER:
                receiveNewPlayer(m);
                break;
              case ClientCommands.REMOVE_PLAYER:
                receiveRemovePlayer(m);
                break;
              case ClientCommands.SET_PLAYER_NAME:
                receiveSetPlayerName(m);
                break;
              case ClientCommands.SET_PLAYER_POSITION:
                receiveSetPlayerPosition(m);
                break;
              case ClientCommands.SET_PLAYER_SCORE:
                receiveSetPlayerScore(m);
                break;
              case ClientCommands.SET_PLAYER_STATUS:
                receiveSetPlayerStatus(m);
                break;
              case ClientCommands.PLAYER_HIT:
                receivePlayerHit(m);
                break;
              case ClientCommands.PLAYER_DIES:
                receivePlayerDies(m);
                break;
              case ClientCommands.PLAYER_RESURRECTS:
                receivePlayerResurrects(m);
                break;
              case ClientCommands.NEW_STAR:
                receiveNewStar(m);
                break;
              case ClientCommands.NEW_PHASER:
                receiveNewPhaser(m);
                break;
              case ClientCommands.REMOVE_PHASER:
                receiveRemovePhaser(m);
                break;
              case ClientCommands.SET_PHASER_POSITION:
                receiveSetPhaserPosition(m);
                break;
              case ClientCommands.NEW_BOMB:
                receiveNewBomb(m);
                break;
              case ClientCommands.REMOVE_BOMB:
                receiveRemoveBomb(m);
                break;
              case ClientCommands.SET_BOMB_POSITION:
                receiveSetBombPosition(m);
                break;
              case ClientCommands.NEW_BOMB_PACK:
                receiveNewBombPack(m);
                break;
              case ClientCommands.REMOVE_BOMB_PACK:
                receiveRemoveBombPack(m);
                break;
              case ClientCommands.SET_BOMB_PACK_POSITION:
                receiveSetBombPackPosition(m);
                break;
              case ClientCommands.NEW_EXPLOSION:
                receiveNewExplosion(m);
                break;
              case ClientCommands.REMOVE_EXPLOSION:
                receiveRemoveExplosion(m);
                break;
              case ClientCommands.SET_EXPLOSION_LEVEL:
                receiveSetExplosionLevel(m);
                break;
              case ClientCommands.PLAYER_SAYS:
                receivePlayerSays(m);
                break;
              default:
                System.err.println("client: got unknown command "
                                   + m.getType());
            }
        } catch (IOException e) {
            System.err.println("client: network read failed: "
                               + e.getMessage());
            System.exit(1);
        }
        return true;
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public Client(MainFuncProvider mainFunc, String host, int port,
                  World world, Board board, String userClientClassName)
    throws IOException {
        super(host, port);
        this.mainFunc = mainFunc;
        myPlayerId = -1;
        this.world = world;
        this.board = board;
        this.userClientClassName = userClientClassName;
        me = null;
    }

    public final Player getMyPlayer() {
        return me;
    }

    public final void startUpdater() {
        Class userClientClass;

        updater = board.startUpdater(this);
        if (userClientClassName != null) {
            try {
                userClientClass = Class.forName(userClientClassName);
                userClient = (UserDefinedClient) userClientClass.newInstance();
                userClient.setClasses(this, world, board, updater);
                updater.setUserClient(userClient);
                board.setUserClient(userClient);
            } catch (Throwable e) {
                System.err.println("unable to load `" + userClientClassName
                                   + "': " + e);
            }
        }
    }

    public final void stopUpdater() {
        updater = null;
        board.stopUpdater();
    }

    public final synchronized void sendLogin(String name) {
        try {
            Message m = new Message(ServerCommands.LOGIN);
            m.putByte(protocolVersion);
            m.putString(name);
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    public final synchronized void sendSetName(String name) {
        try {
            Message m = new Message(ServerCommands.SET_NAME);
            m.putString(name);
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    public final synchronized void sendSetTurn(byte turn) {
        try {
            Message m = new Message(ServerCommands.SET_TURN);
            m.putByte(turn);
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    public final synchronized void sendSetThrust(byte thrust) {
        try {
            Message m = new Message(ServerCommands.SET_THRUST);
            m.putByte(thrust);
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    public final synchronized void sendFirePhaser() {
        try {
            Message m = new Message(ServerCommands.FIRE_PHASER);
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    public final synchronized void sendFireBomb() {
        try {
            Message m = new Message(ServerCommands.FIRE_BOMB);
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    public final synchronized void sendResurrectMe() {
        try {
            Message m = new Message(ServerCommands.RESURRECT_ME);
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    public final synchronized void sendSay(String msg) {
        try {
            Message m = new Message(ServerCommands.SAY);
            m.putString(msg);
            sendMessageNoFlush(m);
        } catch (IOException e) {
            handleException(e);
        }
    }

    public final synchronized void flush() {
        try {
            flushOut();
        } catch (IOException e) {
            handleException(e);
        }
    }
}
