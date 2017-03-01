package ch.stageconcept.dtraff.connection.view;

import ch.stageconcept.dtraff.connection.model.ConnFile;
import ch.stageconcept.dtraff.main.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Dialog controller for edit password container
 * of a ConnFile object instance.
 *
 * @author Olivier Durand
 */
public class ConnFilePasswordContainerDialogController {

    // Fxml resource
    private static final String FXML_PASSWORD_DIALOG_RESOURCE_PATH = "../connection/view/ConnFilePasswordDialog.fxml";

    @FXML
    private AnchorPane passwordDialogAnchorPane;

    @FXML
    private Button okButton;

    @FXML
    private Button cancelButton;

    private Stage dialogStage;
    private ConnFile connFile;
    private boolean okClicked = false;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setConnFile(ConnFile connFile) {
        this.connFile = connFile;
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(MainApp.TEXT_BUNDLE);
            loader.setLocation(MainApp.class.getResource(FXML_PASSWORD_DIALOG_RESOURCE_PATH));
            AnchorPane anchorPane = loader.load();

            ConnFilePasswordDialogController connFilePasswordDialogController = loader.getController();

            okButton.disableProperty().bind(connFilePasswordDialogController.passwordOkProperty().not());

            passwordDialogAnchorPane.getChildren().add(anchorPane);

            /*

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle(MainApp.TEXT_BUNDLE.getString("connFilePasswordContainerDialog.title"));
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

            */

        } catch (IOException e) {
            e.printStackTrace();
            //return false;
        }
    }

    /**
     * Returns true if the user clicked OK, false otherwise.
     *
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {

        okClicked = true;
        dialogStage.close();

    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

}
