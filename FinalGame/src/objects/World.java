package objects;

import java.util.*;
import java.awt.*;

public final class World {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private static final int defaultWidth = 1000;  /* 1500 */
    private static final int defaultHeight = 1000; /* 1500 */

    private int    width;
    private int    height;
    private Vector collidable; /* collidable objs: DrawableGameObject */
    private Vector decoration; /* non-collidable objs: DrawableGameObject */
    private Vector players;    /* players: Player */

    private Vector stars;
    private Vector phasers;
    private Vector bombs;
    private Vector bombPacks;
    private Vector explosions;

    private void setup() {
        width = defaultWidth;
        height = defaultHeight;
        collidable = new Vector(512);
        decoration = new Vector(512);
        players    = new Vector();
        stars      = new Vector(512);
        phasers    = new Vector(512);
        bombs      = new Vector(128);
        bombPacks  = new Vector(128);
        explosions = new Vector(128);
    }

    private final void addObjectsFrom(Vector from,
                                      Rectangle bounds, Vector to) {
        int                  q, n;
        DrawableGameObject   o;
        DrawableGameObject[] oa;

        synchronized (from) {
            n = from.size();
            oa = new DrawableGameObject[n];
            from.copyInto(oa);
            for (q = n - 1; q >= 0; q--) {
                o = oa[q];
                if (bounds.intersects(o.getBounds()))
                    to.addElement(o);
            }
        }
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public static final int TURN_NONE  = 0;
    public static final int TURN_LEFT  = 1;
    public static final int TURN_RIGHT = 2;

    public World() {
        setup();
    }

    public World(int width, int height) {
        this();
        setSize(width, height);
    }

    /* _must_ be called before anything else is done with this world. */
    public final void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public final int getWidth() {
        return width;
    }

    public final int getHeight() {
        return height;
    }

    public final void addCollidable(DrawableGameObject o) {
        synchronized (collidable) {
            /* without the if-test, we occationally got phantom
             * players, players who's ship stayed visible while the
             * player was dead. probably some synchronization problems
             * somewhere. */
            if (!collidable.contains(o))
                collidable.addElement(o);
        }
    }

    public final void removeCollidable(DrawableGameObject o) {
        collidable.removeElement(o);
    }

    public final void addDecoration(DrawableGameObject o) {
        decoration.addElement(o);
    }

    public final void removeDecoration(DrawableGameObject o) {
        decoration.removeElement(o);
    }

    /* return all objects fully or partially within the given area */
    public final DrawableGameObject[] getObjects(Rectangle bounds) {
        Vector               retv;
        DrawableGameObject[] ret;

        retv = new Vector(1024);
        addObjectsFrom(decoration, bounds, retv);
        addObjectsFrom(collidable, bounds, retv);
        ret = new DrawableGameObject[retv.size()];
        retv.copyInto(ret);
        return ret;
    }

    public final int getSquareDistanceBetween(DrawableGameObject o1,
                                              DrawableGameObject o2) {
        Point p1, p2;
        int   xx, yy;

        p1 = o1.getLocation();
        p2 = o2.getLocation();
        xx = p2.x - p1.x;
        yy = p2.y - p1.y;
        return (xx * xx + yy * yy);
    }

    public final int getDistanceBetween(DrawableGameObject o1,
                                        DrawableGameObject o2) {
        return (int) (Math.sqrt(getSquareDistanceBetween(o1, o2)) + 0.5);
    }

    public final int getSquareDistanceBetween(DrawableGameObject o,
                                              int x, int y) {
        Point p;
        int   xx, yy;

        p = o.getLocation();
        xx = x - p.x;
        yy = y - p.y;
        return (xx * xx + yy * yy);
    }

    public final int getDistanceBetween(DrawableGameObject o, int x, int y) {
        return (int) (Math.sqrt(getSquareDistanceBetween(o, x, y)) + 0.5);
    }

    public final int getSquareDistanceBetween(int x0, int y0, int x1, int y1) {
        int xx, yy;

        xx = x1 - x0;
        yy = y1 - y0;
        return (xx * xx + yy * yy);
    }

    public final int getDistanceBetween(int x0, int y0, int x1, int y1) {
        return (int) (Math.sqrt(getSquareDistanceBetween(x0, y0,
                                                         x1, y1)) + 0.5);
    }

    /* suitable when checking relative to an object that is not the player's
     * ship, such as the targeting bombs. */
    public final Player getClosestEnemy(Player me, int myX, int myY) {
        int    q, n, closest2, dist2;
        Player enemy;
        Player ret = null;

        closest2 = Integer.MAX_VALUE;
        synchronized (players) {
            n = players.size();
            for (q = 0; q < n; q++) {
                enemy = (Player) players.elementAt(q);
                if (enemy == me || !enemy.isAlive())
                    continue;
                dist2 = getSquareDistanceBetween(enemy.getShip(), myX, myY);
                if (dist2 < closest2) {
                    closest2 = dist2;
                    ret = enemy;
                }
            }
        }
        return ret;
    }

    /* checks between ships. */
    public final Player getClosestEnemy(Player me) {
        Point myLoc;

        myLoc = me.getLocation();
        return getClosestEnemy(me, myLoc.x, myLoc.y);
    }

    public final Bomb getClosestEnemyBomb(Player me, int myX, int myY) {
        int  q, n, closest2, dist2;
        Bomb enemyBomb;
        Bomb ret = null;

        closest2 = Integer.MAX_VALUE;
        synchronized (bombs) {
            n = bombs.size();
            for (q = 0; q < n; q++) {
                enemyBomb = (Bomb) bombs.elementAt(q);
                if (enemyBomb.getOwner() == me)
                    continue;
                dist2 = getSquareDistanceBetween(enemyBomb, myX, myY);
                if (dist2 < closest2) {
                    closest2 = dist2;
                    ret = enemyBomb;
                }
            }
        }
        return ret;
    }

    public final Bomb getClosestEnemyBomb(Player me) {
        Point myLoc;

        myLoc = me.getLocation();
        return getClosestEnemyBomb(me, myLoc.x, myLoc.y);
    }

    public final Phaser getClosestEnemyPhaser(Player me, int myX, int myY) {
        int    q, n, closest2, dist2;
        Phaser enemyPhaser;
        Phaser ret = null;

        closest2 = Integer.MAX_VALUE;
        synchronized (phasers) {
            n = phasers.size();
            for (q = 0; q < n; q++) {
                enemyPhaser = (Phaser) phasers.elementAt(q);
                if (enemyPhaser.getOwner() == me)
                    continue;
                dist2 = getSquareDistanceBetween(enemyPhaser, myX, myY);
                if (dist2 < closest2) {
                    closest2 = dist2;
                    ret = enemyPhaser;
                }
            }
        }
        return ret;
    }

    public final Phaser getClosestEnemyPhaser(Player me) {
        Point myLoc;

        myLoc = me.getLocation();
        return getClosestEnemyPhaser(me, myLoc.x, myLoc.y);
    }

    public final BombPack getClosestBombPack(int myX, int myY) {
        int      q, n, closest2, dist2;
        BombPack bombPack;
        BombPack ret = null;

        closest2 = Integer.MAX_VALUE;
        synchronized (bombPacks) {
            n = bombPacks.size();
            for (q = 0; q < n; q++) {
                bombPack = (BombPack) bombPacks.elementAt(q);
                dist2 = getSquareDistanceBetween(bombPack, myX, myY);
                if (dist2 < closest2) {
                    closest2 = dist2;
                    ret = bombPack;
                }
            }
        }
        return ret;
    }

    public final BombPack getClosestBombPack(Player me) {
        Point myLoc;

        myLoc = me.getLocation();
        return getClosestBombPack(myLoc.x, myLoc.y);
    }

    public final int getDistanceToClosestCollidable(int x, int y) {
        int                q, n, dist2;
        int                ret2 = Integer.MAX_VALUE;
        DrawableGameObject dgo;

        synchronized (collidable) {
            n = collidable.size();
            for (q = 0; q < n; q++) {
                dgo = (DrawableGameObject) collidable.elementAt(q);
                dist2 = getSquareDistanceBetween(dgo, x, y);
                if (dist2 < ret2)
                    ret2 = dist2;
            }
        }
        return (int) (Math.sqrt(ret2) + 0.5);
    }

    public final Point findGoodLocation() {
        int x, y, maxx, maxy, minx, miny, count;

        minx = width / 100;
        maxx = width - minx;
        miny = height / 100;
        maxy = height - miny;
        count = 0;
        /* create random locations, untill one is found that is
         * suitably far away from other objects. give up after a
         * number of retries. */
        do {
            x = minx + (int) (Math.random() * (maxx - minx));
            y = miny + (int) (Math.random() * (maxy - miny));
            if (++count == 100)
                break;
        } while (getDistanceToClosestCollidable(x, y) < minx * 20);
        return new Point(x, y);
    }

    /* this is the _visual_ angle in radians: 0 is right, PI/2 is up,
     * PI is left, and 3PI/2 is down. */
    public final double getAngle(int fromX, int fromY, int toX, int toY) {
        int    dx, dy;
        double ret;

        dx = toX - fromX;
        dy = fromY - toY; /* mathematical y direction: positive up. */
        if (dx == 0) {
            if (dy >= 0)
                ret = Math.PI / 2.0;
            else
                ret = -Math.PI / 2.0;
        } else
            ret = Math.atan((double) dy / dx);
        /* find correct quadrant, and "normalize". atan returns the
         * interval -PI/2 to PI/2, we want 0 to 2PI. */
        if (ret >= 0.0) {
            if (dy < 0 && dx < 0)
                ret += Math.PI;
        } else {
            if (dy > 0 && dx < 0)
                ret += Math.PI;
            else
                ret += 2.0 * Math.PI;
        }
        return ret;
    }

    public final double getDeltaAngle(double fromAngle, double toAngle) {
        double delta;

        delta = toAngle - fromAngle;
        if (delta < -Math.PI)
            delta += (2.0 * Math.PI);
        if (delta > Math.PI)
            delta -= (2.0 * Math.PI);
        return delta;
    }

    public final double getAbsDeltaAngle(double angle1, double angle2) {
        return Math.abs(getDeltaAngle(angle1, angle2));
    }

    public final int getBestTurn(double oldAngle, double newAngle) {
        int    ret;
        double delta;

        ret = TURN_NONE;
        delta = getDeltaAngle(oldAngle, newAngle);
        /* allow some slack. as ships do discrete turns, returning for
         * an exact match would cause the ship to shiver. */
        if (Math.abs(delta) > 2.0 * Math.PI / 64.0) {
            if (delta < 0.0)
                ret = TURN_RIGHT;
            else
                ret = TURN_LEFT;
        }
        return ret;
    }

    public final void addStar(Star s) {
        stars.addElement(s);
        addDecoration(s);
    }

    public final Star[] getStars() {
        Star[] ret;

        synchronized (stars) {
            ret = new Star[stars.size()];
            stars.copyInto(ret);
        }
        return ret;
    }

    public final void addPlayer(Player p) {
        players.addElement(p);
        if (p.isAlive())
            addCollidable(p.getShip());
    }

    public final void removePlayer(Player p) {
        players.removeElement(p);
        removeCollidable(p.getShip());
    }

    public final Player[] getPlayers() {
        Player[] ret;

        synchronized (players) {
            ret = new Player[players.size()];
            players.copyInto(ret);
        }
        return ret;
    }

    public final Player findPlayer(short id) {
        int    q, n;
        Player p;

        synchronized (players) {
            n = players.size();
            for (q = 0; q < n; q++) {
                p = (Player) players.elementAt(q);
                if (p.getId() == id)
                    return p;
            }
        }
        return null;
    }

    public final void addPhaser(Phaser p) {
        phasers.addElement(p);
        addCollidable(p);
    }

    public final void removePhaser(Phaser p) {
        phasers.removeElement(p);
        removeCollidable(p);
    }

    public final Phaser[] getPhasers() {
        Phaser[] ret;

        synchronized (phasers) {
            ret = new Phaser[phasers.size()];
            phasers.copyInto(ret);
        }
        return ret;
    }

    public final Phaser findPhaser(short id) {
        int      q, n;
        Phaser   p;
        Phaser[] pa;

        synchronized (phasers) {
            n = phasers.size();
            pa = new Phaser[n];
            phasers.copyInto(pa);
            for (q = 0; q < n; q++) {
                p = pa[q];
                if (p.getId() == id)
                    return p;
            }
        }
        return null;
    }

    public final void addBomb(Bomb b) {
        bombs.addElement(b);
        addCollidable(b);
    }

    public final void removeBomb(Bomb b) {
        bombs.removeElement(b);
        removeCollidable(b);
    }

    public final Bomb[] getBombs() {
        Bomb[] ret;

        synchronized (bombs) {
            ret = new Bomb[bombs.size()];
            bombs.copyInto(ret);
        }
        return ret;
    }

    public final Bomb findBomb(short id) {
        int    q, n;
        Bomb   b;
        Bomb[] ba;

        synchronized (bombs) {
            n = bombs.size();
            ba = new Bomb[n];
            bombs.copyInto(ba);
            for (q = 0; q < n; q++) {
                b = ba[q];
                if (b.getId() == id)
                    return b;
            }
        }
        return null;
    }

    public final void addBombPack(BombPack bp) {
        bombPacks.addElement(bp);
        addCollidable(bp);
    }

    public final void removeBombPack(BombPack bp) {
        bombPacks.removeElement(bp);
        removeCollidable(bp);
    }

    public final BombPack[] getBombPacks() {
        BombPack[] ret;

        synchronized (bombPacks) {
            ret = new BombPack[bombPacks.size()];
            bombPacks.copyInto(ret);
        }
        return ret;
    }

    public final BombPack findBombPack(short id) {
        int        q, n;
        BombPack   bp;
        BombPack[] bpa;

        synchronized (bombPacks) {
            n = bombPacks.size();
            bpa = new BombPack[n];
            bombPacks.copyInto(bpa);
            for (q = 0; q < n; q++) {
                bp = bpa[q];
                if (bp.getId() == id)
                    return bp;
            }
        }
        return null;
    }

    public final void addExplosion(Explosion e) {
        explosions.addElement(e);
        addDecoration(e);
    }

    public final void removeExplosion(Explosion e) {
        explosions.removeElement(e);
        removeDecoration(e);
    }

    public final Explosion[] getExplosions() {
        Explosion[] ret;

        synchronized (explosions) {
            ret = new Explosion[explosions.size()];
            explosions.copyInto(ret);
        }
        return ret;
    }

    public final Explosion findExplosion(short id) {
        int         q, n;
        Explosion   e;
        Explosion[] ea;

        synchronized (explosions) {
            n = explosions.size();
            ea = new Explosion[n];
            explosions.copyInto(ea);
            for (q = 0; q < n; q++) {
                e = ea[q];
                if (e.getId() == id)
                    return e;
            }
        }
        return null;
    }
}
