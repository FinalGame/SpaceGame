package no.shhsoft.awt;

import java.awt.*;
import java.awt.event.*;

/**
 * A simple helper class for displaying modal dialog boxes with buttons
 * in them.
 * <P>
 *
 * The following buttons are available:
 * <BLOCKQUOTE>
 * "Ok", "Done", "Close", "Yes", "No", "Accept", "Decline",
 * "Apply", "Stop", "Abort", "Cancel" and "Help".
 * </BLOCKQUOTE>
 * Each button text has a matching constant:
 * <BLOCKQUOTE>
 * <CODE>OK</CODE>, <CODE>DONE</CODE>, <CODE>CLOSE</CODE>,
 * <CODE>YES</CODE>, <CODE>NO</CODE>, <CODE>ACCEPT</CODE>,
 * <CODE>DECLINE</CODE>, <CODE>APPLY</CODE>, <CODE>STOP</CODE>,
 * <CODE>ABORT</CODE>, <CODE>CANCEL</CODE> and <CODE>HELP</CODE>.
 * </BLOCKQUOTE>
 * The constants are used when telling what buttons
 * to display. Simply combine the button constants using XOR (or use
 * + if bits scare you off). The constants are also used as return values
 * to indicate what button was pressed to get out of the dialog.
 * <P>
 *
 * <B>NOTE:</B> Dialogs are centered on the closest parent
 * <CODE>Frame</CODE> component, even if a non-<CODE>Frame</CODE>
 * component is given as the parent.
 */
public class ModalButtonDialog
extends Dialog {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private static final int defaultHorizontalInset = 10;
    private static final int defaultVerticalInset = 10;

    private int buttonCode;
    private static String[] buttonText = {
        /* NOTE: order must match constants given below! */
        "Ok", "Done", "Close", "Yes", "No", "Accept", "Decline",
        "Apply", "Stop", "Abort", "Cancel", "Help",
        "Shoot yourself in the foot"
    };
    private Component parent;

    /* a horizontal line component. */
    private class HorizontalLine
extends Canvas {
        public void paint(Graphics g) {
            Dimension size;

            size = super.getSize();
            g.setColor(super.getBackground().darker().darker().darker());
            g.drawLine(0, 1, size.width, 1);
            g.setColor(super.getBackground().brighter().brighter().brighter());
            g.drawLine(0, 2, size.width, 2);
        }

        public Dimension getMinimumSize() {
            return new Dimension(0, 4);
        }

        public Dimension getPreferredSize() {
            return getMinimumSize();
        }
    }

    /* handles closing the window without using any of the buttons. */
    private class WindowCloseSensor
extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            dispose();
        }
    }

    /* converts a button press (action) to a return value. */
    private class ButtonMapper
    implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int q, code;

            for (q = 0, code = 1; q < buttonText.length; q++, code <<= 1)
                if (buttonText[q].equals(e.getActionCommand())) {
                    buttonCode = code;
                    break;
                }
            dispose();
        }
    }

    /* traverse component hierarchy upwards to find the Frame. */
    private static Frame findFrame(Component comp) {
        if (comp == null)
            return new Frame();
        for (;;) {
            if (comp instanceof Frame)
                break;
            if ((comp = comp.getParent()) == null)
                break;
        }
        return (Frame) comp;
    }

    private static void setLocationCenteredOnParent(Component parent,
                                                    Dialog dialog) {
        int       x, y;
        Point     parentPos;
        Dimension parentSize, dialogSize;

        /* we want it centered in the Frame window, not on the (possibly)
         * tiny component from which the dialog was triggered. */
        if (parent != null) {
            parent = findFrame(parent);
            parentPos = parent.getLocationOnScreen();
            parentSize = parent.getSize();
        } else {
            /* no parent... center on screen. */
            parentPos = new Point(0, 0);
            parentSize = Toolkit.getDefaultToolkit().getScreenSize();
        }
        dialogSize = dialog.getSize();
        x = parentPos.x + (parentSize.width - dialogSize.width) / 2;
        y = parentPos.y + (parentSize.height - dialogSize.height) / 2;
        if (x < 0)
            x = 0;
        if (y < 0)
            y = 0;
        dialog.setLocation(x, y);
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    /* NOTE: order must match strings given above! */
    public static final int OK            =  1 <<  0;
    public static final int DONE          =  1 <<  1;
    public static final int CLOSE         =  1 <<  2;
    public static final int YES           =  1 <<  3;
    public static final int NO            =  1 <<  4;
    public static final int ACCEPT        =  1 <<  5;
    public static final int DECLINE       =  1 <<  6;
    public static final int APPLY         =  1 <<  7;
    public static final int STOP          =  1 <<  8;
    public static final int ABORT         =  1 <<  9;
    public static final int CANCEL        =  1 << 10;
    public static final int HELP          =  1 << 11;
    public static final int SHOOT_IN_FOOT =  1 << 12;

    public ModalButtonDialog(Component parent, String title,
                             int buttons, int defaultButton) {
        super(findFrame(parent), title, true);

        int          code, num;
        Panel        panelLineButtons;
        Panel        panelButtons;
        Panel        panelButtonsGrid;
        Button       button;
        ButtonMapper mapper;

        this.parent = parent;

        addWindowListener(new WindowCloseSensor());
        mapper = new ButtonMapper();

        buttonCode = defaultButton;

        /* buttons */
        /* buttons are put in a grid, to make them equally wide. the grid
         * is put in another panel, to prevent it from eating the entire
         * width of the button area. */
        panelButtonsGrid = new Panel(new GridLayout(1, 0, 10, 0));
        code = 1;
        num = 0;
        while (buttons != 0) {
            if ((buttons & 1) != 0) {
                button = new Button(buttonText[num]);
                button.addActionListener(mapper);
                panelButtonsGrid.add(button);
            }
            buttons >>= 1;
            code <<= 1;
            num += 1;
        }
        panelButtons = new Panel(new FlowLayout());
        panelButtons.add(panelButtonsGrid);
        panelLineButtons = new Panel(new BorderLayout());
        /* a separator line */
        panelLineButtons.add(new HorizontalLine(), BorderLayout.NORTH);
        panelLineButtons.add(panelButtons, BorderLayout.SOUTH);

        add(panelLineButtons, BorderLayout.SOUTH);
    }

    public ModalButtonDialog(Component parent, String title,
                             Component component,
                             int buttons, int defaultButton) {
        this(parent, title, buttons, defaultButton);
        setCenterComponent(component);
    }

    public ModalButtonDialog(Component parent,
                             Component component,
                             int buttons, int defaultButton) {
        this(parent, "", component, buttons, defaultButton);
    }

    public void setCenterComponent(Component component) {
        Panel panelMain;

        /* the message of the dialog box */
        panelMain = new Panel(new FlowLayout(FlowLayout.CENTER,
                                             defaultHorizontalInset,
                                             defaultVerticalInset));
        panelMain.add(component);
        add(panelMain, BorderLayout.CENTER);

        /* activate the layout managers, and display the dialog centered
         * on the parent frame. */
        pack();
        setLocationCenteredOnParent(parent, this);
    }

    public int getButtonCode() {
        return buttonCode;
    }

    public int showAndGetButtonCode() {
        setVisible(true); /* will block */
        return getButtonCode();
    }
}
