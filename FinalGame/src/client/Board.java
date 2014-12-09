package client;

import java.awt.*;
import java.awt.event.*;

import no.shhsoft.awt.event.*;

import objects.*;

/**
 * Handles the visible playground of the game.
 *
 * In addition to drawing all components of the visible game surface,
 * this class also takes care of keyboard input, passing key press and
 * release events to the Game class, the main class of the game.
 */
public final class Board
extends Canvas
implements KeyPressedReleasedListener {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private MainFuncProvider  mainFunc;
    private World             world;
    private BoardUpdater      updater;
    private UserDefinedClient userClient;
    private Client            client;
    private int               width, height;
    private int               radarWidth, radarHeight;
    private int               radarX, radarY;
    private int               pctWidth;
    private int               offsx = 0, offsy = 0;
    private Image             img;
    private Graphics          gimg;
    private Rectangle         bounds;
    private TimeoutMessages   messages;
    private TimeoutMessages   chatMessages;
    private boolean           showScores;
    private boolean           showHelp;
    private String            scoreText;
    private Font              normalFont;
    private Font              smallFont;
    private boolean           allowPhaser;
    private boolean           allowBomb;

    private final void drawText(int x, int y, String txt, Color col) {
        int         q, n, ch;
        int         txtLen, from, to, idx;
        FontMetrics fm;

        txtLen = txt.length();

        gimg.setColor(col);
        fm = gimg.getFontMetrics();
        ch = fm.getAscent() + fm.getDescent();

        from = 0;
        do {
            idx = txt.indexOf('\n', from);
            to = idx >= 0 ? idx : txtLen;

            gimg.drawString(txt.substring(from, to), x, y);
            y += ch;

            if (idx == txtLen - 1) /* trailing blank line */
                break;
            from = idx + 1;
        } while (idx >= 0);
    }

    private final void drawCenteredText(String txt, Color col, boolean clear) {
        int         x, y, cw, ch, tw, th;
        int         txtLength, numLines, longest, length, from, to, idx;
        FontMetrics fm;

        txtLength = txt.length();

        /* find number of lines, and length of longest line. */
        numLines = 0;
        longest = 0;
        from = 0;
        do {
            idx = txt.indexOf('\n', from);
            length = (idx >= 0 ? idx : txtLength) - from;
            if (length > longest)
                longest = length;
            from = idx + 1;
            ++numLines;
        } while (idx >= 0 && idx < txtLength - 1);

        fm = gimg.getFontMetrics();
        cw = fm.charWidth('a');
        ch = fm.getAscent() + fm.getDescent();

        tw = cw * longest;
        th = ch * numLines;

        x = (width - tw) / 2;
        y = (height - th) / 2;

        if (clear)
            gimg.clearRect(x - 10, y - fm.getAscent() - 10, tw + 20, th + 20);

        drawText(x, y, txt, col);
    }

    private final void drawRadar(int offsx, int offsy) {
        int        q, n;
        Player[]   players;
        Player     player;
        BombPack[] bombPacks;
        BombPack   bombPack;
        Ship       ship;
        Point      loc;
        int        x, y;
        int        worldWidth, worldHeight;

        worldWidth = world.getWidth();
        worldHeight = world.getHeight();

        /* clear and border the radar window */
        gimg.clearRect(radarX, radarY, radarWidth, radarHeight);
        gimg.setColor(Color.green);
        gimg.drawRect(radarX, radarY, radarWidth, radarHeight);
        gimg.setClip(radarX + 1, radarY + 1, radarWidth - 1, radarHeight - 1);
        gimg.translate(radarX, radarY);

        /* show a frame indicating the area seen in the window */
        gimg.setColor(Color.gray);
        gimg.drawRect((offsx * radarWidth) / worldWidth,
                      (offsy * radarHeight) / worldHeight,
                      (width * radarWidth) / worldWidth,
                      (height * radarHeight) / worldHeight);

        /* place dots for each player */
        gimg.setColor(Color.green);
        players = world.getPlayers();
        n = players.length;
        for (q = 0; q < n; q++) {
            player = (Player) players[q];
            if (!player.isAlive())
                continue;
            ship = player.getShip();
            loc = ship.getLocation();
            x = 1 + (loc.x * (radarWidth - 1)) / worldWidth;
            y = 1 + (loc.y * (radarHeight - 1)) / worldHeight;
            gimg.drawLine(x, y, x, y);
        }

        /* place dots for each bomb pack */
        gimg.setColor(Color.red);
        bombPacks = world.getBombPacks();
        n = bombPacks.length;
        for (q = 0; q < n; q++) {
            bombPack = (BombPack) bombPacks[q];
            loc = bombPack.getLocation();
            x = 1 + (loc.x * (radarWidth - 1)) / worldWidth;
            y = 1 + (loc.y * (radarHeight - 1)) / worldHeight;
            gimg.drawLine(x, y, x, y);
        }

        gimg.translate(-radarX, -radarY);
        gimg.setClip(0, 0, width, height);
    }

    private final void drawStatus(Player me) {
        int         offsx, offsy;
        int         q, x, xx, y, ch, cw, w, w2, n;
        String[]    msg;
        FontMetrics fm;

        fm = gimg.getFontMetrics();
        ch = fm.getAscent();
        cw = fm.charWidth('a');
        offsx = 10;
        offsy = height - 3 * (ch + fm.getDescent()) - 2;
        x = offsx;
        xx = x + 8 * cw; /* from the longest text string */
        y = offsy + ch;

        gimg.setColor(Color.green);

        gimg.drawString("Bombs:", x, y);
        gimg.drawString(String.valueOf(me.getBombsLeft()), xx, y);

        y += ch + fm.getDescent();

        gimg.drawString("Heat:", x, y);
        n = me.getPhaserHeat();
        gimg.drawRect(xx, y - ch, pctWidth, ch);
        if (n < 75) {
            w = (n * pctWidth) / 100;
            gimg.fillRect(xx + 1, y - ch + 1, w - 1, ch - 1);
        } else {
            w = (75 * pctWidth) / 100;
            w2 = (n * pctWidth) / 100;
            gimg.fillRect(xx + 1, y - ch + 1, w - 1, ch - 1);
            gimg.setColor(Color.red);
            gimg.fillRect(xx + w + 1, y - ch + 1, w2 - w - 1, ch - 1);
            gimg.setColor(Color.green);
        }

        y += ch + fm.getDescent();

        gimg.drawString("Damage:", x, y);
        n = me.getDamage();
        w = (n * pctWidth) / 100;
        gimg.drawRect(xx, y - ch, pctWidth, ch);
        gimg.setColor(Color.red);
        gimg.fillRect(xx + 1, y - ch + 1, w - 1, ch - 1);
        /* color is red here */
    }

    private final void drawTimeoutMessages(TimeoutMessages messages,
                                           Color color,
                                           int offsx, int offsy) {
        int         q, x, y, ch;
        String[]    msg;
        FontMetrics fm;

        msg = messages.getAll();
        if (msg.length == 0)
            return;

        gimg.setColor(color);
        fm = gimg.getFontMetrics();
        ch = fm.getAscent() + fm.getDescent();
        x = offsx;
        y = offsy + ch;
        for (q = 0; q < msg.length; q++) {
            gimg.drawString(msg[q], x, y);
            y += ch;
        }
    }

    private final void drawMessages(int offsx, int offsy) {
        drawTimeoutMessages(messages, Color.orange, offsx, offsy);
    }

    private final void drawChatMessages(int offsx, int offsy) {
        drawTimeoutMessages(chatMessages, Color.yellow, offsx, offsy);
    }

    private final void drawScores() {
        if (!showScores)
            return;

        gimg.setFont(smallFont);
        drawText(width - 17 * 10, 10, scoreText, Color.yellow);
        gimg.setFont(normalFont);
    }

    private final void drawHelp() {
        if (!showHelp)
            return;

        drawCenteredText(  "           SHH SpaceGame\n"
                         + "\n"
                         + "        by Sverre H. Huseby\n"
                         + "\n\n"
                         + "Arrows            Turn and Thrust\n"
                         + "Ctrl or Space     Fire Phaser / New Ship\n"
                         + "Alt or Shift      Fire Targeting Bomb\n"
                         + "A                 Toggle Audio\n"
                         + "S                 Toggle Show Scores\n"
                         + "T                 Enter a Chat Line\n"
                         + "\n"
                         , Color.white, true);
    }

    private static final void sortOnScores(Player[] players) {
        int    len, q, w, smallest, diff;
        Player tmp;

        len = players.length;
        for (q = 0; q < len; q++) {
            smallest = q;
            for (w = q + 1; w < len; w++) {
//              diff = players[smallest].getScore() - players[w].getScore();
                diff = ((Player) players[smallest]).getRatio()
                    - ((Player) players[w]).getRatio();
                if (diff < 0)
                    smallest = w;
            }
            tmp = players[q];
            players[q] = players[smallest];
            players[smallest] = tmp;
        }
    }

    private final void updateBounds() {
        bounds.x = offsx;
        bounds.y = offsy;
        bounds.width = width;
        bounds.height = height;
    }

/*-----------------------------------------------------------------------+
 |  PACKAGE LOCAL PART                                                   |
 +----------------------------------------------------------------------*/
    final synchronized void updateImage(Player player) {
        int                  q, n;
        DrawableGameObject[] objects;
        Ship                 ship;
        Point                center;

        if (img == null) {
            /* i would have put this somewhere else, but createImage
             * can't be called untill the Canvas is `realized', so I
             * put it here. */
            img = createImage(width, height);
            gimg = img.getGraphics();
            normalFont = new Font("Courier", Font.PLAIN, 12);
            smallFont = new Font("Courier", Font.PLAIN, 10);
            gimg.setFont(normalFont);
        }

        ship = player.getShip();
        /* this section needs to be protected against update, cause
         * update to the ship's position after the board is centered
         * around the ship, causes lots of flicker. */
        synchronized (ship) {
            /* center the visible area around the player's ship. */
            center = ship.getLocation();
            offsx = center.x - width / 2;
            offsy = center.y - height / 2;
            updateBounds();

            gimg.clearRect(0, 0, width, height);
            gimg.translate(-offsx, -offsy);
            gimg.setColor(Color.gray);

            /* mark the bounds of the world. this may not be visible in
             * the current viewing area. */
            gimg.drawRect(0, 0, world.getWidth(), world.getHeight());

            /* draw all objects that are visible within the viewing
             * area. */
            objects = world.getObjects(bounds);
            n = objects.length;
            for (q = 0; q < n; q++)
                ((DrawableGameObject) objects[q]).draw(gimg);

            if (userClient != null)
                userClient.annotateBoardImage(gimg);

            gimg.translate(offsx, offsy);
        }
        /* we allow some flickering in the small radar display, to
         * avoid blocking the ship object too long. */
        drawRadar(offsx, offsy);

        drawStatus(player);
        drawScores();

        drawMessages(10, 0);
        drawChatMessages(10, height / 2 + 10);
        drawHelp();

        /* I originally had repaint() here, but that caused lots of
         * flickering on Windows, possibly due to some bad
         * synchronization between the AWT updator thread and this
         * one. now this thread handles the drawing itself: */
        getGraphics().drawImage(img, 0, 0, this);
    }

    final void setUserClient(UserDefinedClient userClient) {
        this.userClient = userClient;
    }

    final int getOffsetX() {
        return offsx;
    }

    final int getOffsetY() {
        return offsy;
    }

    final Canvas getComponent() {
        return this;
    }

    final void addMessage(String msg) {
        messages.add(msg);
    }

    final void addChatMessage(String msg) {
        chatMessages.add(5000, msg);
    }

    final void setShowScores(boolean status) {
        showScores = status;
    }

    final void toggleShowScores() {
        showScores = !showScores;
    }

    final void setShowHelp(boolean status) {
        showHelp = status;
    }

    final void updateScoreText() {
        int          q, w, n;
        Player[]     players;
        Player  player;
        String       score, antiScore, ratio;
        StringBuffer sb;

        sb = new StringBuffer();

        players = world.getPlayers();
        n = players.length;
        sortOnScores(players);
        for (q = 0; q < n; q++) {
            player = (Player) players[q];

            ratio = player.getRatioString();
            sb.append(ratio);
            score = String.valueOf(player.getScore());
            for (w = score.length(); w < 3; w++)
                sb.append((char) ' ');
            sb.append(" (");
            sb.append(score);
            sb.append("/");
            antiScore = String.valueOf(player.getAntiScore());
            sb.append(antiScore);
            sb.append(") ");
            for (w = antiScore.length(); w < 3; w++)
                sb.append((char) ' ');
            sb.append(player.getName());
            sb.append((char) '\n');
        }
        scoreText = sb.toString();
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public Board(MainFuncProvider mainFunc,
                 World world, int width, int height) {
        this.mainFunc = mainFunc;
        this.world = world;
        if (width < 200)
            width = 200;
        if (height < 150)
            height = 150;
        this.width = width;
        this.height = height;
        setBackground(Color.black);
        setForeground(Color.green);
        setSize(width, height);
        bounds = new Rectangle();
        addKeyListener(new KeyRepeatNormalizer(this));
        radarWidth = width / 7;
        radarHeight = (radarWidth * world.getHeight()) / world.getWidth();
        radarX = width - radarWidth - 1;
        radarY = height - radarHeight - 1;
        pctWidth = width / 5;
        messages = new TimeoutMessages();
        chatMessages = new TimeoutMessages();
        showScores = true;
        showHelp = false;
        allowPhaser = true;
        allowBomb   = true;
    }

    /* called by gui.MainWin */
    public synchronized BoardUpdater startUpdater(Client client) {
        stopUpdater();
        this.client = client;
        updater = new BoardUpdater(this, client);
        updater.start();
        return updater;
    }

    /* called by gui.MainWin */
    public synchronized void stopUpdater() {
        if (updater == null)
            return;
        updater.setUserClient(null);
        updater.stop();
        updater = null;
    }

    /* Component *********************************************************/
    public final int getWidth() {
        return width;
    }

    public final int getHeight() {
        return height;
    }

    public final Dimension getSize() {
        return new Dimension(width, height);
    }

    public final Dimension getMinimumSize() {
        return new Dimension(width, height);
    }

    public final Dimension getPreferredSize() {
        return getMinimumSize();
    }

    public final void update(Graphics g) {
        paint(g);
    }

    public final void paint(Graphics g) {
        if (img != null)
            g.drawImage(img, 0, 0, this);
    }

    /* KeyPressedReleasedListener ***************************************/
    public void keyPressed(KeyEvent e) {
        int code;

        if (updater == null)
            return;
        code = e.getKeyCode();
        switch (code) {
          case KeyEvent.VK_LEFT:
            if (updater != null)
                updater.startTurningLeft();
            if (userClient != null)
                userClient.infoManualStartTurningLeft();
            break;
          case KeyEvent.VK_RIGHT:
            if (updater != null)
                updater.startTurningRight();
            if (userClient != null)
                userClient.infoManualStartTurningRight();
            break;
          case KeyEvent.VK_UP:
            if (updater != null)
                updater.startMovingForeward();
            if (userClient != null)
                userClient.infoManualStartMovingForeward();
            break;
          case KeyEvent.VK_DOWN:
            if (updater != null)
                updater.startMovingBack();
            if (userClient != null)
                userClient.infoManualStartMovingBack();
            break;
          case KeyEvent.VK_SPACE:
            if (updater != null && allowPhaser) {
                updater.firePhaser();
                allowPhaser = false;
                if (userClient != null)
                    userClient.infoManualFirePhaser();
            }
            break;
          case KeyEvent.VK_ALT:
          case KeyEvent.VK_CONTROL:
            if (updater != null && allowBomb) {
                updater.fireBomb();
                allowBomb = false;
                if (userClient != null)
                    userClient.infoManualFireBomb();
            }
            break;
          case KeyEvent.VK_S:
            toggleShowScores();
            break;
          case KeyEvent.VK_A:
            break;
          case KeyEvent.VK_F1:
            setShowHelp(true);
            break;
          default:
            if (userClient != null)
                userClient.infoUnhandeledKeyPressed(code);
        }
    }

    public void keyReleased(KeyEvent e) {
        int code;

        if (updater == null)
            return;
        code = e.getKeyCode();
        switch (code) {
          case KeyEvent.VK_LEFT:
            if (updater != null)
                updater.stopTurningLeft();
            if (userClient != null)
                userClient.infoManualStopTurningLeft();
            break;
          case KeyEvent.VK_RIGHT:
            if (updater != null)
                updater.stopTurningRight();
            if (userClient != null)
                userClient.infoManualStopTurningRight();
            break;
          case KeyEvent.VK_UP:
            if (updater != null)
                updater.stopMovingForeward();
            if (userClient != null)
                userClient.infoManualStopMovingForeward();
            break;
          case KeyEvent.VK_DOWN:
            if (updater != null)
                updater.stopMovingBack();
            if (userClient != null)
                userClient.infoManualStopMovingBack();
            break;
          case KeyEvent.VK_CONTROL:
          case KeyEvent.VK_SPACE:
            allowPhaser = true;
            break;
          case KeyEvent.VK_ALT:
          case KeyEvent.VK_SHIFT:
            allowBomb = true;
            break;
          case KeyEvent.VK_F1:
            setShowHelp(false);
            break;
          case KeyEvent.VK_T:
            mainFunc.invokeChatLine();
            break;
          default:
            if (userClient != null)
                userClient.infoUnhandeledKeyReleased(code);
        }
    }
}
