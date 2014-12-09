package server;

import java.awt.*;

import objects.*;
final class UpdatingBombPack
extends BombPack {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private static short nextId = 0;

    private Server  server;
    private World   world;
    private double  x, y;   /* server needs accurate location */
    private int     bombsLeft;
    private boolean remove;

/*-----------------------------------------------------------------------+
 |  PACKAGE LOCAL PART                                                   |
 +----------------------------------------------------------------------*/
    static final short getNextId() {
        return nextId++;
    }

    /* called by the updater daemon */
    final synchronized boolean update() {
        int               q, n;
        Player[]          players;
        UpdatingPlayer player;
        Ship              ship;

        /* any ships "hit" by this? */
        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            if (!player.isAlive())
                continue;
            ship = player.getShip();
            if (ship.isHitBy(loc.x, loc.y)
                || ship.isHitBy(loc.x - 5, loc.y - 5)
                || ship.isHitBy(loc.x + 5, loc.y - 5)
                || ship.isHitBy(loc.x + 5, loc.y + 5)
                || ship.isHitBy(loc.x - 5, loc.y + 5)) {
                /* increase number of bombs for this player */
                player.incBombsLeft(bombsLeft);
                player.getClientHandler().sendSetPlayerStatus();

                remove = true;
            }
        }

        super.setLocation((int) (x + 0.5), (int) (y + 0.5));
        return true;
    }

    final boolean doRemove() {
        return remove;
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public UpdatingBombPack(Server server, World world, short id, int x, int y,
                            double dir, Color col, int bombsLeft) {
        super(id, x, y, dir, col);

        this.server = server;
        this.world = world;
        this.x = x;
        this.y = y;
        this.bombsLeft = bombsLeft;
        remove = false;
    }

    /* BombPack *********************************************************/
    public final synchronized Point getLocation() {
        return new Point((int) (x + 0.5), (int) (y + 0.5));
    }

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
