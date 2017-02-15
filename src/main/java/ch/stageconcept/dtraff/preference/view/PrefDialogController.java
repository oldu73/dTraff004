package ch.stageconcept.dtraff.preference.view;

import ch.stageconcept.dtraff.preference.model.Pref;
import ch.stageconcept.dtraff.util.I18N;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.Locale;

import static org.apache.commons.lang3.text.WordUtils.capitalize;

/**
 * Dialog to edit user preferences.
 *
 * @author Olivier Durand
 */
public class PrefDialogController {

    @FXML
    private CheckBox splashScreenCheckBox;

    @FXML
    private CheckBox errorLoadingDataFromFilePopUpAtStartOrOnOpenCheckBox;

    @FXML
    private CheckBox decryptConnFilePassAtStartOrOnOpenCheckBox;

    @FXML
    private GridPane miscGridPane;

    private ComboBox<Locale> languageComboBox;

    @FXML
    private CheckBox warnRemoveEmptyFileOnCloseCheckBox;

    @FXML
    private CheckBox warnExitingOnCloseCheckBox;

    @FXML
    private Button okButton;

    @FXML
    private Button cancelButton;

    private Stage dialogStage;
    private boolean okClicked = false;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {

        splashScreenCheckBox.setSelected(Pref.isSplashScreen());
        errorLoadingDataFromFilePopUpAtStartOrOnOpenCheckBox.setSelected(Pref.isErrorLoadingFilePopUpAtStartOrOnOpen());
        decryptConnFilePassAtStartOrOnOpenCheckBox.setSelected(Pref.isDecryptFilePassPopUpAtStartOrOnOpen());

        // ### Language

        languageComboBox = new ComboBox<>();

        ObservableList<Locale> languageOptions = FXCollections.observableArrayList(new Locale("en"), new Locale("fr"));

        languageComboBox.setConverter(new StringConverter<Locale>() {
            @Override
            public String toString(Locale object) {
                return capitalize(object.getDisplayLanguage(object));
            }

            @Override
            public Locale fromString(String string) {
                return null;
            }
        });

        languageComboBox.setCellFactory(p -> new ListCell<Locale>() {
            @Override
            protected void updateItem(Locale item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(capitalize(item.getDisplayLanguage(item)));
                }
            }
        });

        languageComboBox.setItems(languageOptions);
        languageComboBox.getSelectionModel().select(new Locale(Pref.getLanguage()));
        miscGridPane.add(languageComboBox, 1, 0);

        // ############

        warnRemoveEmptyFileOnCloseCheckBox.setSelected(Pref.isWarnRemoveEmptyFileOnClose());
        warnExitingOnCloseCheckBox.setSelected(Pref.isWarnExitingOnClose());

    }

    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Returns true if the user clicked OK, false otherwise.
     *
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        okClicked = true;

        Pref.setSplashScreen(splashScreenCheckBox.isSelected());
        Pref.setErrorLoadingFilePopUpAtStartOrOnOpen(errorLoadingDataFromFilePopUpAtStartOrOnOpenCheckBox.isSelected());
        Pref.setDecryptFilePassPopUpAtStartOrOnOpen(decryptConnFilePassAtStartOrOnOpenCheckBox.isSelected());

        String languageString = languageComboBox.getSelectionModel().getSelectedItem().toString();
        if (!languageString.equals(Pref.getLanguage())) {
            Pref.setLanguage(languageString);
            I18N.setLocale(new Locale(languageString));
        }

        Pref.setWarnRemoveEmptyFileOnClose(warnRemoveEmptyFileOnCloseCheckBox.isSelected());
        Pref.setWarnExitingOnClose(warnExitingOnCloseCheckBox.isSelected());

        dialogStage.close();
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

}
