
package gui;

import java.awt.*;

import no.shhsoft.awt.*;

class ReadmeDialog {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private String   readme1 = ""
        + "SpaceGame - A Simple, Networked Multiplayer Game\n";
    private String   readme2 = ""
        + "Starting to Play\n";
    private String readme = readme1 + readme2;

    private Panel setupPanel() {
        Panel    panel;
        TextArea text;

        panel = new Panel(new BorderLayout());

        text = new TextArea(readme, 30, 80, TextArea.SCROLLBARS_VERTICAL_ONLY);
        text.setFont(new Font("Courier", Font.PLAIN, 12));
        text.setEditable(false);
        panel.add(text);

        return panel;
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public ReadmeDialog(Frame parent) {
        ModalButtonDialog dlg;

        dlg = new ModalButtonDialog(parent, "README.txt", setupPanel(),
                                    ModalButtonDialog.CLOSE,
                                    ModalButtonDialog.CLOSE);
        dlg.show();
    }
}
