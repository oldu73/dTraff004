package ch.stageconcept.dtraff.main;

import ch.stageconcept.dtraff.main.view.RootLayoutController;
import ch.stageconcept.dtraff.preference.model.Pref;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.prefs.PreferenceChangeListener;

/**
 * Main application class.
 *
 * @author Olivier Durand
 */
public class MainApp extends Application {

    private static final String APP_TITLE = RootLayoutController.ALINF_ABOUT_HEADER;
    private static final String ROOT_LAYOUT = "view/RootLayout.fxml";
    private static final String I18N_BASE = "text";

    public static Stage primaryStage;   // Static reference to primaryStage
    private BorderPane rootLayout;
    private Scene scene;
    private RootLayoutController controller;

    private String languageString;
    private BorderPane borderPane;

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

        languageString = Pref.getLanguage();

        Pref.getPref().addPreferenceChangeListener(evt -> {
            String prefLanguageString = Pref.getLanguage();
            if (!languageString.equals(prefLanguageString)) {
                languageString = prefLanguageString;
                System.out.println(languageString);
                loadView(new Locale(languageString), true);
            }
        });

        loadView(new Locale(languageString), false);

    }

    /**
     * Load view with local resource
     *
     * @param locale
     */
    private void loadView(Locale locale, boolean changed) {
        System.out.println("local " + locale);
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();

            // Resource bundles name for multi-languages support is set.
            loader.setResources(ResourceBundle.getBundle(I18N_BASE, locale));

            //rootLayout = loader.load(this.getClass().getResource(ROOT_LAYOUT).openStream());
            loader.setLocation(this.getClass().getResource(ROOT_LAYOUT));
            rootLayout = loader.load();

            // Set scene containing the root layout.
            scene = new Scene(rootLayout);

            controller = loader.getController();

            if (changed) {
                Pane pane = (BorderPane) loader.load(this.getClass().getResource(ROOT_LAYOUT).openStream());
                borderPane.getChildren().removeAll();
                borderPane.setCenter(pane);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(APP_TITLE);

        // Manage possible empty clear/decrypted files, or exit confirmation.
        primaryStage.setOnCloseRequest(controller.getConfirmCloseEventHandler());

        primaryStage.setScene(scene);

        if (!Pref.isDecryptFilePassPopUpAtStartOrOnOpen() && !Pref.isErrorLoadingFilePopUpAtStartOrOnOpen()) {
            controller.getRootBorderPane().getChildren().remove(controller.getInitializingLabel());
        }

        borderPane = controller.getRootBorderPane();

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

    /**
     * Called automatically after last window has been closed
     *
     * SRC: http://stackoverflow.com/questions/26619566/javafx-stage-close-handler
     */
    //@Override
    //public void stop(){}

}
