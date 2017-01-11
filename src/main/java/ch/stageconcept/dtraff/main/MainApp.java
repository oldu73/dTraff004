package ch.stageconcept.dtraff.main;

import ch.stageconcept.dtraff.main.view.RootLayoutController;
import ch.stageconcept.dtraff.preference.Pref;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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

    public static void main(String[] args) {

        // debug mode (testing preferences)
        if (Pref.INSTANCE.isDecryptConnFilePassAtStart()) {
            Pref.INSTANCE.setDecryptConnFilePassAtStart(false);
        } else {
            Pref.INSTANCE.setDecryptConnFilePassAtStart(true);
        }

        launch(args);
    }

}
