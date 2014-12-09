package server;

import java.awt.*;

import objects.*;

final class UpdatingPlayer
extends Player {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    /* IMPORTANT: update in UserDefinedClient if changing max speed! */
    private static final double maxSpeed = 8.0;
    private static final double quantSpeed = 1.5;
    private static short        nextId = 0;

    private ClientHandler cliHandler;
    private World         world;
    private byte          turn;
    private byte          thrust;
    private double        x, y;  /* server needs it accurate */
    private double        driftX, driftY;
    private boolean       newPhaserOk;

    private void setup() {
        stopMovement();
        world = cliHandler.getWorld();
    }

/*-----------------------------------------------------------------------+
 |  PACKAGE LOCAL PART                                                   |
 +----------------------------------------------------------------------*/
    static final short getNextId() {
        return nextId++;
    }

    /* called by the updater daemon */
    final synchronized boolean update() {
        boolean ret = false;

        if (!isAlive())
            return false;

        if (turn != 0) {
            double dir = getDirection();

            dir += (turn * 2.0 * Math.PI) / 32.0;
            dir = Math.IEEEremainder(dir, 2.0 * Math.PI);
            if (dir < 0.0)
                dir += 2.0 * Math.PI;
            setDirection(dir);
            ret = true;
        }
        if (thrust != 0) {
            double dir = getDirection();
            double speed;

            driftX += quantSpeed * thrust * Math.cos(dir);
            driftY -= quantSpeed * thrust * Math.sin(dir);
            speed = Math.sqrt(driftX * driftX + driftY * driftY);
            if (speed > maxSpeed) {
                driftX *= maxSpeed / speed;
                driftY *= maxSpeed / speed;
            }
        }
        if (driftX != 0.0 || driftY != 0.0) {
            if (thrust == 0) {
                if (Math.abs(driftX /= 1.03) < 0.25)
                    driftX = 0.0;
                if (Math.abs(driftY /= 1.03) < 0.25)
                    driftY = 0.0;
            }
            x += driftX;
            y += driftY;
            if (x < 0.0)
                x = 0.0;
            else if (x >= world.getWidth())
                x = world.getWidth() - 1;
            if (y < 0.0)
                y = 0.0;
            else if (y >= world.getHeight())
                y = world.getHeight() - 1;
            super.setLocation((int) (x + 0.5), (int) (y + 0.5));
            ret = true;
        }
        return ret;
    }

    final ClientHandler getClientHandler() {
        return cliHandler;
    }

    final byte getTurn() {
        return turn;
    }

    final void setTurn(byte turn) {
        this.turn = turn;
    }

    final byte getThrust() {
        return thrust;
    }

    final void setThrust(byte thrust) {
        this.thrust = thrust;
    }

    final void setNewPhaserOk(boolean status) {
        newPhaserOk = status;
    }

    final boolean getNewPhaserOk() {
        return newPhaserOk;
    }

    final void stopMovement() {
        turn = 0;
        thrust = 0;
        driftX = driftY = 0.0;
    }

    final void handleHit(UpdatingPlayer hitter, byte weapon, int damage,
                         Server server) {
        Point             loc;
        UpdatingExplosion e;
        UpdatingBombPack  bp;

        server.sendPlayerHit(this, hitter, weapon);

        /* increase the damage of the player */
        incDamage(damage);

        /* did player die? */
        if (getDamage() >= 100) {
            /* kill the player */
            setAlive(false);
            incAntiScore();
            stopMovement();
            server.sendPlayerDies(this, hitter, weapon);

            /* make an explosion */
            loc = getLocation();
            e = new UpdatingExplosion(server, world,
                                      UpdatingExplosion.getNextId(),
                                      loc.x, loc.y, (byte) 0, (byte) 15);
            world.addExplosion(e);
            server.sendNewExplosion(e);

            /* update score for murderer (low moral, as always!) */
            hitter.incScore();
            server.sendSetPlayerScore(hitter);

            /* possibly make the rest of the dead man's bombs
             * float around for others to find. */
            if (getBombsLeft() > 0) {
                bp = new UpdatingBombPack(server, world,
                                          UpdatingBombPack.getNextId(),
                                          loc.x, loc.y, getDirection(),
                                          getShip().getColor(),
                                          getBombsLeft());
                world.addBombPack(bp);
                server.sendNewBombPack(bp);
            }
        } else {
            if (damage >= 25) {
                /* make a little explosion */
                loc = getLocation();
                e = new UpdatingExplosion(server, world,
                                          UpdatingExplosion.getNextId(),
                                          loc.x, loc.y, (byte) 0, (byte) 5);
                world.addExplosion(e);
                server.sendNewExplosion(e);
            }
        }
        server.sendSetPlayerScore(this);
        getClientHandler().sendSetPlayerStatus();
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public UpdatingPlayer(ClientHandler cliHandler) {
        super();

        this.cliHandler = cliHandler;
        setup();
    }

    public UpdatingPlayer(ClientHandler cliHandler, short id, String name) {
        super(id, name);

        this.cliHandler = cliHandler;
        setup();
    }

    /* Player ***********************************************************/
    public final synchronized void setLocation(Point loc) {
        super.setLocation(loc);
        x = loc.x;
        y = loc.y;
    }

    public final synchronized void setLocation(int x, int y) {
        super.setLocation(x, y);
        this.x = x;
        this.y = y;
    }
}
