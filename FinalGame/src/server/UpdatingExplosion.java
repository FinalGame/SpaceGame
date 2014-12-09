package server;

import java.awt.*;

import objects.*;
final class UpdatingExplosion
extends Explosion {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private static short nextId = 0;

    private Server  server;
    private World   world;
    private boolean remove;

/*-----------------------------------------------------------------------+
 |  PACKAGE LOCAL PART                                                   |
 +----------------------------------------------------------------------*/
    static final short getNextId() {
        return nextId++;
    }

    /* called by the updater daemon */
    final synchronized boolean update() {
        setLevel((byte) (getLevel() + 1));
        if (getLevel() > getMaxLevel())
            remove = true;
        return true;
    }

    final boolean doRemove() {
        return remove;
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public UpdatingExplosion(Server server, World world, short id,
                             int x, int y, byte level, byte maxLevel) {
        super(id, x, y, level, maxLevel);

        this.server = server;
        this.world = world;
        remove = false;
    }
}
