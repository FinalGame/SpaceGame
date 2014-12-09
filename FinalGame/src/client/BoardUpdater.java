package client;

import objects.*;
final class BoardUpdater
implements Runnable {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private static final int updateFrequency = 15; /* Hz */

    private Thread thread;
    private boolean done;
    private Client            client;
    private Board             board;
    private UserDefinedClient userClient;

    private byte              turn;
    private byte              thrust;
    private boolean           phaser;
    private boolean           bomb;
    private boolean           autoResurrect;

/*-----------------------------------------------------------------------+
 |  PACKAGE LOCAL PART                                                   |
 +----------------------------------------------------------------------*/
    final void setUserClient(UserDefinedClient userClient) {
        this.userClient = userClient;
    }

    final void playerDied() {
    }

    final void playerResurrected() {
        turn   = 0;
        thrust = 0;
        phaser = false;
        bomb   = false;
    }

    final void startTurningLeft() {
        turn = 1;
    }

    final void stopTurningLeft() {
        if (turn > 0)
            turn = 0;
    }

    final void startTurningRight() {
        turn = -1;
    }

    final void stopTurningRight() {
        if (turn < 0)
            turn = 0;
    }

    final void startMovingForeward() {
        thrust = 1;
    }

    final void stopMovingForeward() {
        if (thrust > 0)
            thrust = 0;
    }

    final void startMovingBack() {
        thrust = -1;
    }

    final void stopMovingBack() {
        if (thrust < 0)
            thrust = 0;
    }

    final void firePhaser() {
        phaser = true;
    }

    final void fireBomb() {
        bomb = true;
    }

    final void setAutoResurrect(boolean status) {
        autoResurrect = status;
    }

    final boolean getAutoResurrect() {
        return autoResurrect;
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public BoardUpdater(Board board, Client client) {
        this.board  = board;
        this.client = client;

        turn   = 0;
        thrust = 0;
        phaser = false;
        bomb   = false;

        autoResurrect = false;

        done = false;
        thread = new Thread(this);
        thread.setDaemon(true);
    }

    public final int getUpdateFrequency() {
        return updateFrequency;
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
        long   before, delta;
        long   updateMillis = 1000 / updateFrequency;
        int    lastTurn = 0;
        int    lastThrust = 0;
        Player me;

        while (!done) {
            before = System.currentTimeMillis();
            if ((me = client.getMyPlayer()) != null) {
                if (me.isAlive()) {
                    if (userClient != null)
                        userClient.update();
                    if (turn != lastTurn) {
                        client.sendSetTurn(turn);
                        lastTurn = turn;
                    }
                    if (thrust != lastThrust) {
                        client.sendSetThrust(thrust);
                        lastThrust = thrust;
                    }
                    if (phaser) {
                        if (!me.isPhaserOverheated())
                            client.sendFirePhaser();
                        phaser = false;
                    }
                    if (bomb) {
                        if (me.getBombsLeft() > 0)
                            client.sendFireBomb();
                        bomb = false;
                    }
                } else {
                    if (phaser || bomb || autoResurrect)
                        if (System.currentTimeMillis()
                            > client.myDeathTime + 3000)
                            client.sendResurrectMe();
                    phaser     = false;
                    bomb       = false;
                    lastTurn   = 0;
                    lastThrust = 0;
                }
                client.flush();
                board.updateImage(me);
            }
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
