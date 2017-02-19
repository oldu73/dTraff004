package ch.stageconcept.dtraff.preference.view;

import ch.stageconcept.dtraff.preference.model.Pref;
import ch.stageconcept.dtraff.util.I18N;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    private GridPane miscGridPane;

    @FXML
    private Label startLabel;

    @FXML
    private Label miscLabel;

    @FXML
    private Label closeLabel;

    @FXML
    private Label splashScreenLabel;

    @FXML
    private CheckBox splashScreenCheckBox;

    @FXML
    private Label errorLoadingDataFromFilePopUpAtStartOrOnOpenLabel;

    @FXML
    private CheckBox errorLoadingDataFromFilePopUpAtStartOrOnOpenCheckBox;

    @FXML
    private Label decryptConnFilePassAtStartOrOnOpenLabel;

    @FXML
    private CheckBox decryptConnFilePassAtStartOrOnOpenCheckBox;

    @FXML
    private Label languageLabel;

    private ComboBox<Locale> languageComboBox;

    @FXML
    private Label warnRemoveEmptyFileOnCloseLabel;

    @FXML
    private CheckBox warnRemoveEmptyFileOnCloseCheckBox;

    @FXML
    private Label warnExitingOnCloseLabel;

    @FXML
    private CheckBox warnExitingOnCloseCheckBox;

    @FXML
    private Button okButton;

    @FXML
    private Button cancelButton;

    private Stage dialogStage;
    private boolean okClicked = false;
    private String languagePreference;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {

        splashScreenCheckBox.setSelected(Pref.isSplashScreen());
        errorLoadingDataFromFilePopUpAtStartOrOnOpenCheckBox.setSelected(Pref.isErrorLoadingFilePopUpAtStartOrOnOpen());
        decryptConnFilePassAtStartOrOnOpenCheckBox.setSelected(Pref.isDecryptFilePassPopUpAtStartOrOnOpen());

        initializeLanguage();

        warnRemoveEmptyFileOnCloseCheckBox.setSelected(Pref.isWarnRemoveEmptyFileOnClose());
        warnExitingOnCloseCheckBox.setSelected(Pref.isWarnExitingOnClose());

        // I18N of Controller

        // Start
        startLabel.textProperty().bind(I18N.createStringBinding("prefDialog.start"));

        splashScreenLabel.textProperty().bind(I18N.createStringBinding("prefDialog.splashScreen"));
        errorLoadingDataFromFilePopUpAtStartOrOnOpenLabel.textProperty().bind(I18N.createStringBinding("prefDialog.errorLoadingDataFromFilePopUpAtStartOrOnOpen"));
        decryptConnFilePassAtStartOrOnOpenLabel.textProperty().bind(I18N.createStringBinding("prefDialog.decryptConnFilePassAtStartOrOnOpen"));

        // Misc
        miscLabel.textProperty().bind(I18N.createStringBinding("prefDialog.misc"));

        languageLabel.textProperty().bind(I18N.createStringBinding("prefDialog.language"));

        // Close
        closeLabel.textProperty().bind(I18N.createStringBinding("prefDialog.close"));

        warnRemoveEmptyFileOnCloseLabel.textProperty().bind(I18N.createStringBinding("prefDialog.warnRemoveEmptyFileOnClose"));
        warnExitingOnCloseLabel.textProperty().bind(I18N.createStringBinding("prefDialog.warnExitingOnClose"));

        // Buttons
        okButton.textProperty().bind(I18N.createStringBinding("button.ok"));
        cancelButton.textProperty().bind(I18N.createStringBinding("button.cancel"));

    }

    /**
     * Initialize Language ComboBox.
     */
    private void initializeLanguage() {

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

        languagePreference = Pref.getLanguage();

        languageComboBox.setItems(languageOptions);
        languageComboBox.getSelectionModel().select(new Locale(languagePreference));
        miscGridPane.add(languageComboBox, 1, 0);

        languageComboBox.getSelectionModel().selectedItemProperty().addListener(nv -> {
            String languageSelected = languageComboBox.getSelectionModel().getSelectedItem().toString();
            if (!languageSelected.equals(Pref.getLanguage())) {
                Pref.setLanguage(languageSelected);
                I18N.setLocale(new Locale(languageSelected));
            }
        });

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

        Pref.setWarnRemoveEmptyFileOnClose(warnRemoveEmptyFileOnCloseCheckBox.isSelected());
        Pref.setWarnExitingOnClose(warnExitingOnCloseCheckBox.isSelected());

        dialogStage.close();
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {

        String languageSelected = languageComboBox.getSelectionModel().getSelectedItem().toString();
        if (!languageSelected.equals(languagePreference)) {
            Pref.setLanguage(languagePreference);
            I18N.setLocale(new Locale(languagePreference));
        }

        dialogStage.close();

    }

}
