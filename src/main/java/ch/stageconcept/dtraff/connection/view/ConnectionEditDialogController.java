package ch.stageconcept.dtraff.connection.view;

import ch.stageconcept.dtraff.connection.model.DbConnect;
import ch.stageconcept.dtraff.connection.util.DbDescriptor;
import ch.stageconcept.dtraff.connection.util.DbType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Dialog to edit details of a dbConnect.
 *
 * @author Olivier Durand
 */
public class ConnectionEditDialogController {

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
    private DbConnect dbConnect;
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

        // Test Connection button, Enter key pressed event handler
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
     * Sets the dbConnect to be edited in the dialog.
     *
     * @param dbConnect
     */
    public void setDbConnect(DbConnect dbConnect) {
        this.dbConnect = dbConnect;

        nameField.setText(dbConnect.getName());
        denominationField.getSelectionModel().select(DbType.INSTANCE.getDbDescriptorMap().get(dbConnect.getKey()));
        hostField.setText(dbConnect.getHost());
        portField.setText(Integer.toString(dbConnect.getPort()));
        userField.setText(dbConnect.getUser());
        passwordField.setText(dbConnect.getPassword());
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
     * Called when the user clicks test connection.
     */
    @FXML
    private void handleTestConnection() {
        if (setDbConnectValues()) {
            testConnectionLabel.setText("Try to connect..");

            Task task = new Task<Void>() {
                @Override public Void call() {
                    //Pause for 1 seconds to let progress indicator enough time to appear
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    dbConnect.doConnect();
                    dbConnect.undoConnect();
                    return null;
                }
            };

            // Bind Test Connection button enable/disable and progress indicator visibility with above task running property
            testConnectionButton.disableProperty().bind(task.runningProperty());
            testConnectionProgressIndicator.visibleProperty().bind(task.runningProperty());

            new Thread(task).start();
        }
    }

    /**
     * Utility method to set attributes of dbConnect object
     * from edit dialog form if fields contain valid values.
     */
    private boolean setDbConnectValues() {
        if (isInputValid()) {

            DbDescriptor dbDescriptor = denominationField.getSelectionModel().getSelectedItem();

            dbConnect.setKey(dbDescriptor.getKey());
            dbConnect.setName(nameField.getText());
            dbConnect.setDenomination(dbDescriptor.getDenomination());
            dbConnect.setHost(hostField.getText());
            dbConnect.setPort(Integer.parseInt(portField.getText()));
            dbConnect.setUser(userField.getText());
            dbConnect.setPassword(passwordField.getText());
            dbConnect.setDriver(dbDescriptor.getDriver());

            dbConnect.setBaseUrl(dbDescriptor.getBaseUrl(dbConnect));

            return true;
        }
        return false;
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        if (setDbConnectValues()) {
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
            errorMessage += "No valid name!\n";
        }

        if (denominationField.getSelectionModel().getSelectedItem() == null ||
                denominationField.getSelectionModel().getSelectedItem().toString().length() == 0) {
            errorMessage += "No valid database!\n";
        }

        if (hostField.getText() == null || hostField.getText().length() == 0) {
            errorMessage += "No valid host!\n";
        } else {
            //TODO Check that hostField contain valid URL or IP address
        }

        if (portField.getText() == null || portField.getText().length() == 0) {
            errorMessage += "No valid port!\n";
        } else {
            // try to parse the port into an int.
            try {
                Integer.parseInt(portField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "No valid port (must be an integer)!\n";
            }
        }

        if (userField.getText() == null || userField.getText().length() == 0) {
            errorMessage += "No valid user!\n";
        }

        if (passwordField.getText() == null || passwordField.getText().length() == 0) {
            errorMessage += "No valid password!\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }
}
