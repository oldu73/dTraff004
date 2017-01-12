package ch.stageconcept.dtraff.main;

import ch.stageconcept.dtraff.main.view.RootLayoutController;
import javafx.application.Preloader;
import javafx.application.Preloader.StateChangeNotification.Type;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Main preloader class.
 * SRC: https://blog.codecentric.de/en/2015/09/javafx-how-to-easily-implement-application-preloader-2/
 *
 * @author Aron Sreder
 * Adapted by Olivier Durand
 */
public class MainPreloader extends Preloader {
    private Stage preloaderStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.preloaderStage = primaryStage;

        primaryStage.initStyle(StageStyle.UNDECORATED);

        VBox loading = new VBox(20);
        loading.setMaxWidth(Region.USE_PREF_SIZE);
        loading.setMaxHeight(Region.USE_PREF_SIZE);
        loading.getChildren().add(new Label(RootLayoutController.ALINF_ABOUT_HEADER + "\nLoading, please wait..."));
        loading.getChildren().add(new ProgressBar());

        BorderPane root = new BorderPane(loading);
        Scene scene = new Scene(root);

        primaryStage.setWidth(300);
        primaryStage.setHeight(150);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification stateChangeNotification) {
        if (stateChangeNotification.getType() == Type.BEFORE_START) {
            preloaderStage.hide();
        }
    }

}
