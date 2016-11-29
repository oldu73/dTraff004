package ch.stageconcept.dtraff.main.view;

import ch.stageconcept.dtraff.connection.tree.model.*;
import ch.stageconcept.dtraff.connection.tree.view.ModelTree;
import ch.stageconcept.dtraff.main.MainApp;
import ch.stageconcept.dtraff.connection.unit.model.DbConnect;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

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

    private TreeItem<DbConnect> rootNode;

    private Network network;    // Network description to be used in a treeView : Network (root node) - File - Connection - Database - (...)
    private ModelTree<ConnectionUnit<?>> connectionTree;

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
                ConnectionUnit::getSubUnits,
                ConnectionUnit::nameProperty,
                ConnectionUnit::iconProperty,
                ConnectionUnit::menuProperty,
                unit -> PseudoClass.getPseudoClass(unit.getClass().getSimpleName().toLowerCase()));

        TreeView<ConnectionUnit<?>> connectionTreeView = connectionTree.getTreeView();

        // CSS pseudo class treeView style.
        // !WARNING! In order to use file that reside in resources folder, don’t forget to add a slash before file name!
        connectionTreeView.getStylesheets().add(getClass().getResource("/connectionTreeView.css").toExternalForm());

        rootBorderPane.setLeft(connectionTreeView);

        // Debug mode
        //printChildren(connectionTreeView.getRoot());
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
     * Called when the user selects the File - New Server Connection menu. Opens a dialog to edit
     * details for a new connection.
     */
    @FXML
    private void handleNewConnection() {

        DbConnect tempDbConnect = new DbConnect(mainApp);
        boolean okClicked = mainApp.showConnectionEditDialog(tempDbConnect);
        if (okClicked) {
            mainApp.getDbConnects().add(tempDbConnect);

            /*
            // Update connection tree view with the new entry
            TreeItem<String> serverNode = new TreeItem<String>(
                    tempDbConnect.getName(),
                    new ImageView(serverDefaultIcon)
            );
            */

            // Update connection tree view with the new entry
            TreeItem<DbConnect> serverNode = new TreeItem<>(tempDbConnect);
            rootNode.getChildren().add(serverNode);

            // Maintain connection tree view displayed connection name up to date
            //tempDbConnect.nameProperty().addListener((o, oldVal, newVal) -> connectionTreeView.refresh());

            //TODO Find a solution for the ConnectionEditDialogController Test Connection button side effect that update the edited connection:
            //- pass a temporary copy of the edited connection for testing.
        }

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
     * to be used in a treeView : Network (root node) - File - Connection - Database - (...)
     */
    private Network createNetwork() {
        Network network = new Network("Network");

        // Some sample data, debug mode
	    File file1 = new File("file1");
	    File file2 = new File("file2");
	    File file3 = new File("file3");

	    Connection connection1 = new Connection("connection1");
	    Connection connection2 = new Connection("connection2");
	    Connection connection3 = new Connection("connection3");
	    Connection connection4 = new Connection("connection4");
	    Connection connection5 = new Connection("connection5");

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
