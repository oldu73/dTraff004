package ch.stageconcept.dtraff.connection.util;

import ch.stageconcept.dtraff.connection.model.Conn;
import ch.stageconcept.dtraff.connection.view.ConnEditDialogController;
import ch.stageconcept.dtraff.main.MainApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Helper enum class to supply ConnectionEditDialog
 * in front of PRIMARY_STAGE
 *
 * @author Olivier Durand
 */
public enum ConnEditor {

    INSTANCE;

    // Fxml resource
    private static final String CONN_EDIT_DIALOG_FXML = "../connection/view/ConnEditDialog.fxml";

    /**
     * Opens a dialog to edit details for the specified Conn. If the user
     * clicks OK, the changes are saved into the provided Conn object and true
     * is returned.
     *
     * @param conn the Conn object to be edited
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean supply(Conn conn, String title) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(CONN_EDIT_DIALOG_FXML));
            AnchorPane page = loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(MainApp.PRIMARY_STAGE);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the Stage and Conn objects into the controller.
            ConnEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setConn(conn);

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
