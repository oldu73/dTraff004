package ch.stageconcept.dtraff.util;

import ch.stageconcept.dtraff.main.MainApp;
import javafx.scene.control.Alert;
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

        Alert alert = null;

        if (alertType.equals(Alert.AlertType.CONFIRMATION)) {

            // SRC: http://stackoverflow.com/questions/36309385/how-to-change-the-text-of-yes-no-buttons-in-javafx-8-alert-dialogs

            ButtonType foo = new ButtonType(MainApp.TEXT_BUNDLE.getString("button.ok"), ButtonBar.ButtonData.OK_DONE);
            ButtonType bar = new ButtonType(MainApp.TEXT_BUNDLE.getString("button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

            alert = new Alert(alertType,
                    content,
                    foo,
                    bar);

            alert.setTitle(title);
            alert.setHeaderText(header);

            /*
            Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setText("OK");

            // In certain conditions, cancel button text is "Annuler", so, just to be sure... (as well for above OK button (we never know ;-))

            Button cancelButton = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
            cancelButton.setText("Cancel");
            */

        }

        if (alert == null) {
            alert = new Alert(alertType);

            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
        }

        if (showAndWait) alert.showAndWait();

        return alert;
    }
}
