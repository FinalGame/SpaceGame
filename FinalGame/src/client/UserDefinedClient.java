package client;

import java.awt.*;

import objects.*;

/**
 * A class that may be subclassed to create robot clients, or clients
 * that extend the player's capabilities.
 * <P>
 *
 * The class provides several kinds of methods, some of which must be
 * overridden to have any effect:
 * <DL>
 *   <DT><B>Updater</B></DT>
 *     <DD>
 *       The updater is called by the main client
 *       several times a second. This method should be overridden to
 *       provide the main logic of the new client. There is no need
 *       to call the <CODE>super</CODE> method, as it's empty.
 *     </DD>
 *   <DT><B>Callbacks</B></DT>
 *     <DD>
 *       There are three kinds of callbacks: One lets you draw on the
 *       game board, some provide information
 *       from the game server, while others give information on how the
 *       user interacts with the client. The callbacks are all empty
 *       methods by default, and may be overridden on demand without
 *       calling the <CODE>super</CODE> method.
 *    </DD>
 *   <DT><B>Action Methods</B></DT>
 *     <DD>
 *       These methods make this client's player do something, like
 *       moving, fireing, and sending chat messages.
 *     </DD>
 *   <DT><B>Display Handling</B></DT>
 *     <DD>
 *       Provide (at the moment limited) access to the game display.
 *     </DD>
 *   <DT><B>Game Queries</B></DT>
 *     <DD>
 *       Methods for finding enemies, weapons, etc. Queries on single
 *       entities are not found here. Such queries must be directed
 *       to the object in question, thru the <CODE>objects</CODE>
 *       package.
 *     </DD>
 *   <DT><B>Convenience Methods</B></DT>
 *     <DD>
 *       Not directly connected to the game, these methods provide
 *       calculation of distances, angles, optimal turn direction, etc.
 *     </DD>
 * </DL>
 *
 * It is not possible to create advanced clients without diving into
 * the <A HREF="Package-objects.html">objects package</A>, that provide
 * information on players, weapons, etc.
 */
public class UserDefinedClient {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private Client client;
    private World world;
    private Board board;
    private BoardUpdater updater;

    /* difference in directions returned as degrees */
    private final int getDirectionDeltaDegrees(DrawableGameObject o) {
        return (int) ((getNormalizedAngle(getAngle(o)
                                          - getMyPlayer().getDirection()))
                      * (180.0 / Math.PI));
    }

/*-----------------------------------------------------------------------+
 |  PACKAGE LOCAL PART                                                   |
 +----------------------------------------------------------------------*/
    final void setClient(Client client) {
        this.client = client;
    }

    final void setWorld(World world) {
        this.world = world;
    }

    final void setBoard(Board board) {
        this.board = board;
    }

    final void setUpdater(BoardUpdater updater) {
        this.updater = updater;
    }

    final void setClasses(Client client, World world,
                          Board board, BoardUpdater updater) {
        setClient(client);
        setWorld(world);
        setBoard(board);
        setUpdater(updater);
    }

/*-----------------------------------------------------------------------+
 |  INHERITANTS' INTERFACE                                               |
 +----------------------------------------------------------------------*/
    /*---------------------------------------------------------------+
    |  The heartbeat updater.                                        |
    +---------------------------------------------------------------*/
    /**
     * Updates the client logic at regular intervals.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     */
    protected void update() {
    }

    /*---------------------------------------------------------------+
    |  The game board updater.                                       |
    +---------------------------------------------------------------*/
    /**
     * Lets you draw on the game board. The graphic context provided
     * is connected to an image that has size equal to the entire world.
     * You may thus use world coordinates when drawing.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     *
     * @param      g       the graphic context.
     */
    protected void annotateBoardImage(Graphics g) {
    }

    /*---------------------------------------------------------------+
    |  Callbacks for server info (includes info on our player.)      |
    +---------------------------------------------------------------*/
    /**
     * Informs that the client has successfully logged into the server.
     * When this method is called, everything is set up, and ready to
     * be used, including info on other players.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     */
    protected void infoLoggedIntoServer() {
    }

    /**
     * Informs that a new player has entered the game.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     *
     * @param      player  the player, which may include the client's
     *                     own player.
     * @see        objects.Player
     */
    protected void infoPlayerEntered(Player player) {
    }

    /**
     * Informs that a player has left the game.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     *
     * @param      player  the player, which may include the client's
     *                     own player.
     * @see        objects.Player
     */
    protected void infoPlayerLeft(Player player) {
    }

    /**
     * Informs that a player has moved or changed direction.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     *
     * @param      player  the player, which may include the client's
     *                     own player.
     * @see        objects.Player
     */
    protected void infoPlayerMoved(Player player) {
    }

    /**
     * Informs that a player has been hit by some weapon, without
     * necessarily being killed. If the player dies from the hit,
     * this call is followed by a call to <CODE>infoPlayerDied</CODE>.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     *
     * @param      player  the player, which may include the client's
     *                     own player.
     * @param      hitter  the player who's weapon hit the first player.
     * @param      weapon  the weapon type, as found in
     *                     <CODE>objects.WeaponType</CODE>.
     * @see        objects.Player
     * @see        objects.WeaponType
     */
    protected void infoPlayerHit(Player player, Player hitter, byte weapon) {
    }

    /**
     * Informs that a player has been killed.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     *
     * @param      player  the player, which may include the client's
     *                     own player.
     * @param      hitter  the player who killed the first player.
     * @param      weapon  the weapon type, as found in
     *                     <CODE>objects.WeaponType</CODE>.
     * @see        objects.Player
     * @see        objects.WeaponType
     */
    protected void infoPlayerDied(Player player, Player killer, byte weapon) {
    }

    /**
     * Informs that a player has resurrected from the dead.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     *
     * @param      player  the player, which may include the client's
     *                     own player.
     * @see        objects.Player
     */
    protected void infoPlayerResurrected(Player player) {
    }

    /**
     * Informs that a player has sent a global chat message.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     *
     * @param      player  the player, which may include the client's
     *                     own player.
     * @param      message the message sent by the player.
     * @see        objects.Player
     */
    protected void infoPlayerSaidSomething(Player player, String message) {
    }

    /**
     * Informs that a new phaser shot has been created.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     *
     * @param      phaser  the phaser shot.
     * @see        objects.Phaser
     */
    protected void infoPhaserCreated(Phaser phaser) {
    }

    /**
     * Informs that a phaser has moved a small distance.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     *
     * @param      phaser  the phaser shot.
     * @see        objects.Phaser
     */
    protected void infoPhaserMoved(Phaser phaser) {
    }

    /**
     * Informs that a phaser has been removed.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     *
     * @param      phaser  the phaser shot.
     * @see        objects.Phaser
     */
    protected void infoPhaserDestroyed(Phaser phaser) {
    }

    /**
     * Informs that a new targetting bomb has been created.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     *
     * @param      bomb    the bomb.
     * @see        objects.Bomb
     */
    protected void infoBombCreated(Bomb bomb) {
    }

    /**
     * Informs that a targetting bomb has moved to a new location.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     *
     * @param      bomb    the bomb.
     * @see        objects.Bomb
     */
    protected void infoBombMoved(Bomb bomb) {
    }

    /**
     * Informs that a targetting bomb has been removed, either because
     * it ran out of fuel, or because it hit something.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     *
     * @param      bomb    the bomb.
     * @see        objects.Bomb
     */
    protected void infoBombDestroyed(Bomb bomb) {
    }

    /**
     * Informs that someone has lost their weapons somewhere.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     *
     * @param      bombPack
     *                     the bomb pack.
     * @see        objects.BombPack
     */
    protected void infoBombPackCreated(BombPack bombPack) {
    }

    /**
     * Informs that a bomb pack has moved.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     *
     * @param      bombPack
     *                     the bomb pack.
     * @see        objects.BombPack
     */
    protected void infoBombPackMoved(BombPack bombPack) {
    }

    /**
     * Informs that a bomb pack has been removed, probably because
     * it was picked up by someone.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     *
     * @param      bombPack
     *                     the bomb pack.
     * @see        objects.BombPack
     */
    protected void infoBombPackDestroyed(BombPack bombPack) {
    }

    /*---------------------------------------------------------------+
    |  Callbacks for _unhandeled_ keyboard actions.                  |
    +---------------------------------------------------------------*/
    /**
     * Informs that the user has pressed a key that has no normal
     * function in the game.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     *
     * @param      code    the result from
     *                     <CODE>java.awt.event.KeyEvent.getKeyCode()</CODE>.
     * @see        java.awt.event.KeyEvent
     */
    protected void infoUnhandeledKeyPressed(int code) {
    }

    /**
     * Informs that the user has released a key that has no normal
     * function in the game.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     *
     * @param      code    the result from
     *                     <CODE>java.awt.event.KeyEvent.getKeyCode()</CODE>.
     * @see        java.awt.event.KeyEvent
     */
    protected void infoUnhandeledKeyReleased(int code) {
    }

    /*---------------------------------------------------------------+
    |  Callbacks for handeled keyboard actions.                      |
    +---------------------------------------------------------------*/
    /* methods in this category may be useful for disabling automated
     * moves when the user takes manual control. */
    /**
     * Informs that the user started to turn left. This information may
     * be useful if you want your client to avoid interfering with
     * the user's actions.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     */
    protected void infoManualStartTurningLeft() {
    }

    /**
     * Informs that the user stopped turning left. This information may
     * be useful if you want your client to avoid interfering with
     * the user's actions.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     */
    protected void infoManualStopTurningLeft() {
    }

    /**
     * Informs that the user started to turn right. This information may
     * be useful if you want your client to avoid interfering with
     * the user's actions.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     */
    protected void infoManualStartTurningRight() {
    }

    /**
     * Informs that the user stopped turning right. This information may
     * be useful if you want your client to avoid interfering with
     * the user's actions.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     */
    protected void infoManualStopTurningRight() {
    }

    /**
     * Informs that the user started to move foreward. This information may
     * be useful if you want your client to avoid interfering with
     * the user's actions.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     */
    protected void infoManualStartMovingForeward() {
    }

    /**
     * Informs that the user stopped moving foreward. This information may
     * be useful if you want your client to avoid interfering with
     * the user's actions.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     */
    protected void infoManualStopMovingForeward() {
    }

    /**
     * Informs that the user started to move backward. This information may
     * be useful if you want your client to avoid interfering with
     * the user's actions.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     */
    protected void infoManualStartMovingBack() {
    }

    /**
     * Informs that the user stopped moving backward. This information may
     * be useful if you want your client to avoid interfering with
     * the user's actions.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     */
    protected void infoManualStopMovingBack() {
    }

    /**
     * Informs that the user has fired the phaser. This information may
     * be useful if you want your client to avoid interfering with
     * the user's actions.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     */
    protected void infoManualFirePhaser() {
    }

    /**
     * Informs that the user has fired the bomb. This information may
     * be useful if you want your client to avoid interfering with
     * the user's actions.
     * <P>
     *
     * If you override this, there's no need to call the
     * <CODE>super</CODE> method.
     */
    protected void infoManualFireBomb() {
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    /** <CODE>getBestTurn</CODE> advices you to not turn. */
    public static final int TURN_NONE  = World.TURN_NONE;
    /** <CODE>getBestTurn</CODE> advices you to turn left. */
    public static final int TURN_LEFT  = World.TURN_LEFT;
    /** <CODE>getBestTurn</CODE> advices you to turn right. */
    public static final int TURN_RIGHT = World.TURN_RIGHT;

    /** Maximum phaser shot speed in pixels per second. */
    public static final int MAX_PHASER_SPEED = (15 * 15);
    /** Maximum bomb speed in pixels per second. */
    public static final int MAX_BOMB_SPEED   = (10 * 15);
    /** Maximum player/ship speed in pixels per second. */
    public static final int MAX_SHIP_SPEED   = ( 8 * 15);

    /**
     * <B>NOTE:</B> Your constructor should not call any of the
     * methods in this class, as things have not been set up yet.
     */
    public UserDefinedClient() {
    }

    /*---------------------------------------------------------------+
    |  Action                                                        |
    +---------------------------------------------------------------*/
    /**
     * Starts turning the ship to the left. The ship will continue to
     * turn left untill told to stop the movement in this direction,
     * or untill told to move in the opposite direction.
     */
    public final void startTurningLeft() {
        updater.startTurningLeft();
    }

    /**
     * Stops leftward movement of the ship.
     */
    public final void stopTurningLeft() {
        updater.stopTurningLeft();
    }

    /**
     * Starts turning the ship to the right. The ship will continue to
     * turn right untill told to stop the movement in this direction,
     * or untill told to move in the opposite direction.
     */
    public final void startTurningRight() {
        updater.startTurningRight();
    }

    /**
     * Stops rightward movement of the ship.
     */
    public final void stopTurningRight() {
        updater.stopTurningRight();
    }

    /**
     * Stops any turning of the ship.
     */
    public final void stopTurning() {
        updater.stopTurningLeft();
        updater.stopTurningRight();
    }

    /**
     * Starts moving the ship foreward. The ship will continue to
     * move foreward untill told to stop the movement in this direction,
     * or untill told to move in the opposite direction.
     */
    public final void startMovingForeward() {
        updater.startMovingForeward();
    }

    /**
     * Stops foreward movement of the ship.
     */
    public final void stopMovingForeward() {
        updater.stopMovingForeward();
    }

    /**
     * Starts moving the ship backward. The ship will continue to
     * move backward untill told to stop the movement in this direction,
     * or untill told to move in the opposite direction.
     */
    public final void startMovingBack() {
        updater.startMovingBack();
    }

    /**
     * Stops backward movement of the ship.
     */
    public final void stopMovingBack() {
        updater.stopMovingBack();
    }

    /**
     * Stops all foreward/backward movement of the ship.
     */
    public final void stopMoving() {
        updater.stopMovingForeward();
        updater.stopMovingBack();
    }

    /**
     * Fires a phaser shot. Note that the server may ignore our demand,
     * so you should not rely on it being performed untill you are
     * told using one of the callbacks.
     */
    public final void firePhaser() {
        updater.firePhaser();
    }

    /**
     * Fires a targeting bomb. Note that the server may ignore our demand,
     * so you should not rely on it being performed untill you are
     * told using one of the callbacks.
     */
    public final void fireBomb() {
        updater.fireBomb();
    }

    /**
     * Sends a message to all the players.
     *
     * @param      message the message to send.
     */
    public final void say(String message) {
        client.sendSay(message);
        client.flush();
    }

    /**
     * Sets new name for our player. The name is not stored in SpaceGame's
     * settings file.
     *
     * @param      name    the new name.
     */
    public final void setName(String name) {
        client.sendSetName(name);
        client.flush();
    }

    /*---------------------------------------------------------------+
    |  settings                                                      |
    +---------------------------------------------------------------*/
    /**
     * Instructs the main client code to autimatically resurrect
     * (bring back to life) after being killed.
     *
     * @param      status  <CODE>true</CODE> to turn on,
     *                     <CODE>false</CODE> to turn off.
     */
    public final void setAutoResurrect(boolean status) {
        updater.setAutoResurrect(status);
    }

    /*---------------------------------------------------------------+
    |  display                                                       |
    +---------------------------------------------------------------*/
    /**
     * Displays a message on the game board. This is a local message,
     * that may be used to inform the user of any action taken.
     *
     * @param      message the message to display.
     */
    public final void showMessage(String message) {
        board.addMessage(message);
    }

    /**
     * Returns the AWT Component that functions as the game are of the
     * window. May be used for capturing mouse events, etc.
     *
     * @return     the component.
     */
    public final Component getGameComponent() {
        return board.getComponent();
    }

    /**
     * Gets the horizontal offset into the world of the area currently
     * visible in the game window.
     *
     * @return     the horizontal offset.
     */
    public final int getVisibleOffsetX() {
        return board.getOffsetX();
    }

    /**
     * Gets the vertical offset into the world of the area currently
     * visible in the game window.
     *
     * @return     the vertical offset.
     */
    public final int getVisibleOffsetY() {
        return board.getOffsetY();
    }

    /*---------------------------------------------------------------+
    |  World info                                                    |
    +---------------------------------------------------------------*/
    /**
     * Returns the width of the entire playing area.
     *
     * @return     the width of the world in pixels.
     */
    public final int getWorldWidth() {
        return world.getWidth();
    }

    /**
     * Returns the height of the entire playing area.
     *
     * @return     the height of the world in pixels.
     */
    public final int getWorldHeight() {
        return world.getHeight();
    }

    /**
     * Returns the width of the current visible display are.
     *
     * @return     the width in pixels.
     */
    public final int getVisibleWidth() {
        return board.getWidth();
    }

    /**
     * Returns the height of the current visible display are.
     *
     * @return     the height in pixels.
     */
    public final int getVisibleHeight() {
        return board.getHeight();
    }

    /**
     * Finds the closest enemy player that is alive. This method
     * compares locations against a given point, that possibly lays
     * outside our client's ship location.
     *
     * @param      myX     x-coordinate to compare to.
     * @param      myY     y-coordinate to compare to.
     * @return     the object found, or <CODE>null</CODE> if no
     *             other players are present.
     * @see        objects.Player
     */
    public final Player getClosestEnemy(int myX, int myY) {
        return world.getClosestEnemy(getMyPlayer(), myX, myY);
    }

    /**
     * Finds the closest enemy player that is alive. This method
     * compares according to the location of your client's player.
     *
     * @return     the object found, or
     *             <CODE>null</CODE> if no other players are present.
     * @see        objects.Player
     */
    public final Player getClosestEnemy() {
        return world.getClosestEnemy(getMyPlayer());
    }

    /**
     * Finds the closest enemy bomb. This method
     * compares locations against a given point, that possibly lays
     * outside our client's ship location.
     *
     * @param      myX     x-coordinate to compare to.
     * @param      myY     y-coordinate to compare to.
     * @return     the object found, or <CODE>null</CODE> if no
     *             bombs owned by other players are present.
     * @see        objects.Bomb
     */
    public final Bomb getClosestEnemyBomb(int myX, int myY) {
        return world.getClosestEnemyBomb(getMyPlayer(), myX, myY);
    }

    /**
     * Finds the closest enemy bomb. This method
     * compares according to the location of your client's player.
     *
     * @return     the object found, or <CODE>null</CODE> if no
     *             bombs owned by other players are present.
     * @see        objects.Bomb
     */
    public final Bomb getClosestEnemyBomb() {
        return world.getClosestEnemyBomb(getMyPlayer());
    }

    /**
     * Finds the closest enemy phaser shot. This method
     * compares locations against a given point, that possibly lays
     * outside our client's ship location.
     *
     * @param      myX     x-coordinate to compare to.
     * @param      myY     y-coordinate to compare to.
     * @return     the object found, or <CODE>null</CODE> if no
     *             phasers owned by other players are present.
     * @see        objects.Phaser
     */
    public final Phaser getClosestEnemyPhaser(int myX, int myY) {
        return world.getClosestEnemyPhaser(getMyPlayer(), myX, myY);
    }

    /**
     * Finds the closest enemy phaser. This method
     * compares according to the location of your client's player.
     *
     * @return     the object found, or <CODE>null</CODE> if no
     *             phasers owned by other players are present.
     * @see        objects.Phaser
     */
    public final Phaser getClosestEnemyPhaser() {
        return world.getClosestEnemyPhaser(getMyPlayer());
    }

    /**
     * Finds the closest bomb pack. This method
     * compares locations against a given point, that possibly lays
     * outside our client's ship location.
     *
     * @param      myX     x-coordinate to compare to.
     * @param      myY     y-coordinate to compare to.
     * @return     the object found, or <CODE>null</CODE> if no
     *             bomb packs are present.
     * @see        objects.BombPack
     */
    public final BombPack getClosestBombPack(int myX, int myY) {
        return world.getClosestBombPack(myX, myY);
    }

    /**
     * Finds the closest bomb pack. This method
     * compares according to the location of your client's player.
     *
     * @return     the object found, or <CODE>null</CODE> if no
     *             bomb packs are present.
     * @see        objects.BombPack
     */
    public final BombPack getClosestBombPack() {
        return world.getClosestBombPack(getMyPlayer());
    }

    /**
     * Returns the player controlled by this client.
     *
     * @return     our <CODE>Player</CODE>-object.
     * @see        objects.Player
     */
    public final Player getMyPlayer() {
        return client.getMyPlayer();
    }

    /**
     * Gets all available players, including our own, and including
     * dead ones.
     *
     * @return     an array of <CODE>Player</CODE>-objects.
     * @see        objects.Player
     */
    public final Player[] getPlayers() {
        return world.getPlayers();
    }

    /**
     * Gets all currently moving phasers, including our own.
     *
     * @return     an array of <CODE>Phaser</CODE>-objects.
     * @see        objects.Phaser
     */
    public final Phaser[] getPhasers() {
        return world.getPhasers();
    }

    /**
     * Gets all currently moving bombs, including our own.
     *
     * @return     an array of <CODE>Bomb</CODE>-objects.
     * @see        objects.Bomb
     */
    public final Bomb[] getBombs() {
        return world.getBombs();
    }

    /**
     * Gets all currently available bomb packs.
     *
     * @return     an array of <CODE>BombPack</CODE>-objects.
     * @see        objects.BombPack
     */
    public final BombPack[] getBombPacks() {
        return world.getBombPacks();
    }

    /*---------------------------------------------------------------+
    |  Mathematical helper methods                                   |
    +---------------------------------------------------------------*/
    /**
     * Calculates the squared pixel distance between two objects. May be
     * used for comparing distances, as it is faster than the method
     * that calculates the real distance.
     *
     * @param      o1      one object.
     * @param      o2      another object.
     * @return     the squared pixel distance.
     * @see        objects.DrawableGameObject
     */
    public final int getSquareDistanceBetween(DrawableGameObject o1,
                                              DrawableGameObject o2) {
        return world.getSquareDistanceBetween(o1, o2);
    }

    /**
     * Calculates the pixel distance between two objects.
     *
     * @param      o1      one object.
     * @param      o2      another object.
     * @return     the pixel distance.
     * @see        objects.DrawableGameObject
     */
    public final int getDistanceBetween(DrawableGameObject o1,
                                        DrawableGameObject o2) {
        return world.getDistanceBetween(o1, o2);
    }

    /**
     * Calculates the squared pixel distance between an object and
     * a given location. May be used for comparing distances, as it
     * is faster than the method that calculates the real distance.
     *
     * @param      o1      an object.
     * @param      x       an x-coordinate.
     * @param      y       a y-coordinate.
     * @return     the squared pixel distance.
     * @see        objects.DrawableGameObject
     */
    public final int getSquareDistanceBetween(DrawableGameObject o,
                                              int x, int y) {
        return world.getSquareDistanceBetween(o, x, y);
    }

    /**
     * Calculates the pixel distance between an objects and a given
     * location.
     *
     * @param      o1      an object.
     * @param      x       an x-coordinate.
     * @param      y       a y-coordinate.
     * @return     the pixel distance.
     * @see        objects.DrawableGameObject
     */
    public final int getDistanceBetween(DrawableGameObject o, int x, int y) {
        return world.getDistanceBetween(o, x, y);
    }

    /**
     * Calculates the squared pixel distance between two points.
     * May be used for comparing distances, as it is faster than
     * the method that calculates the real distance.
     *
     * @param      x0      an x-coordinate.
     * @param      y0      a y-coordinate.
     * @param      x1      another x-coordinate.
     * @param      y1      another y-coordinate.
     * @return     the squared pixel distance.
     */
    public final int getSquareDistanceBetween(int x0, int y0, int x1, int y1) {
        return world.getSquareDistanceBetween(x0, y0, x1, y1);
    }

    /**
     * Calculates the pixel distance between two points.
     *
     * @param      x0      an x-coordinate.
     * @param      y0      a y-coordinate.
     * @param      x1      another x-coordinate.
     * @param      y1      another y-coordinate.
     * @return     the pixel distance.
     */
    public final int getDistanceBetween(int x0, int y0, int x1, int y1) {
        return world.getDistanceBetween(x0, y0, x1, y1);
    }

    /**
     * Calculates the angle in radians from one point to another.
     * The angle is the <I>visual</I> angle: 0 is to the right,
     * PI/2 is up, etc. You may have to think a little bit when
     * using this, as the screen's y-axis is positive downwards.
     *
     * @param      fromX   start point x-coordinate.
     * @param      fromY   start point y-coordinate.
     * @param      toX     end point x-coordinate.
     * @param      toY     end point y-coordinate.
     * @return     the angle in radians.
     */
    public final double getAngle(int fromX, int fromY, int toX, int toY) {
        return world.getAngle(fromX, fromY, toX, toY);
    }

    /**
     * Calculates the angle in radians of a vector from our ship to
     * the given object. The angle is the <I>visual</I> angle:
     * 0 is to the right,
     * PI/2 is up, etc. You may have to think a little bit when
     * using this, as the screen's y-axis is positive downwards.
     *
     * @param      o       the object to find the direction to.
     * @return     the angle in radians.
     */
    public final double getAngle(DrawableGameObject o) {
        Point myLoc, objLoc;

        myLoc = getMyPlayer().getLocation();
        objLoc = o.getLocation();
        return world.getAngle(myLoc.x, myLoc.y, objLoc.x, objLoc.y);
    }

    /**
     * Makes sure an angle is within the interval 0.0 to 2PI. May be
     * useful when working with angle differences.
     * Note that it will not work for values > 4PI or < -2PI (more than
     * one round around the circle)!
     *
     * @param      angle   the angle to normalize.
     * @return     the normalized angle.
     */
    public final double getNormalizedAngle(double angle) {
        if (angle >= 2.0 * Math.PI)
            angle -= 2.0 * Math.PI;
        else if (angle < 0.0)
            angle += 2.0 * Math.PI;
        return angle;
    }

    /**
     * Calculates the smallest angle from one angle to another.
     * The angle found is the one that must be added to the
     * <CODE>fromAngle</CODE> to reach the <CODE>toAngle</CODE>,
     * without moving more than half way through the circle.
     * <P>
     *
     * The input angles must be between 0 and 2PI.
     * The output is between -PI and PI.
     *
     * @param      fromAngle
     *                     the angle to start from.
     * @param      toAngle the angle to end up with.
     * @return     the angle difference.
     */
    public final double getDeltaAngle(double fromAngle, double toAngle) {
        return world.getDeltaAngle(fromAngle, toAngle);
    }

    /**
     * Calculates the smallest angle between two given
     * angles, without taking care of direction. The smallest angle
     * is the one that is less than or equal to the half circle.
     * <P>
     *
     * The input angles must be between 0 and 2PI.
     * The output is between 0 and PI.
     *
     * @param      angle1  one angle.
     * @param      angle2  another angle.
     * @return     the angle between the two angles.
     */
    public final double getAbsDeltaAngle(double angle1, double angle2) {
        return world.getAbsDeltaAngle(angle1, angle2);
    }

    /**
     * Finds the best way to turn to move from one direction to another.
     * Very useful for aiming your ship against other players.
     *
     * @param      oldAngle
     *                     the current angle of direction.
     * @param      newAngle
     *                     the wanted angle of direction.
     * @return     the best turn: <CODE>TURN_NONE</CODE>,
     *             <CODE>TURN_LEFT</CODE>, or <CODE>TURN_RIGHT</CODE>.
     */
    public final int getBestTurn(double oldAngle, double newAngle) {
        return world.getBestTurn(oldAngle, newAngle);
    }

    /**
     * Checks if the given object is in front of our ship. Given a
     * line thru the ship's center, perpendicular to the ship's pointing
     * direction, any objects on the same side as the ship's nose are
     * said to be in front.
     *
     * @param      o       the object to check.
     * @return     <CODE>true</CODE> if the object is in front,
     *             <CODE>false</CODE> otherwise.
     */
    public final boolean isInFront(DrawableGameObject o) {
        return (getAbsDeltaAngle(getMyPlayer().getDirection(),
                                 getAngle(o))
                < Math.PI / 2);
    }

    /**
     * Checks if the given object is behind our ship. Given a
     * line thru the ship's center, perpendicular to the ship's pointing
     * direction, any objects on the opposite side as the ship's nose are
     * said to be behind.
     *
     * @param      o       the object to check.
     * @return     <CODE>true</CODE> if the object is behind,
     *             <CODE>false</CODE> otherwise.
     */
    public final boolean isBehind(DrawableGameObject o) {
        return (getAbsDeltaAngle(getMyPlayer().getDirection(),
                                 getAngle(o))
                >= Math.PI / 2);
    }

    /**
     * Checks if the given object is to the left of our ship.
     *
     * @param      o       the object to check.
     * @return     <CODE>true</CODE> if the object is to the left,
     *             <CODE>false</CODE> otherwise.
     */
    public final boolean isToTheLeft(DrawableGameObject o) {
        return (getDeltaAngle(getMyPlayer().getDirection(),
                              getAngle(o))
                > 0.0);
    }

    /**
     * Checks if the given object is to the right of our ship.
     *
     * @param      o       the object to check.
     * @return     <CODE>true</CODE> if the object is to the right,
     *             <CODE>false</CODE> otherwise.
     */
    public final boolean isToTheRight(DrawableGameObject o) {
        return (getDeltaAngle(getMyPlayer().getDirection(),
                              getAngle(o))
                < 0.0);
    }

    /*---------------------------------------------------------------+
    |  Misc                                                          |
    +---------------------------------------------------------------*/
    /**
     * Returns the number of times per second that the
     * <CODE>update</CODE> method is called.
     *
     * @return     the update frequency in Hz.
     * @see        #update
     */
    public final int getUpdateFrequency() {
        return updater.getUpdateFrequency();
    }
}
