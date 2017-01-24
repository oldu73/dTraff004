package ch.stageconcept.dtraff.preference.view;

import ch.stageconcept.dtraff.preference.model.Pref;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

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
