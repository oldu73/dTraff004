package ch.stageconcept.dtraff.connection.view;

import ch.stageconcept.dtraff.connection.model.Connection;
import ch.stageconcept.dtraff.connection.model.File;
import ch.stageconcept.dtraff.connection.util.DbType;
import ch.stageconcept.dtraff.main.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sun.applet.Main;

/**
 * Dialog to edit details of a File.
 *
 * @author Olivier Durand
 */
public class FileEditDialogController {

    @FXML
    private TextField nameField;

    @FXML
    private CheckBox passwordCheckBox;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField repeatPasswordField;

    @FXML
    private Button browseButton;

    @FXML
    private Button okButton;

    @FXML
    private Button cancelButton;

    private Stage dialogStage;
    private File file;
    private boolean okClicked = false;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {

    }

    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Sets the File to be edited in the dialog.
     *
     * @param file
     */
    public void setFile(File file) {
        this.file = file;

        /*
        nameField.setText(connection.getName());
        denominationField.getSelectionModel().select(DbType.INSTANCE.getDbDescriptorMap().get(connection.getKey()));
        hostField.setText(connection.getHost());
        portField.setText(Integer.toString(connection.getPort()));
        userField.setText(connection.getUser());
        passwordField.setText(connection.getPassword());
        */
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
        //if (setConnectionValues()) {
            okClicked = true;
            dialogStage.close();
        //}
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Called when the user clicks browse.
     */
    @FXML
    private void handleBrowse() {
        FileChooser fileChooser = new FileChooser();

        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        java.io.File file = fileChooser.showOpenDialog(dialogStage);
    }
}
