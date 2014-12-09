package gui;

import java.awt.*;
import java.net.*;

import util.*;
import no.shhsoft.awt.*;

class AboutDialog {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private String aboutText =
              " SHH SpaceGame 1.5.0 [2003-03-10] - a Multiplayer Game\n"
            + "\n"
            + " by Sverre H. Huseby, Norway\n"
            + " <shh@thathost.com>\n"
            + "\n"
            + " This program is free:\n"
            + "      Copy it and use it as you wish.";

    private Panel setupPanel() {
        Panel    panel;
        Panel    imagePanel;
        TextArea text;

        panel = new Panel(new BorderLayout());

        imagePanel = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        imagePanel.add(new ImageCanvas("/img/shapes.gif", 160, 100));
        panel.add(imagePanel, BorderLayout.WEST);

        text = new TextArea(aboutText, 7, 65, TextArea.SCROLLBARS_NONE);
        text.setEditable(false);
        panel.add(text, BorderLayout.CENTER);

        return panel;
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public AboutDialog(Frame parent) {
        ModalButtonDialog dlg;

        dlg = new ModalButtonDialog(parent, "About", setupPanel(),
                                    ModalButtonDialog.CLOSE,
                                    ModalButtonDialog.CLOSE);
        dlg.show();
    }
}
