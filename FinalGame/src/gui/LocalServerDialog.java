package gui;

import java.awt.*;

import no.shhsoft.awt.*;

class LocalServerDialog {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private boolean   cancelled;
    private int       port;
    private Label     labelPort = new Label("Port");
    private TextField textPort  = new TextField(5);

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
        gbl.setConstraints(labelPort, gbc);
        panel.add(labelPort);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(textPort, gbc);
        textPort.setText(Integer.toString(port));
        panel.add(textPort);

        return panel;
    }

/*-----------------------------------------------------------------------+
 |  PACKAGE LOCAL PART                                                   |
 +----------------------------------------------------------------------*/
    boolean isCancelled() {
        return cancelled;
    }

    int getPort() {
        return port;
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public LocalServerDialog(Frame parent, int port) {
        ModalButtonDialog dlg;
        int               code;

        this.port = port;

        dlg = new ModalButtonDialog(
                      parent, "Local Game Server",
                      setupPanel(),
                      ModalButtonDialog.OK | ModalButtonDialog.CANCEL,
                      ModalButtonDialog.OK);

        code = dlg.showAndGetButtonCode();
        if (code == ModalButtonDialog.OK) {
            this.port = Integer.valueOf(textPort.getText()).intValue();
            cancelled = false;
        } else { /* CANCEL */
            cancelled = true;
        }
    }
}
