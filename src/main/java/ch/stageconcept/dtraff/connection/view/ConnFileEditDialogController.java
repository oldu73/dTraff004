package ch.stageconcept.dtraff.connection.view;

import ch.stageconcept.dtraff.connection.model.ConnFile;
import ch.stageconcept.dtraff.main.view.RootLayoutController;
import ch.stageconcept.dtraff.util.AlertDialog;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Dialog controller to edit details
 * of a ConnFile object instance.
 *
 * @author Olivier Durand
 */
public class ConnFileEditDialogController {

    private static final String FILE_EXT = ".xml";

    private static final String ALERR_INPUT_INVALID_TITLE = "Invalid Fields";
    private static final String ALERR_INPUT_INVALID_HEADER = "Please correct invalid fields";
    private static final String ALERR_INPUT_INVALID_CONTENT_FOLDER = "No valid folder!\n";
    private static final String ALERR_INPUT_INVALID_CONTENT_FILE_1 = "No valid file!\n";
    private static final String ALERR_INPUT_INVALID_CONTENT_FILE_2 = RootLayoutController.ALINF_FILE_ALREADY_PRESENT_CONTENT;
    private static final String ALERR_INPUT_INVALID_CONTENT_PASSWORD = "No valid password!\n";

    @FXML
    private TextField folderField;

    @FXML
    private ProgressIndicator progressIndicator;

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
     *
     * SRC: http://stackoverflow.com/questions/37393642/creating-a-javafx-dialog-inside-a-javafx-task
     * SRC: http://stackoverflow.com/questions/13838089/file-chooser-dialog-not-closing
     * SRC: http://fabrice-bouye.developpez.com/tutoriels/javafx/gui-service-tache-de-fond-thread-javafx/
     * SRC: http://stackoverflow.com/questions/16978557/wait-until-platform-runlater-is-executed-using-latch
     * SRC: http://tutorials.jenkov.com/java-util-concurrent/countdownlatch.html
     *
     */
    @FXML
    private void handleBrowse() {

        // Could take some times to get back from system to application
        // with path information. During this time, nothing happen and the UI is frozen...
        // ==>> CountDownLatch, Service, Task, runLater and Thread

        final CountDownLatch latch = new CountDownLatch(1);

        // SRC: http://stackoverflow.com/questions/13395114/how-to-initialize-liststring-object-in-java
        // SRC: http://stackoverflow.com/questions/1005073/initialization-of-an-arraylist-in-one-line
        List<Button> buttonList = new ArrayList<>(Arrays.asList(browseButton, okButton, cancelButton));

        // SRC: https://www.mkyong.com/java8/java-8-foreach-examples/
        buttonList.forEach(button -> button.setDisable(true));

        final Service<Void> browseService = new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {

                        Platform.runLater(() -> {
                            progressIndicator.setVisible(true);

                            DirectoryChooser directoryChooser = new DirectoryChooser();

                            try {
                                folderField.setText(directoryChooser.showDialog(dialogStage).getAbsolutePath());
                            } catch(NullPointerException e)
                            {
                                folderField.setText(null);
                            }

                            latch.countDown();
                        });

                        return null;
                    }
                };
            }
        };

        browseService.start();

        // asynchronous thread waiting for the process (browseService) to finish
        new Thread(() -> {

            // debug mode
            //System.out.println("Await");

            try {
                latch.await();
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }

            // queuing the done notification into the javafx thread

            Platform.runLater(() -> {

                // debug mode
                //System.out.println("Done");

                buttonList.forEach(button -> button.setDisable(false));
                progressIndicator.setVisible(false);
            });

        }).start();

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
        String folder = folderField.getText();
        String file = fileField.getText();
        String password = passwordField.getText();
        String repeatPassword = repeatPasswordField.getText();
        String errorMessage = "";

        if (folder == null || folder.length() == 0) errorMessage += ALERR_INPUT_INVALID_CONTENT_FOLDER;

        if (file == null || file.length() == 0) errorMessage += ALERR_INPUT_INVALID_CONTENT_FILE_1;
        else {
            ConnFile existingConnFile = connFile.getRootLayoutController().getConnFile(file);
            if (existingConnFile != null) {
                errorMessage += "\n" + ALERR_INPUT_INVALID_CONTENT_FILE_2 + existingConnFile.getFileName() + "\n\n";
            }
        }

        if (passwordCheckBox.isSelected() && (password == null ||
                password.length() == 0 ||
                !password.equals(repeatPassword))) errorMessage += ALERR_INPUT_INVALID_CONTENT_PASSWORD;

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message.
            AlertDialog.provide(dialogStage, Alert.AlertType.ERROR, ALERR_INPUT_INVALID_TITLE, ALERR_INPUT_INVALID_HEADER, errorMessage, true);
            return false;
        }
    }
}
