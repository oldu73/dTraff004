package ch.stageconcept.dtraff.main;

import ch.stageconcept.dtraff.main.view.RootLayoutController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application class.
 *
 * @author Olivier Durand
 */
public class MainApp extends Application {

    private static final String APP_TITLE = "Data Traffic";
    private static final String ROOT_LAYOUT = "view/RootLayout.fxml";

    public static Stage primaryStage;   // Static reference to primaryStage
    private BorderPane rootLayout;

    /**
     * Constructor
     */
    public MainApp() {}

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(APP_TITLE);

        initRootLayout();
    }

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(ROOT_LAYOUT));
            rootLayout = loader.load();

            // Set scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            // Give the controller access to the main app.
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);

            // Show primaryStage.
            primaryStage.show();

            // RootLayoutController initialize process after main window shows.
            // This way if user is asked for password (decrypt ConnFile at start, user preference),
            // password dialog will be displayed in front of main window.
            // Otherwise (with normal controller automatic call of initialize() method) the password
            // dialog will appear before main window and seems to be lost (like not attached to an application)
            // and since then, to my opinion, the user experience is not really smart in this case.
            controller.subInitialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        // debug mode (testing preferences)
        /*
        if (Pref.INSTANCE.isDecryptConnFilePassAtStart()) {
            Pref.INSTANCE.setDecryptConnFilePassAtStart(false);
        } else {
            Pref.INSTANCE.setDecryptConnFilePassAtStart(true);
        }
        */

        launch(args);
    }

}
