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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Optional;

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

        Platform.setImplicitExit(false);
        // Called before last window has been closed
        // SRC: http://stackoverflow.com/questions/17003906/prevent-cancel-closing-of-primary-stage-in-javafx-2-2
        //primaryStage.setOnCloseRequest(event -> controller.handleExit(event));
        /*
        primaryStage.setOnCloseRequest(event -> {
            System.out.println(controller.isExit());
            if (!controller.isExit()) controller.handleExit();
            else Platform.exit();   //event.consume();
        });
        */

        primaryStage.setOnCloseRequest(confirmCloseEventHandler);

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

    /**
     * Called automatically after last window has been closed
     *
     * SRC: http://stackoverflow.com/questions/26619566/javafx-stage-close-handler
     */
    @Override
    public void stop(){
        //controller.handleExit();
    }

    private EventHandler<WindowEvent> confirmCloseEventHandler = event -> {
        Alert closeConfirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to exit?"
        );
        Button exitButton = (Button) closeConfirmation.getDialogPane().lookupButton(
                ButtonType.OK
        );
        exitButton.setText("Exit");
        closeConfirmation.setHeaderText("Confirm Exit");
        closeConfirmation.initModality(Modality.APPLICATION_MODAL);
        closeConfirmation.initOwner(primaryStage);

        // normally, you would just use the default alert positioning,
        // but for this simple sample the main stage is small,
        // so explicitly position the alert so that the main window can still be seen.
        closeConfirmation.setX(primaryStage.getX());
        closeConfirmation.setY(primaryStage.getY() + primaryStage.getHeight());

        Optional<ButtonType> closeResponse = closeConfirmation.showAndWait();
        if (!ButtonType.OK.equals(closeResponse.get())) {
            System.exit(0);
            //event.consume();


        }
    };

}
