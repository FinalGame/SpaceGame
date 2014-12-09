package server;

import java.awt.*;

import objects.*;


final class UpdatingPhaser
extends Phaser {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private static final int  maxDistance = 500;
    /* IMPORTANT: update in UserDefinedClient if changing pix per update! */
    private static final int  pixPerUpdate = 15;

    private static short      nextId = 0;

    private Server  server;
    private World   world;
    private double  x, y;   /* server needs accurate location */
    private double  dx, dy; /* update */
    private int     distance;
    private boolean remove;

/*-----------------------------------------------------------------------+
 |  PACKAGE LOCAL PART                                                   |
 +----------------------------------------------------------------------*/
    static final short getNextId() {
        return nextId++;
    }

    /* called by the updater daemon */
    final synchronized boolean update() {
        int            q, n;
        double         px, py, m1x, m1y, m2x, m2y, m3x, m3y;
        Player[]       players;
        UpdatingPlayer player;
        Ship           ship;
        Point          loc;

        px = x;
        py = y;

        x += dx;
        y -= dy;
        distance += pixPerUpdate;
        if (distance > maxDistance
            || x < 0 || x >= world.getWidth()
            || y < 0 || y >= world.getHeight()) {
            /* time to shut this phaser down */
            remove = true;
        }

        /* any ships hit? */
        /* as the bullet moves rather far for each update, we better check
           some midpoints between the previous and the new location too. */
        m1x = px + (x - px) / 2.0;
        m1y = py + (y - py) / 2.0;
        m2x = px + (m1x - px) / 2.0;
        m2y = py + (m1y - py) / 2.0;
        m3x = m1x + (x - m1x) / 2.0;
        m3y = m1y + (y - m1y) / 2.0;
        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (UpdatingPlayer) players[q];
            if (player == getOwner() || !player.isAlive())
                continue;
            ship = player.getShip();
            if (ship.isHitBy((int) (x + 0.5), (int) (y + 0.5))
                || ship.isHitBy((int) (m1x + 0.5), (int) (m1y + 0.5))
                || ship.isHitBy((int) (m2x + 0.5), (int) (m2y + 0.5))
                || ship.isHitBy((int) (m3x + 0.5), (int) (m3y + 0.5))
                ) {
                player.handleHit((UpdatingPlayer) getOwner(),
                                 WeaponType.PHASER,
                                 5 + (int) (Math.random() * 15.0),
                                 server);
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
    public UpdatingPhaser(Server server, World world, short id,
                          UpdatingPlayer owner,
                          int x, int y, double dir, Color col) {
        super(id, owner, x, y, dir, col);

        this.server = server;
        this.world = world;
        this.x = x;
        this.y = y;
        dx = pixPerUpdate * Math.cos(dir);
        dy = pixPerUpdate * Math.sin(dir);
        distance = 0;
        remove = false;
    }

    /* Phaser ***********************************************************/
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
