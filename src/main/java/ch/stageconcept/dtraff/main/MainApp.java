package ch.stageconcept.dtraff.main;

import ch.stageconcept.dtraff.main.view.RootLayoutController;
import ch.stageconcept.dtraff.preference.model.Pref;
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

    private static final String APP_TITLE = RootLayoutController.ALINF_ABOUT_HEADER;
    private static final String ROOT_LAYOUT = "view/RootLayout.fxml";

    public static Stage primaryStage;   // Static reference to primaryStage
    private BorderPane rootLayout;
    private Scene scene;
    private RootLayoutController controller;

    /**
     * Constructor
     */
    public MainApp() {}

    /**
     * Initializes the root layout, scene and controller.
     */
    @Override
    public void init() {

        if (Pref.isSplashScreen()) {
            // (Do some heavy lifting)

            // Let preloader some time to appear
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(ROOT_LAYOUT));
            rootLayout = loader.load();

            // Set scene containing the root layout.
            scene = new Scene(rootLayout);

            controller = loader.getController();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(APP_TITLE);

        primaryStage.setScene(scene);

        if (!Pref.isDecryptFilePassPopUpAtStartOrOnOpen() && !Pref.isErrorLoadingFilePopUpAtStartOrOnOpen()) {
            controller.getRootBorderPane().getChildren().remove(controller.getInitializingLabel());
        }

        // Show primaryStage.
        primaryStage.show();

        // RootLayoutController initialize process after main window shows.
        // This way if user is asked for password (decrypt ConnFile at start, user preference),
        // password dialog will be displayed in front of main window.
        // Otherwise (with normal controller automatic call of initialize() method) the password
        // dialog will appear before main window and seems to be lost (like not attached to an application)
        // and since then, to my opinion, the user experience is not really smart in this case.
        controller.subInitialize();
    }

    // old version
    //public static void main(String[] args) {

        // debug mode (testing preferences)
        /*
        if (Pref.isDecryptFilePassPopUpAtStartOrOnOpen()) {
            Pref.setDecryptFilePassPopUpAtStartOrOnOpen(false);
        } else {
            Pref.setDecryptFilePassPopUpAtStartOrOnOpen(true);
        }
        */

        //launch(args);
    //}

}
