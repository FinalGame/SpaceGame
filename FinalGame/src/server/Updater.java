
package server;

import java.io.*;
import java.net.*;
import java.awt.*;

import objects.*;

final class Updater
implements Runnable {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    Thread thread;
    boolean done;
    private Server server;
    private World  world;

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public Updater(Server server, World world) {
        this.server = server;
        this.world = world;
        done = false;
        thread = new Thread(this);
        thread.setDaemon(true);
    }

    public void start() {
        thread.start();
    }

    public void stop() {
        done = true;
        thread.interrupt();
    }

    /* Runnable *********************************************************/
    public void run() {
        int               q, n, counter;
        long              before, delta;
        /* IMPORTANT: update max speeds in UserDefinedClient if changing
         * update frequency! */
        long              updateFrequency = 15;
        long              updateMillis = 1000 / updateFrequency;
        Explosion[]       explosions;
        UpdatingExplosion explosion;
        Phaser[]          phasers;
        UpdatingPhaser    phaser;
        Bomb[]            bombs;
        UpdatingBomb      bomb;
        BombPack[]        bombPacks;
        UpdatingBombPack  bombPack;
        Player[]          players;
        UpdatingPlayer    player;

        counter = 0;
        while (!done) {
            before = System.currentTimeMillis();

            /* update explosions */
            explosions = world.getExplosions();
            n = explosions.length;
            for (q = 0; q < n; q++) {
                explosion = (UpdatingExplosion) explosions[q];
                if (explosion.doRemove()) {
                    world.removeExplosion(explosion);
                    server.sendRemoveExplosion(explosion);
                } else if (explosion.update())
                    server.sendSetExplosionLevel(explosion);
            }

            /* update phasers */
            phasers = world.getPhasers();
            n = phasers.length;
            for (q = 0; q < n; q++) {
                phaser = (UpdatingPhaser) phasers[q];
                if (phaser.doRemove()) {
                    world.removePhaser(phaser);
                    server.sendRemovePhaser(phaser);
                } else if (phaser.update())
                    server.sendSetPhaserPosition(phaser);
            }

            /* update bombs */
            bombs = world.getBombs();
            n = bombs.length;
            for (q = 0; q < n; q++) {
                bomb = (UpdatingBomb) bombs[q];
                if (bomb.doRemove()) {
                    world.removeBomb(bomb);
                    server.sendRemoveBomb(bomb);
                } else if (bomb.update())
                    server.sendSetBombPosition(bomb);
            }

            /* update bomb packs */
            bombPacks = world.getBombPacks();
            n = bombPacks.length;
            for (q = 0; q < n; q++) {
                bombPack = (UpdatingBombPack) bombPacks[q];
                if (bombPack.doRemove()) {
                    world.removeBombPack(bombPack);
                    server.sendRemoveBombPack(bombPack);
                } else if (bombPack.update())
                    server.sendSetBombPackPosition(bombPack);
            }

            /* update players */
            players = world.getPlayers();
            n = players.length;
            for (q = 0; q < n; q++) {
                player = (UpdatingPlayer) players[q];
                if (player.update())
                    server.sendSetPlayerPosition(player);
                player.setNewPhaserOk(true);
            }

            /* possibly update damage and phaser heat */
            if (++counter >= updateFrequency) { /* each second */
                counter = 0;
                players = world.getPlayers();
                n = players.length;
                for (q = 0; q < n; q++) {
                    player = (UpdatingPlayer) players[q];
                    if (player.isAlive()) {
                        player.decDamage(1);
                        player.decPhaserHeat((int) (Math.random() * 7) + 5);
                        player.getClientHandler().sendSetPlayerStatus();
                    }
                }
            }

            /* make sure every packet is delivered. */
            server.flush();

            delta = System.currentTimeMillis() - before;
            if (delta < updateMillis) {
                try {
                    Thread.sleep(updateMillis - delta);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
