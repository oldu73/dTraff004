package ch.stageconcept.dtraff.main.view;

import ch.stageconcept.dtraff.main.MainApp;
import ch.stageconcept.dtraff.connection.model.DbConnect;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

/**
 * The controller for the root layout. The root layout provides the basic
 * application layout containing a menu bar and space where other JavaFX
 * elements can be placed.
 *
 * @author Olivier Durand
 */
public class RootLayoutController {

    // Reference to the main application
    private MainApp mainApp;

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Called when the user selects the File - New Server Connection menu. Opens a dialog to edit
     * details for a new connection.
     */
    @FXML
    private void handleNewConnection() {
        DbConnect tempDbConnect = new DbConnect();
        boolean okClicked = mainApp.showConnectionEditDialog(tempDbConnect);
        if (okClicked) {
            mainApp.getDbConnects().add(tempDbConnect);
        }
    }

    /**
     * Opens an about dialog.
     */
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Data Traffic");
        alert.setHeaderText("About");
        alert.setContentText("Author: Olivier Durand\nWebsite: http://www.stageconcept.ch");

        alert.showAndWait();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        System.exit(0);
    }

}
