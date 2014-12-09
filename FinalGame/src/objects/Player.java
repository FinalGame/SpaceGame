package objects;

import java.awt.*;

import main.*;

/**
 * This is a player class. It contains information on a player that
 * currently attends to the game.
 * <P>
 *
 * If you write a user defined client, you should not try change this
 * object by calling any of it's setter methods, as it will make
 * your view of the game different from all the other players' views.
 * <P>
 *
 * Also note that for clients, not all getter methods return usable
 * results, as the server doesn't send everything across. The following
 * information is, however, updated and usable for this class:
 * <UL>
 *   <LI>Name</LI>
 *   <LI>Location</LI>
 *   <LI>Direction</LI>
 *   <LI>Alive/dead status</LI>
 *   <LI>Score</LI>
 *   <LI>Ship</LI>
 * </UL>
 * In addition, if the player object matches the player controlled by
 * this client, the following informatin is available:
 * <UL>
 *   <LI>Damage status</LI>
 *   <LI>Phaser heat status</LI>
 *   <LI>Number of bombs left</LI>
 * </UL>
 */
public class Player {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private static final int MAX_BOMBS = 50;
    private static final int VIEW_LIMIT_WIDTH = Main.MAX_BOARD_WIDTH / 2 + 30;
    private static final int VIEW_LIMIT_HEIGHT = Main.MAX_BOARD_HEIGHT / 2 + 30;

    private short   id;
    private String  name;
    private int     score;
    private boolean alive;
    private int     antiScore;
    private Ship    ship;
    private int     damage;     /* 0 - 100 */
    private int     phaserHeat; /* 0 - 100 */
    private int     bombsLeft;

    private void setup() {
        id = -1;
        name = "";
        score = 0;
        ship = new Ship(this);
        alive = true;
        antiScore = 0;
        damage = 0;
        phaserHeat = 0;
        bombsLeft = 5;
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    /**
     * Constructs a new player with the default settings.
     */
    public Player() {
        setup();
    }

    /**
     * Constructs a new player with the given id and name.
     *
     * @param      id      the ID of this player.
     * @param      name    the name.
     */
    public Player(short id, String name) {
        this();
        setId(id);
        setName(name);
    }

    /**
     * Sets the ID of this player. The ID is used for identifying
     * players when talking to the server.
     *
     * @param      id      the ID of this player.
     */
    public final void setId(short id) {
        this.id = id;
    }

    /**
     * Fetches the ID of this player. The ID is used for identifying
     * players when talking to the server.
     *
     * @return     the ID of this player.
     */
    public final short getId() {
        return id;
    }

    /**
     * Sets the name of this player.
     *
     * @param      name    the name.
     */
    public final synchronized void setName(String name) {
        if (name.length() > 15)
            name = name.substring(0, 15);
        this.name = name;
    }

    /**
     * Fetches the name of this player.
     *
     * @return     the name.
     */
    public final synchronized String getName() {
        return name;
    }

    /**
     * Sets the current score. The score is the number of kills done
     * by this player.
     *
     * @param      score   the new score.
     */
    public final void setScore(int score) {
        this.score = score;
    }

    /**
     * Increases the number of kills done by this player.
     */
    public final void incScore() {
        ++score;
    }

    /**
     * Fetches the number of kills done by this player.
     *
     * @return     the number of kills.
     */
    public final int getScore() {
        return score;
    }

    /**
     * Sets the location of this object within the world.
     * This method maps directly to the matching method within
     * the player's <CODE>Ship</CODE> object.
     *
     * @param      loc     the location.
     * @see        objects.Ship
     */
    public void setLocation(Point loc) {
        ship.setLocation(loc);
    }

    /**
     * Sets the location of this object within the world.
     * This method maps directly to the matching method within
     * the player's <CODE>Ship</CODE> object.
     *
     * @param      x       the x-coordinate.
     * @param      y       the y-coordinate.
     * @see        objects.Ship
     */
    public void setLocation(int x, int y) {
        ship.setLocation(x, y);
    }

    /**
     * Fetches the location of this player within the world.
     * This method maps directly to the matching method within
     * the player's <CODE>Ship</CODE> object.
     *
     * @return     the location.
     * @see        objects.Ship
     */
    public final Point getLocation() {
        return ship.getLocation();
    }

    /**
     * Sets the direction of this object as an angle in radians.
     * This method maps directly to the matching method within
     * the player's <CODE>Ship</CODE> object.
     *
     * @param      angle   the angle in radians.
     * @see        objects.Ship
     */
    public final void setDirection(double angle) {
        ship.setDirection(angle);
    }

    /**
     * Returns the direction of this object in radians.
     * This method maps directly to the matching method within
     * the player's <CODE>Ship</CODE> object.
     *
     * @return     the direction.
     * @see        objects.Ship
     */
    public final double getDirection() {
        return ship.getDirection();
    }

    /**
     * Sets the direction of this object, encoded as a short, as sent
     * from the game server.
     * This method maps directly to the matching method within
     * the player's <CODE>Ship</CODE> object.
     *
     * @param      angle   the angle encoded as a short.
     * @see        objects.Ship
     */
    public final void setDirectionFromShort(short angle) {
        ship.setDirectionFromShort(angle);
    }

    /**
     * Returns the direction of this object in radians, encoded in a short
     * for transfer to the game server.
     * This method maps directly to the matching method within
     * the player's <CODE>Ship</CODE> object.
     *
     * @return     the direction.
     * @see        objects.Ship
     */
    public final short getDirectionAsShort() {
        return ship.getDirectionAsShort();
    }

    /**
     * Returns the <CODE>Ship</CODE> object that is controlled by this
     * player.
     *
     * @return     the <CODE>Ship</CODE> object.
     */
    public final Ship getShip() {
        return ship;
    }

    /**
     * Sets whether this player is alive.
     *
     * @param      alive   <CODE>true</CODE> to indicate that the player
     *                     is alive, <CODE>false</CODE> to indicate that
     *                     he/she is dead.
     */
    public final void setAlive(boolean alive) {
        this.alive = alive;
    }

    /**
     * Checks if this player is dead or alive.
     *
     * @return     <CODE>true</CODE> if the player is alive,
     *             <CODE>false</CODE> otherwise.
     */
    public final boolean isAlive() {
        return alive;
    }

    /**
     * Sets the damage level of this player. Damage is between 0 and 100,
     * with 100 indicating that the player is destroyed.
     *
     * @param      damage  the damage level.
     */
    public final void setDamage(int damage) {
        this.damage = damage;
    }

    /**
     * Increases the damage by the given amount. If damage gets above
     * 100, it is set to 100.
     *
     * @param      add     the amount to add.
     */
    public final void incDamage(int add) {
        if ((damage += add) > 100)
            damage = 100;
    }

    /**
     * Decreases the damage by the given amount. If damage gets below
     * 0, it is set to 0.
     *
     * @param      sub     the amount to subtract.
     */
    public final void decDamage(int sub) {
        if ((damage -= sub) < 0)
            damage = 0;
    }

    /**
     * Gets the current damage level. Damage is between 0 and 100,
     * with 100 indicating that the player is destroyed.
     *
     * @return     the damage level.
     */
    public final int getDamage() {
        return damage;
    }

    /**
     * Sets the phaser cannon heat percent of this player.
     * The heat is a number between 0 and 100. Values above 75 or so
     * indicates that the cannon is overheated, and it won't fire.
     *
     * @param      phaserHeat
     *                     the phaser heat value.
     */
    public final void setPhaserHeat(int phaserHeat) {
        this.phaserHeat = phaserHeat;
    }

    /**
     * Increases phaser heat by the given amount, making sure it doesn't
     * get above 100.
     *
     * @param      add     the number to add.
     */
    public final void incPhaserHeat(int add) {
        if ((phaserHeat += add) > 100)
            phaserHeat = 100;
    }

    /**
     * Decreases phaser heat by the given amount, making sure it doesn't
     * get below zero.
     *
     * @param      sub     the number to subtract.
     */
    public final void decPhaserHeat(int sub) {
        if ((phaserHeat -= sub) < 0)
            phaserHeat = 0;
    }

    /**
     * Gets the current phaser heat value.
     * The heat is a number between 0 and 100. Values above 75 or so
     * indicates that the cannon is overheated, and it won't fire.
     *
     * @return     the phaser heat value.
     */
    public final int getPhaserHeat() {
        return phaserHeat;
    }

    /**
     * Checks if the phaser cannon is currently overheated.
     *
     * @return     <CODE>true</CODE> if overheated,
     *             <CODE>false</CODE> otherwise.
     */
    public final boolean isPhaserOverheated() {
        return (phaserHeat > 75);
    }

    /**
     * Sets the available number of targeting bombs.
     *
     * @param      bombsLeft
     *                     the number of bombs.
     */
    public final void setBombsLeft(int bombsLeft) {
        if (bombsLeft > MAX_BOMBS)
            bombsLeft = MAX_BOMBS;
        this.bombsLeft = bombsLeft;
    }

    /**
     * Increases the number of bombs left by the given number.
     *
     * @param      add     the number to add.
     */
    public final void incBombsLeft(int add) {
        bombsLeft += add;
        if (bombsLeft > MAX_BOMBS)
            bombsLeft = MAX_BOMBS;
    }

    /**
     * Decreases the number of bombs left by the given number.
     *
     * @param      sub     the number to subtract.
     */
    public final void decBombsLeft(int sub) {
        bombsLeft -= sub;
        if (bombsLeft < 0)
            bombsLeft = 0;
    }

    /**
     * Gets the number of available targeting bombs.
     *
     * @return     the number of bombs left.
     */
    public final int getBombsLeft() {
        return bombsLeft;
    }

    /**
     * Sets the number of times this player has been killed.
     *
     * @param      antiScore
     *                     the number of times killed.
     */
    public final void setAntiScore(int antiScore) {
        this.antiScore = antiScore;
    }

    /**
     * Increases the number of times this player has been killed.
     */
    public final void incAntiScore() {
        ++antiScore;
    }

    /**
     * Returns the number of times this player has been killed.
     *
     * @return     number of times killed.
     */
    public final int getAntiScore() {
        return antiScore;
    }

    /**
     * Gets a ratio between the number of kills and the number of
     * times being killed.
     *
     * @return     the ratio.
     * @author     Jon S. Bratseth
     */
    public final int getRatio() {
        if (getAntiScore() == 0)
            return getScore() * 200; /* infinite is too much */
        return (int) ((getScore() * 100) / getAntiScore());
    }

    /**
     * Creates a string that describes the ratio between the number
     * of kills and the number of times killed. Used when scores
     * are displayed.
     *
     * @return     a string.
     */
    public final String getRatioString() {
        int          q;
        String       whole, frac;
        StringBuffer ratio;

        ratio = new StringBuffer();
        if (getAntiScore() == 0) {
            whole = String.valueOf(getScore() * 2); /* infinite is too much */
            frac = "0";
        } else {
            whole = String.valueOf((int) getRatio() / 100);
            frac = String.valueOf(getRatio() % 100);
        }
        for (q = whole.length(); q < 4; q++) /* works for ratio < 1000 */
            ratio.append((char) ' ');
        ratio.append(whole);

        if (frac.equals("0"))
            ratio.append(".00");
        else {
            ratio.append((char) '.');
            if (frac.length() == 1)
                ratio.append((char) '0');
            ratio.append(frac);
        }
        return ratio.toString();
    }

    public final boolean isInView(DrawableGameObject o) {
        Point p, op;

        p = getLocation();
        op = o.getLocation();
        if (op.x < p.x - VIEW_LIMIT_WIDTH)
            return false;
        if (op.x > p.x + VIEW_LIMIT_WIDTH)
            return false;
        if (op.y < p.y - VIEW_LIMIT_HEIGHT)
            return false;
        if (op.y > p.y + VIEW_LIMIT_HEIGHT)
            return false;
        return true;
    }
}
