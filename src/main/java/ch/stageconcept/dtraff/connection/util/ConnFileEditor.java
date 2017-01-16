package ch.stageconcept.dtraff.connection.util;

import ch.stageconcept.dtraff.connection.model.ConnFile;
import ch.stageconcept.dtraff.connection.view.ConnFileEditDialogController;
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
public enum ConnFileEditor {

    INSTANCE;

    /**
     * Opens a dialog to edit details for the specified Connection File. If the user
     * clicks OK, the changes are saved into the provided ConnFile object and true
     * is returned.
     *
     * @param file the file object to be edited
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean supply(ConnFile file) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("../connection/view/ConnFileEditDialog.fxml"));
            AnchorPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("New Connection File");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(MainApp.primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the Stage and ConnFile objects into the controller.
            ConnFileEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setConnFile(file);

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
