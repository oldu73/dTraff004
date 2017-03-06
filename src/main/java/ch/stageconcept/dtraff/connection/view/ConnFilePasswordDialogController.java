package ch.stageconcept.dtraff.connection.view;

import ch.stageconcept.dtraff.connection.model.ConnFile;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

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
    private Pane pane1;

    @FXML
    private Pane pane2;

    @FXML
    private Pane pane3;

    @FXML
    private Pane pane4;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ImageView passwordOkIcon;

    @FXML
    private PasswordField repeatPasswordField;

    @FXML
    private ImageView repeatPasswordOkIcon;

    private String passwordToCheck;
    private BooleanProperty passwordOk;

    // Getters & Setters
    // #################

    public void setPasswordToCheck(String passwordToCheck) {
        this.passwordToCheck = passwordToCheck;
    }

    public boolean isPasswordOk() {
        return passwordOk.get();
    }

    public BooleanProperty passwordOkProperty() {
        return passwordOk;
    }

    public String getPassword() {
        return passwordField.getText();
    }

    // Methods
    // #######

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        //
    }

    /**
     * Initialization called from outside.
     */
    public void postInitialize() {

        // SRC: http://stackoverflow.com/questions/22841000/how-to-change-the-color-of-pane-in-javafx
        // Below code is used to check GridPane column width
        /*
        pane1.setBackground(new Background(new BackgroundFill(Color.web("#d43232"), CornerRadii.EMPTY, Insets.EMPTY)));
        pane2.setBackground(new Background(new BackgroundFill(Color.web("#ffe34c"), CornerRadii.EMPTY, Insets.EMPTY)));
        pane3.setBackground(new Background(new BackgroundFill(Color.web("#6ca158"), CornerRadii.EMPTY, Insets.EMPTY)));
        pane4.setBackground(new Background(new BackgroundFill(Color.web("#c4a155"), CornerRadii.EMPTY, Insets.EMPTY)));
        */

        Image ok = new Image(ICON_OK_FILENAME);
        Image notOk = new Image(ICON_NOT_OK_FILENAME);

        passwordOkIcon.setImage(notOk);
        repeatPasswordOkIcon.setImage(notOk);

        passwordOk = new SimpleBooleanProperty();

        passwordOk.bind(Bindings.createBooleanBinding(() -> {

                    String password = passwordField.getText();
                    String repeatPassword = repeatPasswordField.getText();

                    Predicate<String> isEntryValid = entry -> entry != null && entry.length() > 0;

                    boolean passwordValid = isEntryValid.test(password) && !password.equals(passwordToCheck);
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
