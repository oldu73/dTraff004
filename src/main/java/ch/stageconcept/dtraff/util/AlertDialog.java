package ch.stageconcept.dtraff.util;

import ch.stageconcept.dtraff.main.MainApp;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * Alert dialog message popup
 *
 * @author Olivier Durand
 */
public enum AlertDialog {

    ;

    /**
     * Provide alert dialog with optional showAndWait possibility.
     * Return Alert object in case of outside method behavior management needs.
     *
     * @param stage
     * @param alertType
     * @param title
     * @param header
     * @param content
     * @param showAndWait
     * @return alert Alert object
     */
    public static Alert provide(Stage stage, Alert.AlertType alertType, String title, String header, String content, boolean showAndWait) {

        Alert alert = new Alert(alertType);

        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        if (alertType.equals(Alert.AlertType.CONFIRMATION)) {

            Button cancelButton = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
            cancelButton.setText(MainApp.TEXT_BUNDLE.getString("button.cancel"));

        }

        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText(MainApp.TEXT_BUNDLE.getString("button.ok"));

        if (showAndWait) alert.showAndWait();

        return alert;
    }
}
