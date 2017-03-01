package ch.stageconcept.dtraff.connection.view;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.util.function.Predicate;

/**
 * Dialog controller to edit password
 * of a ConnFile object instance.
 *
 * @author Olivier Durand
 */
public class ConnFilePasswordDialogController {

    // Icon resource file name and size
    private static final String ICON_OK_FILENAME = "ok001.png";
    private static final String ICON_NOT_OK_FILENAME = "notOk001.png";

    @FXML
    private GridPane gridPane;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ImageView passwordOkIcon;

    @FXML
    private PasswordField repeatPasswordField;

    @FXML
    private ImageView repeatPasswordOkIcon;

    private BooleanProperty passwordOk;

    public boolean isPasswordOk() {
        return passwordOk.get();
    }

    public BooleanProperty passwordOkProperty() {
        return passwordOk;
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {

        Image ok = new Image(ICON_OK_FILENAME);
        Image notOk = new Image(ICON_NOT_OK_FILENAME);

        passwordOkIcon.setImage(notOk);
        repeatPasswordOkIcon.setImage(notOk);

        passwordOk = new SimpleBooleanProperty();

        passwordOk.bind(Bindings.createBooleanBinding(() -> {

            String password = passwordField.getText();
            String repeatPassword = repeatPasswordField.getText();

            Predicate<String> isEntryValid = entry -> entry != null && entry.length() > 0;

            boolean passwordValid = isEntryValid.test(password);
            boolean repeatPasswordValid = isEntryValid.test(repeatPassword);

            if (passwordValid) passwordOkIcon.setImage(ok);
            else passwordOkIcon.setImage(notOk);

            if (passwordValid && repeatPasswordValid && password.equals(repeatPassword)) {
                repeatPasswordOkIcon.setImage(ok);
                return true;
            }

            repeatPasswordOkIcon.setImage(notOk);
            return false;},

                passwordField.textProperty(),
                repeatPasswordField.textProperty()));

    }

}
