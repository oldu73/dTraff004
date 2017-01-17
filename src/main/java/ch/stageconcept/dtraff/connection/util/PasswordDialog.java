package ch.stageconcept.dtraff.connection.util;

import ch.stageconcept.dtraff.connection.model.ConnFile;
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
        setTitle(ConnFile.MENU_ENTER_PASSWORD);
        setHeaderText("Please enter password for: \n" + fileName);

        ButtonType okButton = new ButtonType("OK", ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

        //ButtonType passwordButtonType = new ButtonType("Decrypt", ButtonData.OK_DONE);
        //getDialogPane().getButtonTypes().addAll(passwordButtonType, ButtonType.CANCEL);
        getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        // Set the icon (must be included in the project).
        getDialogPane().setGraphic(new ImageView(this.getClass().getResource("/password001.png").toString()));

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");

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