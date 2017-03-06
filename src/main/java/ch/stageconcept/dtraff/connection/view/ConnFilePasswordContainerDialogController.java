package ch.stageconcept.dtraff.connection.view;

import ch.stageconcept.dtraff.connection.model.ConnFile;
import ch.stageconcept.dtraff.main.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
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
    private ConnFilePasswordDialogController controller;

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
        //
    }

    /**
     * Initialization called from outside.
     */
    public void postInitialize() {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(MainApp.TEXT_BUNDLE);
            loader.setLocation(MainApp.class.getResource(FXML_PASSWORD_DIALOG_RESOURCE_PATH));
            AnchorPane anchorPane = loader.load();

            controller = loader.getController();
            controller.setPasswordToCheck(connFile.getParent().getPasswordToCheck());
            controller.postInitialize();

            okButton.disableProperty().bind(controller.passwordOkProperty().not());

            passwordDialogAnchorPane.getChildren().add(anchorPane);

        } catch (IOException e) {
            e.printStackTrace();
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

        connFile.setPasswordProtected(true);
        connFile.setPassword(controller.getPassword());

        connFile.getSubUnits().forEach(conn -> conn.setEncryptedPassword(conn.getPassword()));

        if (connFile.isEmptyClear()) connFile.setEmptyDecrypted();
        else {
            connFile.setDecrypted();
            connFile.saveConnDataToFile();
        }

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
