package ch.stageconcept.dtraff.main.view;

import ch.stageconcept.dtraff.main.MainApp;
import ch.stageconcept.dtraff.connection.model.DbConnect;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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
    private TreeView<String> connectionTreeView;

    private TreeItem<String> rootNode;

    private final Node rootIcon = new ImageView(new Image(getClass().getResourceAsStream("/network001.gif")));
    private final Image serverOkIcon = new Image(getClass().getResourceAsStream("/serverDefault001.gif"));

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {

        // Connection tree view
        rootNode = new TreeItem<String>("Network", rootIcon);
        connectionTreeView.setRoot(rootNode);
        rootNode.setExpanded(true);
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
        DbConnect tempDbConnect = new DbConnect();
        boolean okClicked = mainApp.showConnectionEditDialog(tempDbConnect);
        if (okClicked) {
            mainApp.getDbConnects().add(tempDbConnect);

            // Update connection tree view with the new entry
            TreeItem<String> serverNode = new TreeItem<String>(
                    tempDbConnect.getName(),
                    new ImageView(serverOkIcon)
            );

            rootNode.getChildren().add(serverNode);
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
