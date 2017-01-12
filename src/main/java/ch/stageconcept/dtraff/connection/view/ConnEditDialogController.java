package ch.stageconcept.dtraff.connection.view;

import ch.stageconcept.dtraff.connection.model.Conn;
import ch.stageconcept.dtraff.connection.util.Crypto;
import ch.stageconcept.dtraff.connection.util.DbDescriptor;
import ch.stageconcept.dtraff.connection.util.DbType;
import ch.stageconcept.dtraff.connection.util.ErrorAlert;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Dialog to edit details of a Conn.
 *
 * @author Olivier Durand
 */
public class ConnEditDialogController {

    private static final String CONNECTION_TRY = "Try to connect..";
    private static final String CONNECTION_SUCCESSFUL = "Connection successfully established..";
    private static final String CONNECTION_FAIL = "Unable to establish connection!";

    private static final String ALERT_INVALID_NAME = "No valid name!\n";
    private static final String ALERT_INVALID_DATABASE = "No valid database!\n";
    private static final String ALERT_INVALID_HOST = "No valid host!\n";
    private static final String ALERT_INVALID_PORT = "No valid port!\n";
    private static final String ALERT_INVALID_PORT_NUMBER = "No valid port (must be an integer)!\n";
    private static final String ALERT_INVALID_USER = "No valid user!\n";
    private static final String ALERT_INVALID_PASSWORD = "No valid password!\n";
    private static final String ALERT_INVALID_TITLE = "Invalid Fields";
    private static final String ALERT_INVALID_HEADER = "Please correct invalid fields";

    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<DbDescriptor> denominationField;

    private ObservableList<DbDescriptor> denominationFieldData;

    @FXML
    private TextField hostField;
    @FXML
    private TextField portField;
    @FXML
    private TextField userField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private Button testConnectionButton;

    @FXML
    private ProgressIndicator testConnectionProgressIndicator;

    @FXML
    private Label testConnectionLabel;

    @FXML
    private Button okButton;

    @FXML
    private Button cancelButton;

    private Stage dialogStage;
    private Conn conn;
    private boolean okClicked = false;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        denominationFieldData = FXCollections.observableArrayList();

        Set dbTypeSet = DbType.INSTANCE.getDbDescriptorMap().entrySet();
        Iterator dbTypeIterator = dbTypeSet.iterator();

        while(dbTypeIterator.hasNext()) {
            Map.Entry mentry = (Map.Entry)dbTypeIterator.next();
            DbDescriptor dbDescriptor = (DbDescriptor)mentry.getValue();
            denominationFieldData.add(dbDescriptor);
        }

        denominationField.setItems(denominationFieldData);

        // Synchronize denomination and port fields
        denominationField.setOnAction((event) -> {
            DbDescriptor dbDescriptor = denominationField.getSelectionModel().getSelectedItem();
            portField.setText(Integer.toString(dbDescriptor.getPort()));
        });

        // Test Conn button, Enter key pressed event handler
        testConnectionButton.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                testConnectionButton.fire();
                ev.consume();
            }
        });

        //TODO check if there is a solution to remove code redundancy on event handler, Test OK Cancel buttons = same code
        // Note on potential solution: - Manage key pressed at layout level on focused element (button)

        // Ok button, Enter key pressed event handler
        okButton.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                okButton.fire();
                ev.consume();
            }
        });

        // Cancel button, Enter key pressed event handler
        cancelButton.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                cancelButton.fire();
                ev.consume();
            }
        });

        // Initial state of testConnectionProgressIndicator is hidden
        testConnectionProgressIndicator.setVisible(false);
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
     * Sets the Conn to be edited in the dialog.
     *
     * @param conn
     */
    public void setConn(Conn conn) {
        this.conn = conn;

        nameField.setText(conn.getName());
        denominationField.getSelectionModel().select(DbType.INSTANCE.getDbDescriptorMap().get(conn.getKey()));
        hostField.setText(conn.getHost());
        portField.setText(Integer.toString(conn.getPort()));
        userField.setText(conn.getUser());

        if (conn.getParent().isPasswordProtected()) {
            Crypto crypto = new Crypto(conn.getParent().getPassword());
            passwordField.setText(crypto.getDecrypted(conn.getPassword()));
        } else {
            passwordField.setText(conn.getPassword());
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
     * Called when the user clicks Test Connection button.
     */
    @FXML
    private void handleTestConnection() {

        // SRC: http://stackoverflow.com/questions/35369591/updating-your-ui-and-forcibly-waiting-before-continuing-javafx
        PauseTransition pause = new PauseTransition(Duration.seconds(1));

        if (setConnectionValues()) {

            Task<Boolean> task = new Task<Boolean>() {
                @Override public Boolean call() throws SQLException {

                    // updateMessage("Try to connect..");

                    // Pause for 1 seconds to let progress indicator enough time to appear
                    // (Check if it really do the job (the old version below do the job for sure)
                    pause.play();

                    //old version
                    /*
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    */

                    if (conn.doConnect()) {
                        // updateMessage("Conn successfully established..");
                        conn.undoConnect();
                        return Boolean.TRUE;
                    } /* else {
                        updateMessage("Unable to establish conn!");
                    } */

                    return Boolean.FALSE;
                }
            };

            // Bind Test Conn button enable/disable and progress indicator visibility
            // with above task running property
            testConnectionButton.disableProperty().bind(task.runningProperty());
            testConnectionProgressIndicator.visibleProperty().bind(task.runningProperty());

            // If above commented updateMessage in task is used,
            // bind Test Conn label with message property
            //testConnectionLabel.textProperty().bind(task.messageProperty());

            task.setOnRunning(t -> {
                testConnectionLabel.setText(CONNECTION_TRY);
                testConnectionLabel.setTextFill(Color.BLACK);
            });

            // SRC: http://stackoverflow.com/questions/13935366/javafx-concurrent-task-setting-state
            task.setOnSucceeded(t -> {
                // This handler will be called if Task successfully executed code
                // disregarding result of login operation

                // and here we act according to result of code
                if (task.getValue()) {
                    // Successful login
                    testConnectionLabel.setText(CONNECTION_SUCCESSFUL);
                    testConnectionLabel.setTextFill(Color.GREEN);
                } /* Unnecessary because exception thrown in case of bad parameters -> task.setOnFailed
                else {
                    // Unsuccessful login
                    testConnectionLabel.setText("Unable to establish conn!");
                    testConnectionLabel.setTextFill(Color.RED);
                }
                */
            });

            task.setOnFailed(t -> {
                    // This handler will be called if exception occurred during your task execution
                    // E.g. network or db conn exceptions
                    testConnectionLabel.setText(CONNECTION_FAIL);
                    testConnectionLabel.setTextFill(Color.RED);
            });

            // Launch task..
            Thread th = new Thread(task);
            th.setDaemon(true);
            th.start();
        }
    }

    /**
     * Utility method to set attributes of Conn object
     * from edit dialog form if fields contain valid values.
     */
    private boolean setConnectionValues() {
        if (isInputValid()) {

            DbDescriptor dbDescriptor = denominationField.getSelectionModel().getSelectedItem();

            conn.setKey(dbDescriptor.getKey());
            conn.setName(nameField.getText());
            conn.setDenomination(dbDescriptor.getDenomination());
            conn.setHost(hostField.getText());
            conn.setPort(Integer.parseInt(portField.getText()));
            conn.setUser(userField.getText());

            if (conn.getParent().isPasswordProtected()) {
                conn.setPasswordEncrypted(true);
                Crypto crypto = new Crypto(conn.getParent().getPassword());
                conn.setPassword(crypto.getEncrypted(passwordField.getText()));
            } else {
                conn.setPassword(passwordField.getText());
            }

            conn.setDriver(dbDescriptor.getDriver());
            conn.setBaseUrl(dbDescriptor.getBaseUrl(conn));

            return true;
        }
        return false;
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        if (setConnectionValues()) {
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
     * Validates the user input in the fields.
     *
     * @return true if the input is valid
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (nameField.getText() == null || nameField.getText().length() == 0) {
            errorMessage += ALERT_INVALID_NAME;
        }

        if (denominationField.getSelectionModel().getSelectedItem() == null ||
                denominationField.getSelectionModel().getSelectedItem().toString().length() == 0) {
            errorMessage += ALERT_INVALID_DATABASE;
        }

        if (hostField.getText() == null || hostField.getText().length() == 0) {
            errorMessage += ALERT_INVALID_HOST;
        } else {
            //TODO Check that hostField contain valid URL or IP address
        }

        if (portField.getText() == null || portField.getText().length() == 0) {
            errorMessage += ALERT_INVALID_PORT;
        } else {
            // try to parse the port into an int.
            try {
                Integer.parseInt(portField.getText());
            } catch (NumberFormatException e) {
                errorMessage += ALERT_INVALID_PORT_NUMBER;
            }
        }

        if (userField.getText() == null || userField.getText().length() == 0) {
            errorMessage += ALERT_INVALID_USER;
        }

        if (passwordField.getText() == null || passwordField.getText().length() == 0) {
            errorMessage += ALERT_INVALID_PASSWORD;
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message.
            ErrorAlert.INSTANCE.show(dialogStage, ALERT_INVALID_TITLE, ALERT_INVALID_HEADER, errorMessage);
            return false;
        }
    }

}
