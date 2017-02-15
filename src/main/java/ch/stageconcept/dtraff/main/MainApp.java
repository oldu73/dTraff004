package ch.stageconcept.dtraff.main;

import ch.stageconcept.dtraff.main.view.RootLayoutController;
import ch.stageconcept.dtraff.preference.model.Pref;
import ch.stageconcept.dtraff.util.I18N;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Main application class.
 *
 * @author Olivier Durand
 */
public class MainApp extends Application {

    private static final String APP_TITLE = RootLayoutController.ALINF_ABOUT_HEADER;
    private static final String ROOT_LAYOUT = "view/RootLayout.fxml";
    public static final String I18N_BASE = "text";

    public static Stage PRIMARY_STAGE;   // Static reference to primaryStage
    private BorderPane rootLayout;
    private Scene scene;
    private RootLayoutController controller;

    private String languageString;
    public static ResourceBundle TEXT_BUNDLE;

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
        Locale locale = new Locale(languageString);
        I18N.setLocale(locale);
        TEXT_BUNDLE = ResourceBundle.getBundle(I18N_BASE, locale);

        Pref.getPref().addPreferenceChangeListener(evt -> {
            String prefLanguageString = Pref.getLanguage();
            if (!languageString.equals(prefLanguageString)) {
                languageString = prefLanguageString;
                TEXT_BUNDLE = ResourceBundle.getBundle(I18N_BASE, new Locale(languageString));
            }
        });

        loadView();

    }

    /**
     * Load view with local resource
     */
    private void loadView() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();

            // Resource bundles name for multi-languages support is set.
            loader.setResources(TEXT_BUNDLE);

            //rootLayout = loader.load(this.getClass().getResource(ROOT_LAYOUT).openStream());
            loader.setLocation(this.getClass().getResource(ROOT_LAYOUT));
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

        this.PRIMARY_STAGE = primaryStage;
        this.PRIMARY_STAGE.setTitle(APP_TITLE);

        // Manage possible empty clear/decrypted files, or exit confirmation.
        primaryStage.setOnCloseRequest(controller.getConfirmCloseEventHandler());

        primaryStage.setScene(scene);

        if (!Pref.isDecryptFilePassPopUpAtStartOrOnOpen() && !Pref.isErrorLoadingFilePopUpAtStartOrOnOpen()) {
            controller.getRootBorderPane().getChildren().remove(controller.getInitializingLabel());
        }

        // Show PRIMARY_STAGE.
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
