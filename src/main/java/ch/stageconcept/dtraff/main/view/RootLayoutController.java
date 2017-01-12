package ch.stageconcept.dtraff.main.view;

import ch.stageconcept.dtraff.connection.model.*;
import ch.stageconcept.dtraff.connection.util.*;
import ch.stageconcept.dtraff.connection.view.ModelTree;
import ch.stageconcept.dtraff.main.MainApp;
import ch.stageconcept.dtraff.preference.model.Pref;
import ch.stageconcept.dtraff.preference.util.PrefEditor;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * The controller for the root layout. The root layout provides the basic
 * application layout containing a menu bar and space where other JavaFX
 * elements can be placed.
 *
 * @author Olivier Durand
 */
public class RootLayoutController {

    // Connections treeView root denomination
    private static final String NETWORK = "Network";

    // Connections treeView CSS resource
    private static final String CONNECTION_TREEVIEW_CSS = "/connectionTreeView.css";

    // Alerts statics texts
    private static final String ALINF_ABOUT_TITLE = "Data Traffic";
    private static final String ALINF_ABOUT_HEADER = ALINF_ABOUT_TITLE + " r0.4";
    private static final String ALINF_ABOUT_CONTENT = "Author: Olivier Durand\nWebsite: http://www.stageconcept.ch";

    private static final String ALERR_LOAD_DATA_TITLE = "Error";
    private static final String ALERR_LOAD_DATA_HEADER = "Could not load data";
    private static final String ALERR_LOAD_DATA_CONTENT = "Could not load data from file:\n";

    private static final String ALCNF_BAD_PASSWORD_TITLE = "Password Dialog";
    private static final String ALCNF_BAD_PASSWORD_HEADER = "Bad password!";
    private static final String ALCNF_BAD_PASSWORD_CONTENT = "Try again?";

    // Reference to the main application
    private MainApp mainApp;

    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private MenuItem fileNewMenuItem;

    @FXML
    private MenuItem fileOpenMenuItem;

    @FXML
    private MenuItem fileEnterPasswordMenuItem;

    @FXML
    private MenuItem fileCloseMenuItem;

    @FXML
    private MenuItem serverConnectionMenuItem;

    @FXML
    private MenuItem newServerConnectionMenuItem;

    @FXML
    private MenuItem editServerConnectionMenuItem;

    @FXML
    private MenuItem editDeleteMenuItem;

    @FXML
    private MenuItem editPreferencesMenuItem;

    private Network network;    // Network description to be used in a treeView : Network (root node) - ConnFile - Conn - Database - (...)
    private ModelTree<ConnUnit<?>> connectionTree;
    private TreeView<ConnUnit<?>> connectionTreeView;

    private ObjectProperty<ConnFileState> selectedConnFileState = new SimpleObjectProperty<>();

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {

        // JavaFX TreeView of multiple object types? (and more)
        // SRC: http://stackoverflow.com/questions/35009982/javafx-treeview-of-multiple-object-types-and-more
        // ANSWER FROM: James_D
        // GITHUB: - heterogeneous-tree-example - https://github.com/james-d/heterogeneous-tree-example
        network = createNetwork();

        connectionTree = new ModelTree<>(network,
                ConnUnit::getSubUnits,
                ConnUnit::nameProperty,
                ConnUnit::iconProperty,
                ConnUnit::menuProperty,
                unit -> PseudoClass.getPseudoClass(unit.getClass().getSimpleName().toLowerCase()));

        connectionTreeView = connectionTree.getTreeView();

        // CSS pseudo class treeView style.
        // !WARNING! In order to use file that reside in resources folder, don’t forget to add a slash before file name!
        connectionTreeView.getStylesheets().add(getClass().getResource(CONNECTION_TREEVIEW_CSS).toExternalForm());

        rootBorderPane.setLeft(connectionTreeView);

        // debug mode
        //printChildren(connectionTreeView.getRoot());

        // Double click on Connections treeView
        connectionTreeView.setOnMouseClicked((event) ->
        {
            if(event.getClickCount() == 2 &&
                    connectionTreeView.getSelectionModel().getSelectedItem().getValue() instanceof ConnFile) {

                //TreeItem<?> item = connectionTreeView.getSelectionModel().getSelectedItem();
                ConnFile connFile = (ConnFile) connectionTreeView.getSelectionModel().getSelectedItem().getValue();

                if (connFile.getState().equals(ConnFileState.ENCRYPTED) && decryptConnFile(connFile)) {
                    populateSubunit(connFile, loadConnDataFromFile(connFile));
                }
            }
        });

        // ### Tool bar menu ############################################################

        // New Server Connection Menu initial state is set to disable
        newServerConnectionMenuItem.setDisable(true);

        // Disable tool bar menu New Server Connection if the Connections treeView
        // selected item is not a clear or decrypted ConnFile object
        connectionTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.getValue() instanceof ConnFile) {
                selectedConnFileState.bind(((ConnFile) newValue.getValue()).stateProperty());

                // debug mode
                //System.out.print("1 ");

                if (((ConnFile) newValue.getValue()).getState().equals(ConnFileState.CLEAR) ||
                        ((ConnFile) newValue.getValue()).getState().equals(ConnFileState.DECRYPTED)) {
                    newServerConnectionMenuItem.setDisable(false);

                    // debug mode
                    //System.out.print("2 ");

                } else {
                    newServerConnectionMenuItem.setDisable(true);

                    // debug mode
                    //System.out.print("3 ");

                }
            } else {
                newServerConnectionMenuItem.setDisable(true);

                // debug mode
                //System.out.print("4 ");

            }
        });

        // After double click on an encrypted file to enter correct password,
        // the serverConnectionMenuItem remain disabled until treeview selection changed!
        // So, the belowing lines deserv to track ConnFile (in Connections treeView)
        // selected object state changes in order to update the newServerConnectionMenuItem disabled status.
        selectedConnFileState.addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && (oldValue.equals(ConnFileState.ENCRYPTED) && newValue.equals(ConnFileState.DECRYPTED))) {
                newServerConnectionMenuItem.setDisable(false);

                // debug mode
                //System.out.print("5 ");

            }
        });

        /*
        // Old version with BooleanBinding
        newServerConnectionMenuItem.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        connectionTreeView.getSelectionModel().getSelectedItem() == null ||
                                !(connectionTreeView.getSelectionModel().getSelectedItem().getValue() instanceof ConnFile) ||
                                (connectionTreeView.getSelectionModel().getSelectedItem().getValue() instanceof ConnFile &&
                                        (((ConnFile) connectionTreeView.getSelectionModel().getSelectedItem().getValue()).getState().equals(ConnFileState.BROKEN) ||
                                                ((ConnFile) connectionTreeView.getSelectionModel().getSelectedItem().getValue()).getState().equals(ConnFileState.ENCRYPTED))),
                connectionTreeView.getSelectionModel().selectedItemProperty()));
        */

        // Disable tool bar menu Edit Server Connection if no item or not a Conn instance selected in Connections treeView
        editServerConnectionMenuItem.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        connectionTreeView.getSelectionModel().getSelectedItem() == null ||
                                !(connectionTreeView.getSelectionModel().getSelectedItem().getValue() instanceof Conn),
                connectionTreeView.getSelectionModel().selectedItemProperty()));

        // Disable tool bar menu Server Connection if New and Edit Server Connection menus are disabled
        serverConnectionMenuItem.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        newServerConnectionMenuItem.isDisable() && editServerConnectionMenuItem.isDisable(),
                newServerConnectionMenuItem.disableProperty(),
                editServerConnectionMenuItem.disableProperty()));

        // ##############################################################################

    }

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Called when the user selects the tool bar File - New menu.
     */
    @FXML
    private void handleFileNew() {
        System.out.println("New File..");
    }

    /**
     * Called when the user selects the tool bar File - Open menu.
     */
    @FXML
    private void handleFileOpen() {
        System.out.println("Open File..");
    }

    /**
     * Called when the user selects the tool bar File - Enter Password menu.
     */
    @FXML
    private void handleFileEnterPassword() {
        System.out.println("Enter Password..");
    }

    /**
     * Called when the user selects the tool bar File - Close menu.
     */
    @FXML
    private void handleFileClose() {
        System.out.println("Close File..");
    }

    /**
     * Called when the user selects the tool bar File - Server Connection - New, menu.
     * Opens a dialog to edit details for a new connection.
     */
    @FXML
    private void handleNewConnection() {

        // The tool bar menu is disabled if none or not a ConnFile is selected in Connection treeView,
        // so the item could only be a ConnFile -> (cast)
        ConnFile connFile = (ConnFile) connectionTreeView.getSelectionModel().getSelectedItem().getValue();
        connFile.newConnection();

        //TODO Find a solution for the ConnEditDialogController Test Conn button side effect that update the edited conn:
        //- pass a temporary copy of the edited conn for testing.
    }

    /**
     * Called when the user selects the tool bar File - Server Connection - Edit, menu.
     * Opens a dialog to edit details for the existing connection.
     */
    @FXML
    private void handleEditConnection() {
        // The tool bar menu is disabled if none or not a Conn is selected in Connection treeView,
        // so the item could only be a Conn -> (cast)
        Conn conn = (Conn) connectionTreeView.getSelectionModel().getSelectedItem().getValue();
        conn.editConnection();

        //TODO Find a solution for the ConnEditDialogController Test Conn button side effect that update the edited conn:
        //- pass a temporary copy of the edited conn for testing.
    }

    /**
     * Called when the user selects the tool bar Edit - Delete menu.
     */
    @FXML
    private void handleEditDelete() {
        System.out.println("Edit Delete..");
    }

    /**
     * Called when the user selects the tool bar Edit - Preferences menu.
     */
    @FXML
    private void handleEditPreferences() {
        PrefEditor.INSTANCE.supply();
    }

    /**
     * Opens an about dialog.
     */
    @FXML
    private void handleAbout() {
        provideAlert(Alert.AlertType.INFORMATION,
                ALINF_ABOUT_TITLE,
                ALINF_ABOUT_HEADER,
                ALINF_ABOUT_CONTENT, true);
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        System.exit(0);
    }

    /**
     * Create network description in a tree data structure,
     * to be used in a treeView : Network (root node) - ConnFile - Conn - Database - (...)
     */
    private Network createNetwork() {
        Network network = new Network(NETWORK);
        boolean prefNodeExist = false;
        String netPrefPath = Network.PREFS_PATH;
        Preferences pref = null;
        String[] prefKeys = null;

        // ### 1. Check if preference node exist.
        try {
            prefNodeExist = Preferences.userRoot().nodeExists(netPrefPath);
        } catch (BackingStoreException e) {
            //e.printStackTrace();
        }
        // #########################################################################

       // ### 2. Set preference node.
       if (prefNodeExist) {
           pref = Preferences.userRoot().node(netPrefPath);
       }
       // #########################################################################

       // ### 3. Get preference keys.
       if (pref != null) {
           try {
               prefKeys = pref.keys();
           } catch (BackingStoreException e) {
               //System.err.println("createNetwork() method, unable to read backing store: " + e);
               //e.printStackTrace();
           } catch (IllegalStateException e) {
               //System.err.println("createNetwork() method, " + e);
               //System.out.println("createNetwork() method, node has been removed!");
           }
       }
       // #########################################################################

       // ### 4. Iterate through preference keys to create ConnFile object
       // and check if corresponding file exist.
       if (prefKeys != null) {

           for (String prefKey : prefKeys) {
               ConnFile connFile = new ConnFile(prefKey);

               String fileName = pref.get(prefKey, null);

               if (fileName != null) {
                   connFile.setFileName(fileName);

                   File file = new File(fileName);

                   if(!file.exists() || file.isDirectory()) {
                       connFile.setState(ConnFileState.BROKEN);
                   }

                   connFile.setParent(network);
                   connFile.setRootLayoutController(this);
                   network.getSubUnits().add(connFile);
               }
           }

       }
       // #########################################################################

       // ### 5. Iterate through network subunits (ConnFile objects)
       // If ConnFile object is encrypted -> ask for password
       // If not password protected or password OK, populate
       // ConnFile object subunits with Conn objects.
       if (!network.getSubUnits().isEmpty()) {
           network.getSubUnits().forEach((subUnit) -> {
               if (!subUnit.getState().equals(ConnFileState.BROKEN)) {

                   List<Conn> listConn = loadConnDataFromFile(subUnit);

                   // If loadConnDataFromFile raise an exception (file is damaged)
                   // it set ConnFile object state to BROKEN and return null value in listConn.
                   if (listConn != null) {

                       // debug mode
                       //System.out.println("String to decrypt: " + listConn.get(0).getPassword());

                       // If first element (Conn) of the list is encrypted, also all others are (with same password)
                       if (listConn.get(0).isPasswordEncrypted()) {
                           subUnit.setState(ConnFileState.ENCRYPTED);
                           if (Pref.INSTANCE.isDecryptConnFilePassAtStart()) decryptConnFile(subUnit);
                       }

                       // debug mode
                            /*
                            for (Conn conn: listConn) {
                                System.out.println(conn);
                            }
                            */

                       if (subUnit.getState().equals(ConnFileState.CLEAR) || subUnit.getState().equals(ConnFileState.DECRYPTED)) {
                           populateSubunit(subUnit, listConn);
                       }
                   }
               }
           });
       }
       // #########################################################################

       // Some sample data, debug mode
       //buildSampleData(network);

       return network;
    }

    /**
     * Populate subUnit (ConnFile object)
     *
     * @param subUnit
     * @param listConn
     */
    public void populateSubunit(ConnFile subUnit, List<Conn> listConn) {

        // Set Conn object reference to his ConnFile parent object
        for (Conn conn : listConn) {
            conn.setParent(subUnit);
        }

        subUnit.getSubUnits().addAll(listConn);
    }

    /**
     * Populate tree with sample data,
     * for testing purpose.
     *
     * @param network
     */
    private void buildSampleData(Network network) {
        // Some sample data, debug mode
        ConnFile file1 = new ConnFile("file1");
        ConnFile file2 = new ConnFile("file2");
        ConnFile file3 = new ConnFile("file3");

        Conn connection1 = new Conn("connection1");
        Conn connection2 = new Conn("connection2");
        Conn connection3 = new Conn("connection3");
        Conn connection4 = new Conn("connection4");
        Conn connection5 = new Conn("connection5");

        DataBase dataBase1 = new DataBase("dataBase1");
        DataBase dataBase2 = new DataBase("dataBase2");
        DataBase dataBase3 = new DataBase("dataBase3");
        DataBase dataBase4 = new DataBase("dataBase4");
        DataBase dataBase5 = new DataBase("dataBase5");

        connection1.getSubUnits().add(dataBase1);

        connection2.getSubUnits().addAll(dataBase2, dataBase3);
        connection3.getSubUnits().addAll(dataBase2, dataBase3);

        connection4.getSubUnits().addAll(dataBase4, dataBase5);
        connection5.getSubUnits().addAll(dataBase4, dataBase5);

        file1.getSubUnits().add(connection1);
        file2.getSubUnits().addAll(connection2, connection3);
        file3.getSubUnits().addAll(connection4, connection5);

        network.getSubUnits().addAll(file1, file2, file3);
    }

    /**
     * Iterate TreeView nodes to print items (debug mode).
     * SRC: http://stackoverflow.com/questions/28342309/iterate-treeview-nodes
     *
     * @param root tree root
     */
    private void printChildren(TreeItem<?> root){
        System.out.println("Current Parent: " + root.getValue());
        for(TreeItem<?> child: root.getChildren()){
            if(child.getChildren().isEmpty()){
                System.out.println(child.getValue());
            } else {
                printChildren(child);
            }
        }
    }

/**
 * Loads connection data from the specified file,
 * unmarshaled with JAXB.
 *
 * @param connFile
 */
    public List<Conn> loadConnDataFromFile(ConnFile connFile) {
        File file = new java.io.File(connFile.getFileName());

        // debug mode
        //System.out.println(file.toString());

        try {
            JAXBContext context = JAXBContext.newInstance(ConnListWrapper.class);

            // debug mode
            //System.out.println(context.toString());

            Unmarshaller um = context.createUnmarshaller();

            // Reading XML from the file and unmarshalling.
            ConnListWrapper wrapper = (ConnListWrapper) um.unmarshal(file);

            // debug mode
            /*
            for (Conn conn: wrapper.getConns()) {
                System.out.println(conn);
            }
            */

            return wrapper.getConns();

        } catch (Exception e) { // catches ANY exception
            provideAlert(Alert.AlertType.ERROR,
                    ALERR_LOAD_DATA_TITLE,
                    ALERR_LOAD_DATA_HEADER,
                    ALERR_LOAD_DATA_CONTENT + file.getPath(), true);

            connFile.setState(ConnFileState.BROKEN);
        }

        return null;
    }

    /**
     * Decrypt ConnFile object
     *
     * @param connFile
     */
    public boolean decryptConnFile(ConnFile connFile) {

        String password = getConnFilePassword(connFile);

        if (password != null) {
            connFile.setPasswordProtected(true);
            connFile.setPassword(password);
            connFile.setState(ConnFileState.DECRYPTED);
            return true;
        }

        return false;
    }

    /**
     * Ask user for password in a dialog.
     * This password is used at ConnFile level
     * to encrypt/decrypt Conn password
     *
     * @param connFile
     * @return password if correct, null otherwise
     */
    private String getConnFilePassword(ConnFile connFile) {

        //TODO When TAB key is pressed to change focus on Cancel button and hit ENTER key
        //Bad password popup is displayed even if password field was let empty (OK button (which is highlighted) like behavior)
        //Also Bad Password dialog has an "Annuler" button who should be a "Cancel" button!

        boolean tryAgain;

        do {
            PasswordDialog pd = new PasswordDialog(connFile.getFileName());
            Optional<String> passwordDialogResult = pd.showAndWait();
            //result.ifPresent(password -> System.out.println(password));

            if (passwordDialogResult.isPresent()) {
                String password = passwordDialogResult.get();

                try {
                    Crypto crypto = new Crypto(password);
                    // If no exception thrown by line below, means that password is correct
                    // If first element (Conn (get(0))) of the list returned by loadConnDataFromFile method is encrypted,
                    // also all others are (with same password)
                    crypto.getDecrypted(loadConnDataFromFile(connFile).get(0).getPassword());

                    return password;

                } catch (Exception e) {

                    //e.printStackTrace();

                    Alert alert = provideAlert(Alert.AlertType.CONFIRMATION,
                            ALCNF_BAD_PASSWORD_TITLE,
                            ALCNF_BAD_PASSWORD_HEADER,
                            ALCNF_BAD_PASSWORD_CONTENT, false);

                    Optional<ButtonType> badPasswordDialogResult = alert.showAndWait();

                    if (badPasswordDialogResult.get() == ButtonType.OK) {
                        // ... user chose OK
                        tryAgain = true;

                    } else {
                        // ... user chose CANCEL or closed the dialog
                        tryAgain = false;
                    }
                }
            } else {
                tryAgain = false;
            }

        } while (tryAgain);

        return null;
    }

    /**
     * Provide alert dialog with optional showAndWait possibility.
     * Return Alert object in case of outside method behavior management needs.
     *
     * @param alertType
     * @param title
     * @param header
     * @param content
     * @param showAndWait
     * @return alert Alert object
     */
    private Alert provideAlert(Alert.AlertType alertType, String title, String header, String content, boolean showAndWait) {
        Alert alert = new Alert(alertType);

        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        if (showAndWait) alert.showAndWait();
        return alert;
    }

}
