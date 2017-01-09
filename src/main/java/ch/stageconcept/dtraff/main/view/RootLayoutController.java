package ch.stageconcept.dtraff.main.view;

import ch.stageconcept.dtraff.connection.model.*;
import ch.stageconcept.dtraff.connection.util.*;
import ch.stageconcept.dtraff.connection.view.ModelTree;
import ch.stageconcept.dtraff.main.MainApp;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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

    private static final String NETWORK = "Network";

    // Reference to the main application
    private MainApp mainApp;

    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private MenuItem serverConnectionMenuItem;

    @FXML
    private MenuItem newServerConnectionMenuItem;

    @FXML
    private MenuItem editServerConnectionMenuItem;

    private Network network;    // Network description to be used in a treeView : Network (root node) - ConnFile - Conn - Database - (...)
    private ModelTree<ConnUnit<?>> connectionTree;
    private TreeView<ConnUnit<?>> connectionTreeView;

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
        connectionTreeView.getStylesheets().add(getClass().getResource("/connectionTreeView.css").toExternalForm());

        rootBorderPane.setLeft(connectionTreeView);

        // Debug mode
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
            if (newValue.getValue() instanceof ConnFile &&
                    (((ConnFile) newValue.getValue()).getState().equals(ConnFileState.CLEAR) ||
                            ((ConnFile) newValue.getValue()).getState().equals(ConnFileState.DECRYPTED))) {
                newServerConnectionMenuItem.setDisable(false);
            } else {
                newServerConnectionMenuItem.setDisable(true);
            }
        });

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
     * Called when the user selects the ConnFile - New Server Conn menu. Opens a dialog to edit
     * details for a new connection.
     */
    @FXML
    private void handleNewConnection() {
        // new Conn instance with default name value
        Conn conn = new Conn(DbType.INSTANCE.getDbDescriptorMap().get(DbType.MYSQL_KEY).getName());

        if (ConnEditor.INSTANCE.supply(conn)) {
            // The tool bar menu is disabled if none or not a ConnFile is selected in Conn treeView,
            // so the item could only be a ConnFile -> (cast)
            ConnFile file = (ConnFile) connectionTreeView.getSelectionModel().getSelectedItem().getValue();
            file.getSubUnits().add(conn);
        }

        //TODO Find a solution for the ConnEditDialogController Test Conn button side effect that update the edited conn:
        //- pass a temporary copy of the edited conn for testing.
    }

    /**
     * Called when the user selects the ConnFile - Edit Server Conn menu. Opens a dialog to edit
     * details for the existing connection.
     */
    @FXML
    private void handleEditConnection() {
        // The tool bar menu is disabled if none or not a Conn is selected in Conn treeView,
        // so the item could only be a Conn -> (cast)
        Conn conn = (Conn) connectionTreeView.getSelectionModel().getSelectedItem().getValue();

        ConnEditor.INSTANCE.supply(conn);

        //TODO Find a solution for the ConnEditDialogController Test Conn button side effect that update the edited conn:
        //- pass a temporary copy of the edited conn for testing.
    }

    /**
     * Opens an about dialog.
     */
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        //TODO put text in static String
        alert.setTitle("Data Traffic");
        alert.setHeaderText("About");
        alert.setContentText("Author: Olivier Durand\nWebsite: http://www.stageconcept.ch");

        alert.showAndWait();
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

                   // debug mode
                   //System.out.println("String to decrypt: " + listConn.get(0).getPassword());

                   // If first element (Conn) of the list is encrypted, also all others are (with same password)
                   if (listConn.get(0).isPasswordEncrypted()) {
                       subUnit.setState(ConnFileState.ENCRYPTED);
                       decryptConnFile(subUnit);
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
    private void populateSubunit(ConnFile subUnit, List<Conn> listConn) {

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
    private List<Conn> loadConnDataFromFile(ConnFile connFile) {
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

            Alert alert = new Alert(Alert.AlertType.ERROR);
            //TODO put text in static String
            alert.setTitle("Error");
            alert.setHeaderText("Could not load data");
            alert.setContentText("Could not load data from file:\n" + file.getPath());

            alert.showAndWait();
        }

        return null;
    }

    /**
     * Decrypt ConnFile object
     *
     * @param connFile
     */
    private boolean decryptConnFile(ConnFile connFile) {

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

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    //TODO put text in static String
                    alert.setTitle("Password Dialog");
                    alert.setHeaderText("Bad password!");
                    alert.setContentText("Try again?");

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

}
