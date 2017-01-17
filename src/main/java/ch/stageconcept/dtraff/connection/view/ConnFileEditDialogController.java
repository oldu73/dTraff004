package ch.stageconcept.dtraff.connection.view;

import ch.stageconcept.dtraff.connection.model.ConnFile;
import ch.stageconcept.dtraff.connection.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * Dialog controller to edit details
 * of a ConnFile object instance.
 *
 * @author Olivier Durand
 */
public class ConnFileEditDialogController {

    private static final String FILE_EXT = ".xml";

    @FXML
    private TextField folderField;

    @FXML
    private TextField fileField;

    @FXML
    private CheckBox passwordCheckBox;

    @FXML
    private Label passwordLabel;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label repeatPasswordLabel;

    @FXML
    private PasswordField repeatPasswordField;

    @FXML
    private Button browseButton;

    @FXML
    private Button okButton;

    @FXML
    private Button cancelButton;

    private Stage dialogStage;
    private ConnFile connFile;
    private boolean okClicked = false;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        passwordLabel.disableProperty().bind(passwordCheckBox.selectedProperty().not());
        passwordField.disableProperty().bind(passwordCheckBox.selectedProperty().not());
        repeatPasswordLabel.disableProperty().bind(passwordCheckBox.selectedProperty().not());
        repeatPasswordField.disableProperty().bind(passwordCheckBox.selectedProperty().not());
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
     * Sets the ConnFile to be edited in the dialog.
     *
     * @param Connfile
     */
    public void setConnFile(ConnFile Connfile) {
        this.connFile = Connfile;
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
        if (setConnFileValues()) {
            okClicked = true;
            dialogStage.close();
        }
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
        //TODO Put in "Thread"
        // Could take some times to get back from system to application
        // with path information. During this time, nothing happen and the UI is frozen.
        // So, put in task or something alike to let user be informed that process is on the way.
        // (UI disabled and mouse waiting symbol animation.)

        DirectoryChooser directoryChooser = new DirectoryChooser();

        try {
            folderField.setText(directoryChooser.showDialog(dialogStage).getAbsolutePath());
        } catch(NullPointerException e)
        {
            folderField.setText(null);
        }

    }

    /**
     * Utility method to set attributes of ConnFile object
     * from edit dialog form if fields contain valid values.
     */
    private boolean setConnFileValues() {

        //TODO Check if file already exist

        if (isInputValid()) {
            connFile.setFileName(folderField.getText() + "\\" + fileField.getText() + FILE_EXT);
            connFile.setName(fileField.getText());

            boolean isSafe = passwordCheckBox.isSelected();
            connFile.setPasswordProtected(isSafe);
            if (isSafe) {
                connFile.setPassword(passwordField.getText());
            } else {
                connFile.setPassword(null);
            }

            //System.out.println(Connfile.isPasswordProtected());
            //System.out.println(Connfile.getPassword());
            //System.out.println();

            return true;
        }
        return false;
    }

    /**
     * Validates the user input in the fields.
     *
     * @return true if the input is valid
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (folderField.getText() == null || folderField.getText().length() == 0) {
            errorMessage += "No valid folder!\n";
        }

        if (fileField.getText() == null || fileField.getText().length() == 0) {
            errorMessage += "No valid file!\n";
        }

        if (passwordCheckBox.isSelected() && (passwordField.getText() == null
                || passwordField.getText().length() == 0
                || !passwordField.getText().equals(repeatPasswordField.getText()))) {

            errorMessage += "No valid password!\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message.
            ErrorAlert.INSTANCE.show(dialogStage, "Invalid Fields", "Please correct invalid fields", errorMessage);
            return false;
        }
    }
}
