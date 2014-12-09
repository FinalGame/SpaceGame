
package gui;

import java.awt.*;

import no.shhsoft.awt.*;

class RemoteServerDialog {
/*-----------------------------------------------------------------------+
 |  PRIVATE PART                                                         |
 +----------------------------------------------------------------------*/
    private boolean   cancelled;
    private String    host;
    private int       port;
    private Label     labelHost = new Label("Nombre de Host");
    private TextField textHost  = new TextField(15);
    private Label     labelPort = new Label("Puerto");
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
        gbl.setConstraints(labelHost, gbc);
        panel.add(labelHost);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(textHost, gbc);
        textHost.setText(host);
        panel.add(textHost);

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

    String getHost() {
        return host;
    }

    int getPort() {
        return port;
    }

/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    public RemoteServerDialog(Frame parent, String host, int port) {
        ModalButtonDialog dlg;
        int               code;

        this.host = host;
        this.port = port;

        dlg = new ModalButtonDialog(
                      parent, "Servidor Remoto",
                      setupPanel(),
                      ModalButtonDialog.OK | ModalButtonDialog.CANCEL,
                      ModalButtonDialog.OK);

        code = dlg.showAndGetButtonCode();
        if (code == ModalButtonDialog.OK) {
            this.host = textHost.getText();
            this.port = Integer.valueOf(textPort.getText()).intValue();
            cancelled = false;
        } else { /* CANCEL */
            cancelled = true;
        }
    }
}
