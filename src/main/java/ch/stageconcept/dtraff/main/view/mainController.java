package ch.stageconcept.dtraff.main.view;

import ch.stageconcept.dtraff.servcon.model.Connection;
import javafx.fxml.FXML;

/**
 * Main application fxml layout controller.
 *
 * @author Olivier Durand
 */
public class mainController {

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
    }

    /**
     * Called when the user clicks the new button. Opens a dialog to edit
     * details for a new connection.
     */
    @FXML
    private void handleNewConnection() {
        Connection tempConnection = new Connection();
        boolean okClicked = mainApp.showPersonEditDialog(tempPerson);
        if (okClicked) {
            mainApp.getPersonData().add(tempPerson);
        }
    }
}
