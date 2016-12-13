package ch.stageconcept.dtraff.main.view;

import ch.stageconcept.dtraff.connection.model.*;
import ch.stageconcept.dtraff.connection.util.ConnEditor;
import ch.stageconcept.dtraff.connection.view.ModelTree;
import ch.stageconcept.dtraff.connection.util.DbType;
import ch.stageconcept.dtraff.main.MainApp;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;

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

    // Reference to the main application
    private MainApp mainApp;

    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private MenuItem newServerConnectionMenuItem;

    @FXML
    private MenuItem editServerConnectionMenuItem;

    private Network network;    // Network description to be used in a treeView : Network (root node) - File - Conn - Database - (...)
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

        // Disable tool bar menu New Server Conn if no item or not a File instance selected in Connections treeView
        newServerConnectionMenuItem.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        connectionTreeView.getSelectionModel().getSelectedItem() == null ||
                                !(connectionTreeView.getSelectionModel().getSelectedItem().getValue() instanceof File),
                connectionTreeView.getSelectionModel().selectedItemProperty()));

        // Disable tool bar menu Edit Server Conn if no item or not a Conn instance selected in Connections treeView
        editServerConnectionMenuItem.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        connectionTreeView.getSelectionModel().getSelectedItem() == null ||
                                !(connectionTreeView.getSelectionModel().getSelectedItem().getValue() instanceof Conn),
                connectionTreeView.getSelectionModel().selectedItemProperty()));
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
     * Called when the user selects the File - New Server Conn menu. Opens a dialog to edit
     * details for a new connection.
     */
    @FXML
    private void handleNewConnection() {
        // new Conn instance with default name value
        Conn conn = new Conn(DbType.INSTANCE.getDbDescriptorMap().get(DbType.MYSQL_KEY).getName());

        if (ConnEditor.INSTANCE.supply(conn)) {
            // The tool bar menu is disabled if none or not a File is selected in Conn treeView,
            // so the item could only be a File -> (cast)
            File file = (File) connectionTreeView.getSelectionModel().getSelectedItem().getValue();
            file.getSubUnits().add(conn);
        }

        //TODO Find a solution for the ConnEditDialogController Test Conn button side effect that update the edited conn:
        //- pass a temporary copy of the edited conn for testing.
    }

    /**
     * Called when the user selects the File - Edit Server Conn menu. Opens a dialog to edit
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
     * to be used in a treeView : Network (root node) - File - Conn - Database - (...)
     */
    private Network createNetwork() {
        Network network = new Network("Network");

        boolean exists = false;

        try {
            exists = Preferences.userRoot().nodeExists(Network.PREFS_PATH);
        } catch (BackingStoreException e) {
            //e.printStackTrace();
        }

        if (exists) {
            Preferences preferences = Preferences.userRoot().node(Network.PREFS_PATH);

            String[] keys = new String[0];

            try {
                keys = preferences.keys();

                for (String key : keys) {
                    File file = new File(key);
                    file.setFileName(preferences.get(key, null));
                    network.getSubUnits().add(file);
                    //System.out.println(key + " = " + preferences.get(key, null));
                }

            } catch (BackingStoreException e) {
                //System.err.println("createNetwork() method, unable to read backing store: " + e);
                //e.printStackTrace();
            } catch (IllegalStateException e) {
                //System.err.println("createNetwork() method, " + e);
                //System.out.println("createNetwork() method, node has been removed!");
            }
        }

        /*
        // Some sample data, debug mode
	    File file1 = new File("file1");
	    File file2 = new File("file2");
	    File file3 = new File("file3");

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
        */

        return network ;
    }

    /**
     * Iterate TreeView nodes to print items (debug mode).
     * SRC: http://stackoverflow.com/questions/28342309/iterate-treeview-nodes
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

}
