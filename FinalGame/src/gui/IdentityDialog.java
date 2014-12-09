package gui;

import java.awt.*;

import no.shhsoft.awt.*;

class IdentityDialog {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private boolean   cancelled;
    private String    name;
    private Label     labelName = new Label("Name");
    private TextField textName  = new TextField(15);

    private Panel setupPanel() {
        Panel              panel;
        GridBagLayout      gbl;
        GridBagConstraints gbc;

        panel = new Panel();

        gbl = new GridBagLayout();
        gbc = new GridBagConstraints();
        panel.setLayout(gbl);
        gbc.insets    = new Insets(3, 10, 3, 10);
        gbc.anchor    = GridBagConstraints.WEST;

        gbc.gridwidth = 1;
        gbl.setConstraints(labelName, gbc);
        panel.add(labelName);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(textName, gbc);
        textName.setText(name);
        panel.add(textName);

        return panel;
    }

/*-----------------------------------------------------------------------+
 |  PACKAGE LOCAL PART                                                   |
 +----------------------------------------------------------------------*/
    boolean isCancelled() {
        return cancelled;
    }

    String getName() {
        return name;
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public IdentityDialog(Frame parent, String name) {
        ModalButtonDialog dlg;
        int               code;

        this.name = name;

        dlg = new ModalButtonDialog(
                      parent, "Identity",
                      setupPanel(),
                      ModalButtonDialog.OK | ModalButtonDialog.CANCEL,
                      ModalButtonDialog.OK);

        code = dlg.showAndGetButtonCode();
        if (code == ModalButtonDialog.OK) {
            this.name = textName.getText();
            cancelled = false;
        } else { /* CANCEL */
            cancelled = true;
        }
    }
}
