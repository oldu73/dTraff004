package ch.stageconcept.dtraff.connection.view;

import ch.stageconcept.dtraff.connection.model.DbConnect;
import ch.stageconcept.dtraff.connection.util.DbDescriptor;
import ch.stageconcept.dtraff.connection.util.DbType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    //private ComboBox<String> denominationField;

    private ObservableList<DbDescriptor> denominationFieldData;
    //private ObservableList<String> denominationFieldData;

    @FXML
    private TextField hostField;
    @FXML
    private TextField portField;
    @FXML
    private TextField userField;
    @FXML
    private PasswordField passwordField;

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

        Set dbTypeSet = DbType.INSTANCE.getDbDescriptorHashMap().entrySet();
        Iterator dbTypeIterator = dbTypeSet.iterator();

        while(dbTypeIterator.hasNext()) {
            Map.Entry mentry = (Map.Entry)dbTypeIterator.next();
            DbDescriptor dbDescriptor = (DbDescriptor)mentry.getValue();
            denominationFieldData.add(dbDescriptor);
            //denominationFieldData.add(dbDescriptor.getDenomination());
        }

        //TODO Sort item alphabetically
        denominationField.setItems(denominationFieldData);
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

        //TODO add listener on denomination field (or binding?) to synchronize port number

        nameField.setText(dbConnect.getName());
        //TODO denominationField.getSelectionModel().select(dbConnect.getDenomination());
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
        if (isInputValid()) {
            System.out.println("test connection..");
        }
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            //TODO dbConnect.setDriver
            dbConnect.setHost(hostField.getText());
            dbConnect.setPort(Integer.parseInt(portField.getText()));
            dbConnect.setUser(userField.getText());
            dbConnect.setPassword(passwordField.getText());

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
