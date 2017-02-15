package ch.stageconcept.dtraff.preference.util;

import ch.stageconcept.dtraff.main.MainApp;
import ch.stageconcept.dtraff.preference.view.PrefDialogController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Helper enum class to supply PrefDialog
 * in front of PRIMARY_STAGE
 *
 * @author Olivier Durand
 */
public enum PrefEditor {

    INSTANCE;

    /**
     * Opens a dialog to edit user preferences.
     *
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean supply() {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("../preference/view/PrefDialog.fxml"));
            AnchorPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit User Preferences");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(MainApp.PRIMARY_STAGE);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the Stage object into the controller.
            PrefDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

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
