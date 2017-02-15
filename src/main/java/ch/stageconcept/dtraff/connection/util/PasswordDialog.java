package ch.stageconcept.dtraff.connection.util;

import ch.stageconcept.dtraff.connection.model.ConnFile;
import ch.stageconcept.dtraff.main.MainApp;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * JavaFX doesn't have a password dialog, so Simon Morgan wrote one.
 * SRC: https://gist.github.com/drguildo/ba2834bf52d624113041
 *
 * @author Simon Morgan
 * Adapted by Olivier Durand
 */
public class PasswordDialog extends Dialog<String> {

    private PasswordField passwordField;

    public PasswordDialog(String fileName) {
        setTitle(MainApp.TEXT_BUNDLE.getString("passwordDialog.title"));
        setHeaderText(MainApp.TEXT_BUNDLE.getString("passwordDialog.header") + "\n" + fileName);

        ButtonType okButton = new ButtonType(MainApp.TEXT_BUNDLE.getString("button.ok"), ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType(MainApp.TEXT_BUNDLE.getString("button.cancel"), ButtonData.CANCEL_CLOSE);

        //ButtonType passwordButtonType = new ButtonType("Decrypt", ButtonData.OK_DONE);
        //getDialogPane().getButtonTypes().addAll(passwordButtonType, ButtonType.CANCEL);
        getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        // Set the icon (must be included in the project).
        getDialogPane().setGraphic(new ImageView(this.getClass().getResource("/password001.png").toString()));

        passwordField = new PasswordField();
        passwordField.setPromptText(MainApp.TEXT_BUNDLE.getString("passwordDialog.promptText"));

        HBox hBox = new HBox();
        hBox.getChildren().add(passwordField);
        hBox.setPadding(new Insets(20));

        HBox.setHgrow(passwordField, Priority.ALWAYS);

        getDialogPane().setContent(hBox);

        Platform.runLater(() -> passwordField.requestFocus());

        setResultConverter(dialogButton -> {
            //if (dialogButton == passwordButtonType) {
            if (dialogButton == okButton) {
                return passwordField.getText();
            }
            return null;
        });
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }
}