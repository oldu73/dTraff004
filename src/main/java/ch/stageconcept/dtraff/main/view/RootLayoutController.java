package ch.stageconcept.dtraff.main.view;

import ch.stageconcept.dtraff.main.MainApp;
import ch.stageconcept.dtraff.connection.model.DbConnect;
import ch.stageconcept.dtraff.main.util.SimpleConnectionTreeCell;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.WindowEvent;

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
    private TreeView<DbConnect> connectionTreeView;

    private TreeItem<DbConnect> rootNode;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {

        // Connection tree view cell factory
        connectionTreeView.setCellFactory(tv -> new SimpleConnectionTreeCell());

        // Just for root
        DbConnect rootDbConnect = new DbConnect(mainApp);
        rootDbConnect.setName("Network");
        rootDbConnect.setIcon(new ImageView("/network001.gif"));

        // Connection tree view
        rootNode = new TreeItem<DbConnect>(rootDbConnect);
        connectionTreeView.setRoot(rootNode);
        rootNode.setExpanded(true);
        // Hide the root Item.
        connectionTreeView.setShowRoot(false);

        /*
        // Connection tree view, contextual menu
        final ContextMenu contextMenu = new ContextMenu();

        MenuItem item1 = new MenuItem("Edit");
        item1.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                System.out.println("Edit");
            }
        });

        contextMenu.getItems().addAll(item1);

        connectionTreeView.setContextMenu(contextMenu);
        */
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
            tempDbConnect.nameProperty().addListener((o, oldVal, newVal) -> connectionTreeView.refresh());

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

}
