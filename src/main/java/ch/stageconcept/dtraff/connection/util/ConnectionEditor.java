package ch.stageconcept.dtraff.connection.util;

import ch.stageconcept.dtraff.connection.model.Connection;
import ch.stageconcept.dtraff.connection.view.ConnectionEditDialogController;
import ch.stageconcept.dtraff.main.MainApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Helper enum class to supply ConnectionEditDialog
 * in front of primaryStage
 *
 * @author Olivier Durand
 */
public enum ConnectionEditor {

    INSTANCE;

    /**
     * Opens a dialog to edit details for the specified Connection. If the user
     * clicks OK, the changes are saved into the provided Connection object and true
     * is returned.
     *
     * @param connection the Connection object to be edited
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean supply(Connection connection) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("../connection/view/ConnectionEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Connection");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(MainApp.primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the dbConnect into the controller.
            ConnectionEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setConnection(connection);

            // Disable resize
            dialogStage.setResizable(false);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
