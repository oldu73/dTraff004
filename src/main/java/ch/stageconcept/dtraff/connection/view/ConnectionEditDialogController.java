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

    private static final String DBTYPE1 = "MariaDB";
    private static final String DBTYPE2 = "MySQL";
    private static final String DBTYPE3 = "PostgreSQL";

    @FXML
    private ComboBox<String> databaseField;

    private ObservableList<String> databaseFieldData;

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
        databaseFieldData = FXCollections.observableArrayList();

        Set dbTypeSet = DbType.INSTANCE.getDbDescriptorHashMap().entrySet();
        Iterator dbTypeIterator = dbTypeSet.iterator();

        while(dbTypeIterator.hasNext()) {
            Map.Entry mentry = (Map.Entry)dbTypeIterator.next();
            DbDescriptor dbDescriptor = (DbDescriptor)mentry.getValue();
            databaseFieldData.add(dbDescriptor.getDenomination());
        }

        databaseField.setItems(databaseFieldData);
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

        //TODO Solution to pass dbConnect parameter when it's a case of a new one: - Construct an object with MySQL default values
        //name: default
        //database: MySql
        //port: xxx
        //user: root
        //password: root
        //Autoselect field in form on focus to ease default values replacement
        if (dbConnect.getHost() != null) {  // means that dbConnect object contain only null values,
                                            // not a really clean solution but for now it do the job
                                            // and I have no other idea...

            //TODO Set databaseField
            hostField.setText(dbConnect.getHost());
            portField.setText(Integer.toString(dbConnect.getPort()));
            userField.setText(dbConnect.getUser());
            passwordField.setText(dbConnect.getPassword());
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
     * Validates the user input in the text fields.
     *
     * @return true if the input is valid
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (databaseField.getSelectionModel().getSelectedItem() == null || databaseField.getSelectionModel().getSelectedItem().toString().length() == 0) {
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
