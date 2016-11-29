package ch.stageconcept.dtraff.main;

import ch.stageconcept.dtraff.main.view.RootLayoutController;
import ch.stageconcept.dtraff.connection.unit.model.DbConnect;
import ch.stageconcept.dtraff.connection.unit.view.ConnectionEditDialogController;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application class.
 *
 * @author Olivier Durand
 */
public class MainApp extends Application {

    public static Stage primaryStage;   // Static reference to primaryStage
    private BorderPane rootLayout;

    // The server connections data as an observable list of DbConnect objects.
    private ObservableList<DbConnect> dbConnects = FXCollections.observableArrayList();

    /**
     * Constructor
     */
    public MainApp() {
        //TODO load connections data from preference file
    }

    /**
     * Returns the server connections data as an observable list of DbConnect objects.
     * @return
     */
    public ObservableList<DbConnect> getDbConnects() {
        return dbConnects;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Data Traffic");

        initRootLayout();
    }

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class
                    .getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            // Give the controller access to the main app.
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the main stage.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Opens a dialog to edit details for the specified dbConnect. If the user
     * clicks OK, the changes are saved into the provided dbConnect object and true
     * is returned.
     *
     * @param dbConnect the dbConnect object to be edited
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean showConnectionEditDialog(DbConnect dbConnect) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("../connection/unit/view/ConnectionEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Connection");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the dbConnect into the controller.
            ConnectionEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            //controller.setConnection(dbConnect);

            // Disable resize
            dialogStage.setResizable(false);

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
