package server;

import java.awt.*;

import objects.*;

final class UpdatingBomb
extends Bomb {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private static final int    minMaxDistance = 500;
    /* IMPORTANT: update in UserDefinedClient if changing max speed! */
    private static final double maxSpeed = 10.0;
    private static final double quantSpeed = 4.0;
    private              double maxTurn
                                 = 2.0 * Math.PI
                                 / (20.0 + Math.random() * 7.0);
    private static short nextId = 0;

    private int     maxDistance;
    private Server  server;
    private World   world;
    private double  x, y;   /* server needs accurate location */
    private double  dx, dy; /* update */
    private double  speed;
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
        UpdatingPlayer player, enemy;
        Ship           ship;
        Point          loc, myLoc;

        px = x;
        py = y;

        /* try to turn direction towards an enemy player */
        myLoc = getLocation();
        enemy = (UpdatingPlayer) world.getClosestEnemy(getOwner(),
                                                       myLoc.x, myLoc.y);
        if (enemy != null) {
            /* calculate turn. */
            double wantedDirection, dir, ddir, addir, correct;

            /* get direction to the enemy. */
            loc = enemy.getLocation();
            wantedDirection = world.getAngle(myLoc.x, myLoc.y, loc.x, loc.y);

            /* determine turn */
            dir = getDirection();
            ddir = wantedDirection - dir;
            addir = Math.abs(ddir);
            correct = addir < maxTurn ? addir : maxTurn;
            if (addir < Math.PI) {
                if (ddir < 0.0)
                    dir -= correct;
                else
                    dir += correct;
            } else {
                if (ddir < 0.0)
                    dir += correct;
                else
                    dir -= correct;
            }
            dir = Math.IEEEremainder(dir, 2.0 * Math.PI);
            if (dir < 0.0)
                dir += 2.0 * Math.PI;
            setDirection(dir);

            dx += quantSpeed * Math.cos(dir);
            dy += quantSpeed * Math.sin(dir);
            speed = Math.sqrt(dx * dx + dy * dy);
            if (speed > maxSpeed) {
                dx *= maxSpeed / speed;
                dy *= maxSpeed / speed;
            }
        }

        x += dx;
        y -= dy;
        distance += (int) (speed + 0.5);
        if (distance > maxDistance
            || x < 0 || x >= world.getWidth()
            || y < 0 || y >= world.getHeight()) {
            /* time to shut this bomb down */
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
                player.handleHit((UpdatingPlayer) getOwner(), WeaponType.BOMB,
                                 30 + (int) (Math.random() * 30.0),
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
    public UpdatingBomb(Server server, World world, short id,
                        UpdatingPlayer owner, int x, int y,
                        double dir, Color col) {
        super(id, owner, x, y, dir, col);

        this.server = server;
        this.world = world;
        this.x = x;
        this.y = y;
        distance = 0;
        maxDistance = minMaxDistance + (int) (Math.random() * 300.0);
        dx = maxSpeed * Math.cos(dir);
        dy = maxSpeed * Math.sin(dir);
        speed = maxSpeed;
        remove = false;
    }

    /* Bomb *************************************************************/
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
