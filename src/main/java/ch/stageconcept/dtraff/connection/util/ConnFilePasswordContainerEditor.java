package ch.stageconcept.dtraff.connection.util;

import ch.stageconcept.dtraff.connection.model.ConnFile;
import ch.stageconcept.dtraff.connection.view.ConnFileEditDialogController;
import ch.stageconcept.dtraff.connection.view.ConnFilePasswordContainerDialogController;
import ch.stageconcept.dtraff.main.MainApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Helper enum class to supply ConnFilePasswordContainerDialog
 * in front of PRIMARY_STAGE.
 *
 * @author Olivier Durand
 */
public enum ConnFilePasswordContainerEditor {

    INSTANCE;

    // Fxml resource
    private static final String FXML_RESOURCE_PATH = "../connection/view/ConnFilePasswordContainerDialog.fxml";

    /**
     * Opens a dialog to edit password for the specified Connection File (ConnFile).
     * If the user clicks OK, the changes are saved into the provided ConnFile object and true
     * is returned.
     *
     * @param connFile the object to be edited
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean supply(ConnFile connFile, String title) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(MainApp.TEXT_BUNDLE);
            loader.setLocation(MainApp.class.getResource(FXML_RESOURCE_PATH));
            AnchorPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            if (title != null) dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(MainApp.PRIMARY_STAGE);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the Stage and ConnFile objects into the controller.
            ConnFilePasswordContainerDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setConnFile(connFile);

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
