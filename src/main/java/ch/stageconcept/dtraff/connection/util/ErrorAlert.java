package ch.stageconcept.dtraff.connection.util;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Show alert message
 *
 * @author Olivier Durand
 */
public enum ErrorAlert {

    INSTANCE;

    public void show(Stage stage, String title, String header, String body) {
        // Show the error message.
        Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(body);

        alert.showAndWait();
    }
}
