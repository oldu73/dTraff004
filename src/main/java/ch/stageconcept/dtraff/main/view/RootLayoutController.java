package ch.stageconcept.dtraff.main.view;

import ch.stageconcept.dtraff.connection.model.*;
import ch.stageconcept.dtraff.connection.util.*;
import ch.stageconcept.dtraff.connection.view.ModelTree;
import ch.stageconcept.dtraff.main.MainApp;
import ch.stageconcept.dtraff.preference.model.Pref;
import ch.stageconcept.dtraff.preference.util.PrefEditor;
import ch.stageconcept.dtraff.util.AlertDialog;
import ch.stageconcept.dtraff.util.I18N;
import ch.stageconcept.dtraff.util.Is1st;
import ch.stageconcept.dtraff.xrelease.Release;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

//TODO refactor (clean) doc

/**
 * The controller for the root layout. The root layout provides the basic
 * application layout containing a menu bar and space where other JavaFX
 * elements can be placed.
 *
 * @author Olivier Durand
 */
public class RootLayoutController {

    // Attributes
    // #####################################################################

    // ConnRoot treeView CSS resource
    // !WARNING! In order to use file that reside in resources folder, don’t forget to add a slash before file name!
    private static final String CONNROOT_TREEVIEW_CSS = "/connRootTreeView.css";

    // Alerts statics texts
    private static final String ALINF_ABOUT_TITLE = "Data Traffic";
    public static final String ALINF_ABOUT_HEADER = ALINF_ABOUT_TITLE + Release.NUMBER.getValue();

    // Deprecated!
    private static final String ALERR_LOAD_DATA_TITLE = "Deprecated! " + "Error";
    private static final String ALERR_LOAD_DATA_HEADER = "Deprecated! " + "Could not load data";
    private static final String ALERR_LOAD_DATA_CONTENT = "Deprecated! " + "Could not load data from file:\n";

    // Deprecated!
    private static final String ALCNF_BAD_PASSWORD_TITLE = "Deprecated! " + "Enter Password";
    private static final String ALCNF_BAD_PASSWORD_HEADER = "Deprecated! " + "Bad password!";
    private static final String ALCNF_BAD_PASSWORD_CONTENT = "Deprecated! " + "Try again?";

    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private Label initializingLabel;

    @FXML
    private Menu fileMenu;

    @FXML
    private MenuItem fileNewMenuItem;

    @FXML
    private MenuItem fileOpenMenuItem;

    @FXML
    private Menu passwordMenu;

    @FXML
    private MenuItem setPasswordMenuItem;

    @FXML
    private MenuItem enterPasswordMenuItem;

    @FXML
    private MenuItem changePasswordMenuItem;

    @FXML
    private MenuItem removePasswordMenuItem;

    @FXML
    private MenuItem fileRepairMenuItem;

    @FXML
    private MenuItem fileCloseMenuItem;

    @FXML
    private Menu serverConnectionMenu;

    @FXML
    private MenuItem newServerConnectionMenuItem;

    @FXML
    private MenuItem editServerConnectionMenuItem;

    @FXML
    private MenuItem fileExitMenuItem;

    @FXML
    private Menu editMenu;

    @FXML
    private MenuItem editDeleteMenuItem;

    @FXML
    private MenuItem editPreferencesMenuItem;

    @FXML
    private Menu helpMenu;

    @FXML
    private MenuItem helpAboutMenuItem;

    private ConnRoot connRoot;    // ConnRoot description to be used in a treeView : ConnRoot (root node) - ConnFile - Conn - Database - (...)
    private ModelTree<ConnUnit<?>> connTree;
    private TreeView<ConnUnit<?>> connTreeView;
    private Preferences preferences = Preferences.userRoot().node(ConnRoot.PREFS_PATH);  // User preferences
    private ObjectProperty<ConnFileState> selectedConnFileState = new SimpleObjectProperty<>();
    private int passwordMenuIndex;

    // Functional interface implementations
    // #####################################################################

    /**
     * Manage possible empty clear/decrypted files,
     * or exit confirmation,
     * and close application (or not).
     *
     * SRC: http://stackoverflow.com/questions/31540500/alert-box-for-when-user-attempts-to-close-application-using-setoncloserequest-in
     */
    private EventHandler<WindowEvent> confirmCloseEventHandler = event -> {

        // If connRoot has empty clear/decrypted file and user pref is set then,
        // popup an alert message to inform user about empty clear/decrypted files.
        // If Ok, remove empty clear/decrypted file entries in user preferences then exit
        // else, return back to application.

        // On application crash, acceptable behavior is that on next start empty files will appear as broken
        // and should be manually removed (close/delete menus).

        if (connRoot.hasEmptyAndIsPref().get()) {

            if (!connRoot.alertEmptyFiles()) {
                event.consume();    // Consume window close request event
                return;             // and get back to application!
            }

        } else if (Pref.isWarnExitingOnClose()) {

            // ### Exit confirmation

            Alert alert = AlertDialog.provide(MainApp.PRIMARY_STAGE,
                    Alert.AlertType.CONFIRMATION,
                    MainApp.TEXT_BUNDLE.getString("alcnfExit.title"),
                    MainApp.TEXT_BUNDLE.getString("alcnfExit.header"),
                    MainApp.TEXT_BUNDLE.getString("alcnfExit.content"), false);

            Optional<ButtonType> result = alert.showAndWait();

            if (!(result.get() == ButtonType.OK)) {

                event.consume();    // Consume window close request event
                return;             // and get back to application!

            }

        }

        // Event not consumed (by conditions) before and not returned back to application,
        // so here we are at the end of window closing process

        // In any case, remove empty clear/decrypted file(s)
        connRoot.removeEmptyFiles();

        // Exit!

    };

    // Getters and Setters
    // #####################################################################

    public BorderPane getRootBorderPane() {
        return rootBorderPane;
    }

    public Label getInitializingLabel() {
        return initializingLabel;
    }

    public TreeView<ConnUnit<?>> getConnTreeView() {
        return connTreeView;
    }

    public EventHandler<WindowEvent> getConfirmCloseEventHandler() {
        return confirmCloseEventHandler;
    }

    // Node can only be displayed once in Scene Graph.
    // So, when context menu show on a ConnFile instance,
    // remove and return passwordMenu to ConnFile instance...
    public Menu getPasswordMenu() {
        passwordMenuIndex = fileMenu.getItems().indexOf(passwordMenu) != -1 ? fileMenu.getItems().indexOf(passwordMenu) : passwordMenuIndex;

        if (passwordMenuIndex != -1) fileMenu.getItems().remove(passwordMenu);
        return passwordMenu;
    }

    // ...And, when context menu hide on a ConnFile instance,
    // remove it on a ConnFile instance and set passwordMenu back to controller
    public void setPasswordMenu(Menu passwordMenu) {
        this.passwordMenu = passwordMenu;
        fileMenu.getItems().add(passwordMenuIndex, passwordMenu);
    }

    // Methods
    // #####################################################################

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {

        // I18N String bindings.

        // File

        fileMenu.textProperty().bind(I18N.createStringBinding("fileMenu"));
        fileNewMenuItem.textProperty().bind(I18N.createStringBinding("fileMenuItem.new"));
        fileOpenMenuItem.textProperty().bind(I18N.createStringBinding("fileMenuItem.open"));
        fileRepairMenuItem.textProperty().bind(I18N.createStringBinding("fileMenuItem.repair"));
        fileCloseMenuItem.textProperty().bind(I18N.createStringBinding("fileMenuItem.close"));

        passwordMenu.textProperty().bind(I18N.createStringBinding("passwordMenu"));
        setPasswordMenuItem.textProperty().bind(I18N.createStringBinding("passwordMenuItem.set"));
        enterPasswordMenuItem.textProperty().bind(I18N.createStringBinding("passwordMenuItem.enter"));
        changePasswordMenuItem.textProperty().bind(I18N.createStringBinding("passwordMenuItem.change"));
        removePasswordMenuItem.textProperty().bind(I18N.createStringBinding("passwordMenuItem.remove"));

        serverConnectionMenu.textProperty().bind(I18N.createStringBinding("serverConnectionMenu"));
        newServerConnectionMenuItem.textProperty().bind(I18N.createStringBinding("serverConnectionMenuItem.new"));
        editServerConnectionMenuItem.textProperty().bind(I18N.createStringBinding("serverConnectionMenuItem.edit"));

        fileExitMenuItem.textProperty().bind(I18N.createStringBinding("fileMenuItem.exit"));

        // Edit

        editMenu.textProperty().bind(I18N.createStringBinding("editMenu"));
        editDeleteMenuItem.textProperty().bind(I18N.createStringBinding("editMenuItem.delete"));
        editPreferencesMenuItem.textProperty().bind(I18N.createStringBinding("editMenuItem.preferences"));

        // Help

        helpMenu.textProperty().bind(I18N.createStringBinding("helpMenu"));
        helpAboutMenuItem.textProperty().bind(I18N.createStringBinding("helpMenuItem.about"));

    }

    /**
     * Initialization called from outside.
     *
     * Implemented in order to wait until Main application
     * window appear before launching initialization process.
     * The goal to reach is, about ConnFile objects, to warn user about broken one
     * or ask user password for encrypted one after that Main window is displayed.
     */
    public void subInitialize() {

        connRoot = new ConnRoot(MainApp.TEXT_BUNDLE.getString("network.network"), MainApp.PRIMARY_STAGE, this);
        connRoot.nameProperty().bind(I18N.createStringBinding("network.network"));

        boolean initializingLabelAnimation = connRoot.isUserActionNeededAtStart();

        if (initializingLabelAnimation) initializingLabelTextAnimation();
        else rootBorderPane.getChildren().remove(initializingLabel);

        // If connRoot has broken file and user pref is set then,
        // popup an alert message to inform user about broken files.
        Is1st.do2nd(connRoot.hasBrokenAndIsPref(), connRoot::alertLoadFiles);

        // If connRoot has encrypted file and user pref is set then,
        // ask user password for encrypted file(s) through popup(s).
        Is1st.do2nd(connRoot.hasEncryptedAndIsPref(), connRoot::decryptPasswords);

        anteInitialize();

        if (initializingLabelAnimation) {
            // Fade out Initialization label (main window background).
            FadeTransition fadeOut = initializingLabelFadeOut();
            fadeOut.playFromStart();

            // When fade out finished, remove label and continue initialization process.
            fadeOut.setOnFinished((ActionEvent event) -> {
                rootBorderPane.getChildren().remove(initializingLabel);
                postInitialize();
            });
        } else {
            postInitialize();
        }

    }

    /**
     * Initializing Label Text Animation.
     * SRC: http://stackoverflow.com/questions/33646317/typing-animation-on-a-text-with-javafx
     *
     * Initializing
     * Initializing.
     * Initializing..
     * Initializing...
     */
    private void initializingLabelTextAnimation() {

        String initializingString = MainApp.TEXT_BUNDLE.getString("initializingLabel");
        int indexOfDot = initializingString.indexOf('.');

        final IntegerProperty i = new SimpleIntegerProperty(indexOfDot);
        Timeline timeline = new Timeline();

        KeyFrame keyFrame = new KeyFrame(
                Duration.seconds(0.4),
                event -> {
                    if (i.get() > initializingString.length()) {
                        i.set(indexOfDot);
                        timeline.playFromStart();
                    } else {
                        initializingLabel.setText(initializingString.substring(0, i.get()));
                        i.set(i.get() + 1);
                    }
                });

        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

    }

    /**
     * Initializing Label Fade Out Transition.
     *
     * @return FadeTransition instance
     * on initializingLabel node.
     */
    private FadeTransition initializingLabelFadeOut() {

        // Fade out Initialization label (main window background).
        FadeTransition fadeOut = new FadeTransition(Duration.millis(1000));
        fadeOut.setNode(initializingLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setCycleCount(1);
        fadeOut.setAutoReverse(false);

        return fadeOut;

    }

    /**
     * Ante initialize
     *
     * Eventually with initialization label on main
     * window background displayed (and animated).
     */
    private void anteInitialize() {

        connRoot.populateSubUnits();

        connTree = new ModelTree<>(connRoot,
                ConnUnit::getSubUnits,
                ConnUnit::nameProperty,
                ConnUnit::iconProperty,
                ConnUnit::menuProperty,
                unit -> PseudoClass.getPseudoClass(unit.getClass().getSimpleName().toLowerCase()));

        //TODO on close/delete connRoot subUnits.. maintain treeView and related ObservableList synchronized.

        connTreeView = connTree.getTreeView();

        //TODO check if sorting processes refactor is needed.
        connRoot.sortSubUnitsOnChangeListener();
        // Initial sort
        connRoot.sortSubUnits();

        // CSS pseudo class treeView style.
        connTreeView.getStylesheets().add(getClass().getResource(CONNROOT_TREEVIEW_CSS).toExternalForm());

        connTreeView.getRoot().setExpanded(true);

    }

    /**
     * Post initialize
     *
     * After initialization label on main
     * window background disappear (fade out).
     */
    private void postInitialize() {

        // Set connTreeView on the left part of the rootBorderPane
        rootBorderPane.setLeft(connTreeView);

        // Handle double click behavior on a broken or encrypted ConnFile instance
        connTreeView.setOnMouseClicked(connTreeViewOnMouseClicked());

        // ### Tool bar menu ###
        // #####################

        // ### Enable/Disable
        menusDisable();

        // ### Action
        menusAction();

        // #####################

    }

    /**
     * Action on a conTreeView ConnFile instance double click :
     * - if broken -> file chooser
     * - if encrypted -> try to decrypt and on succeeded -> populate subUnit
     *
     * @return MouseEvent Handler
     */
    private EventHandler<? super MouseEvent> connTreeViewOnMouseClicked() {
        return event -> {
            if (event.getClickCount() == 2 &&   // double click
                    !event.getTarget().getClass().equals(Group.class) &&    // not on expand/collapse icon
                    connTreeView.getSelectionModel().getSelectedItem().getValue() instanceof ConnFile)  // and selected is a ConnFile instance
            {
                ConnFile connFile = (ConnFile) connTreeView.getSelectionModel().getSelectedItem().getValue();

                if (connFile.isBroken()) openBrokenConnFile(connFile);
                else if (connFile.isEncrypted()) connRoot.decryptPassword(connFile).populateSubUnit(connFile);
            }
        };
    }

    /**
     * Main menus disable process.
     */
    private void menusDisable() {

        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // !!WARNING!! Steps 1 to 4 process, don't miss one (at least, check if necessary)!!
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        // 1. initial

        // ### File - Password - Remove: Disable if the ConnRoot treeView
        // selected item is not an encrypted, empty decrypted or decrypted ConnFile object (like change password above)
        //removePasswordMenuItem.disableProperty().bind(changePasswordMenuItem.disableProperty());

        // Some File - menus disable property initial state
        //TODO refactor with a MenuItem list (or something alike) which will also be used a few line below (same treatment -> so, factorize!)
        setMenusDisable(true,
                passwordMenu,
                enterPasswordMenuItem,
                setPasswordMenuItem,
                changePasswordMenuItem,
                removePasswordMenuItem,
                newServerConnectionMenuItem,
                fileRepairMenuItem);

        // Some File - menus disable property setting if the ConnRoot treeView
        // selected item is not a ConnFile object and other menu specific related conditions
        connTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            try {

                if (newValue.getValue() instanceof ConnFile) {

                    // 2. listener

                    ConnFile selectedConnFile = (ConnFile) newValue.getValue();

                    boolean isBroken = selectedConnFile.isBroken();
                    boolean isEmptyClear = selectedConnFile.isEmptyClear();
                    boolean isClear = selectedConnFile.isClear();
                    boolean isEncrypted = selectedConnFile.isEncrypted();
                    boolean isEmptyDecrypted = selectedConnFile.isEmptyDecrypted();
                    boolean isDecrypted = selectedConnFile.isDecrypted();

                    passwordMenu.disableProperty().bind(selectedConnFile.menuPasswordDisabledProperty());
                    enterPasswordMenuItem.disableProperty().bind(selectedConnFile.menuEnterPasswordDisabledProperty());
                    setPasswordMenuItem.disableProperty().bind(selectedConnFile.menuSetPasswordDisabledProperty());
                    changePasswordMenuItem.disableProperty().bind(selectedConnFile.menuChangePasswordDisabledProperty());
                    removePasswordMenuItem.disableProperty().bind(changePasswordMenuItem.disableProperty());

                    fileRepairMenuItem.disableProperty().bind(selectedConnFile.menuFileRepairDisabledProperty());

                    // Following line is related to double click action that may
                    // change selected ConnFile state. Therefor menus disable property
                    // should follow directly (as soon as state change) even if connTreeView
                    // selected item doesn't change.
                    selectedConnFileState.bind(selectedConnFile.stateProperty());

                    // ### File - Repair: Disable if the ConnRoot treeView
                    // selected item is not a broken ConnFile object
                    //fileRepairMenuItem.setDisable(!selectedConnFile.isBroken());

                    // ### File - Password - Enter: Disable if the ConnRoot treeView
                    // selected item is not an encrypted ConnFile object
                    //enterPasswordMenuItem.setDisable(!selectedConnFile.isEncrypted());

                    // ### File - Password - Set: Disable if the ConnRoot treeView
                    // selected item is not a clear or empty clear ConnFile object
                    //setPasswordMenuItem.setDisable(selectedConnFile.isMenuSetPasswordDisabled());
                    //TODO refactor with boolean bindings, see below, to avoid the actual menu enable/disable 4 steps process (initial, listener, reset, state change listener)
                    //setPasswordMenuItem.disableProperty().bind(selectedConnFile.menuSetPasswordDisabledProperty());

                    // ### File - Password - Change: Disable if the ConnRoot treeView
                    // selected item is not an encrypted, empty decrypted or decrypted ConnFile object
                    //changePasswordMenuItem.setDisable(!(selectedConnFile.isEncrypted() || selectedConnFile.isEmptyDecrypted() || selectedConnFile.isDecrypted()));

                    // ### File - Server Connection - New: Disable if the ConnRoot treeView
                    // selected item is not a clear or decrypted ConnFile object
                    newServerConnectionMenuItem.setDisable(!(selectedConnFile.isClear() || selectedConnFile.isDecrypted()));

                } else

                    // 3. reset

                /*
                    setMenusDisable(true,
                            enterPasswordMenuItem,
                            setPasswordMenuItem,
                            changePasswordMenuItem,
                            newServerConnectionMenuItem,
                            fileRepairMenuItem);    // connTreeView selected item is NOT a ConnFile instance
                */

                setMenusDisable(true,
                        newServerConnectionMenuItem);

            } catch (NullPointerException ex) {
                // ...
            }

        });

        // 4. state change listener

        // Double click action completion generate a state change on a treeView ConnFile instance.
        // As an example, after double click on an encrypted file to enter correct password,
        // the serverConnectionMenu remain disabled until treeView selection changed!
        // So, the bellowing lines deserve to track treeView ConnFile
        // selected object state changes in order to update related MenuItem disabled status.
        selectedConnFileState.addListener((observable, oldValue, newValue) -> {

            if (oldValue != null && (oldValue.equals(ConnFileState.ENCRYPTED) && newValue.equals(ConnFileState.DECRYPTED))) {
                newServerConnectionMenuItem.setDisable(false);
                //enterPasswordMenuItem.setDisable(true);
            }

            if (oldValue != null && (oldValue.equals(ConnFileState.BROKEN) && !newValue.equals(ConnFileState.BROKEN))) {
                //fileRepairMenuItem.setDisable(true);
            }

            // After broken ConnFile repair failed tentative
            if (oldValue != null && (oldValue.equals(ConnFileState.CLEAR) && newValue.equals(ConnFileState.BROKEN))) {
                //fileRepairMenuItem.setDisable(false);
            }

            if (oldValue != null && ((oldValue.equals(ConnFileState.EMPTY_CLEAR) || oldValue.equals(ConnFileState.CLEAR)) &&
                    (newValue.equals(ConnFileState.EMPTY_DECRYPTED) || newValue.equals(ConnFileState.DECRYPTED)))) {
                //setPasswordMenuItem.setDisable(true);
            }

            if (oldValue != null && ((oldValue.equals(ConnFileState.ENCRYPTED) || oldValue.equals(ConnFileState.EMPTY_DECRYPTED) || oldValue.equals(ConnFileState.DECRYPTED)) &&
                    (newValue.equals(ConnFileState.EMPTY_CLEAR) || newValue.equals(ConnFileState.CLEAR)))) {
                //enterPasswordMenuItem.setDisable(true);
                //setPasswordMenuItem.setDisable(false);
                //changePasswordMenuItem.setDisable(true);
            }

            if (oldValue != null && ((oldValue.equals(ConnFileState.CLEAR) || oldValue.equals(ConnFileState.EMPTY_CLEAR)) &&
                    (newValue.equals(ConnFileState.ENCRYPTED) || newValue.equals(ConnFileState.EMPTY_DECRYPTED) || newValue.equals(ConnFileState.DECRYPTED)))) {
                //setPasswordMenuItem.setDisable(true);
                //changePasswordMenuItem.setDisable(false);
            }

        });

        // Disable tool bar menu File - Server Connection - Edit if no item or not a Conn object instance are selected in ConnRoot treeView
        editServerConnectionMenuItem.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        connTreeView.getSelectionModel().getSelectedItem() == null ||
                                !(connTreeView.getSelectionModel().getSelectedItem().getValue() instanceof Conn),
                connTreeView.getSelectionModel().selectedItemProperty()));

        // Disable tool bar menu File - Server Connection if File - Server Connection - New and Edit are disabled
        serverConnectionMenu.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        newServerConnectionMenuItem.isDisable() && editServerConnectionMenuItem.isDisable(),
                newServerConnectionMenuItem.disableProperty(),
                editServerConnectionMenuItem.disableProperty()));

    }

    /**
     * Set disable property of each menu in list
     * (JavaFX MenuItem variable length argument lists),
     * to disable argument value (true/false).
     *
     * SRC, varargs(variable length argument lists):
     * http://www.deitel.com/articles/java_tutorials/20060106/VariableLengthArgumentLists.html
     *
     * @param disable
     * @param menus
     */
    private void setMenusDisable(boolean disable, MenuItem... menus) {
        for (MenuItem menu : menus) menu.setDisable(disable);
    }

    /**
     * Main menus action process.
     */
    private void menusAction() {

        // Action when the user selects the tool bar File - New menu.
        fileNewMenuItem.setOnAction(connRoot.newConnFile());

    }

    /**
     * Called when the user selects the tool bar File - Open menu.
     */
    @FXML
    private void handleFileOpen() {

        openConnFile();

    }

    /**
     * Called when the user selects the tool bar File - Repair menu.
     */
    @FXML
    private void handleFileRepair() {

        ConnFile connFile = getSelectedConnFile();
        if (connFile != null && connFile.isBroken()) openBrokenConnFile(connFile);

    }

    /**
     * Open ConnFile object.
     */
    public void openConnFile() {

        File file = getXmlFile();

        if (file != null) {
            // name without extension
            String name = file.getName().substring(0, file.getName().indexOf("."));
            // file name (path)
            String fileName = file.getAbsolutePath();

            ConnFile connFile = getConnFile(name);

            if (connFile != null) alertAlreadyPresent(connFile);
            else {
                // Create new ConnRoot treeView entry (ConnFile instance)
                connRoot.createSubUnit(name, fileName, this);

                //treatSubUnit(connFile, true);
            }
        }
    }

    /**
     * Open broken state ConnFile object (file)
     * @param connFile
     */
    public void openBrokenConnFile(ConnFile connFile) {
        if (connFile != null && connFile.isBroken()) {

            File file = getXmlFile();

            if (file != null) {

                // ### Process ###

                // Chosen file differ from connFile and is already present (whatever the state)
                // -> Alert already present and nothing else!

                // Chosen file differ from connFile and is not present
                // connFile update name, fileName (path), state
                // -> Open

                // Chosen file name == connFile.name
                // connFile update fileName (path), state
                // -> Open

                // ###############

                // ### Implementation ###

                // name without extension
                String name = file.getName().substring(0, file.getName().indexOf("."));
                // file name (path)
                String fileName = file.getAbsolutePath();

                if (!name.equals(connFile.getName()) && (getConnFile(name) != null)) alertAlreadyPresent(getConnFile(name));
                else {
                    // update and open (treat..)
                    preferences.remove(connFile.getName());
                    connFile.setName(name);
                    connFile.setFileName(fileName);
                    // Reset state to default
                    connFile.setClear();

                    connRoot.treatSubUnit(connFile);

                    //treatSubUnit(connFile, true);
                }

                // ConnRoot treeView entry modification in place, so no list change triggered
                // therefore the sort method should be called "manually".
                connRoot.sortSubUnits();

                // ######################

            }
        }
    }

    /**
     * Open file chooser on primary stage with xml file type filter
     * @return File or null
     */
    private File getXmlFile() {

        // Because of UI frozen behavior (depends of machine) due to file chooser,
        // we manage to inform user of running process (opening file) through treeView
        // root item icon and designation.

        // ### Before file chooser

        // Put connRoot in OPENING_FILE state
        connRoot.setOpeningFile();
        // If root is selected -> deselect (for visual reason)
        boolean rootIsSelected = connTreeView.getSelectionModel().getSelectedIndex() == 0;
        if (rootIsSelected) connTreeView.getSelectionModel().clearSelection();
        // and finally, disable treeView (before opening file chooser)
        connTreeView.setDisable(true);

        // ### File chooser

        FileChooser fileChooser = new FileChooser();
        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show open file dialog
        File file = fileChooser.showOpenDialog(MainApp.PRIMARY_STAGE);

        // ### After file chooser

        // Once finished with file chooser, if root was selected in treeView, select it again
        if (rootIsSelected) connTreeView.getSelectionModel().selectFirst();
        // Re-enable treeView
        connTreeView.setDisable(false);
        // Put treeView in QUITE state
        connRoot.setQuite();

        return file;
    }

    /**
     * Alert ConnFile already present in treeView.
     * @param connFile
     */
    private void alertAlreadyPresent(ConnFile connFile) {

        AlertDialog.provide(MainApp.PRIMARY_STAGE,
                Alert.AlertType.INFORMATION,
                MainApp.TEXT_BUNDLE.getString("alinfFileAlreadyPresent.title"),
                MainApp.TEXT_BUNDLE.getString("alinfFileAlreadyPresent.header"),
                MainApp.TEXT_BUNDLE.getString("alinfFileAlreadyPresent.content")
                        + "\n"
                        + ConnRoot.getNameFileNameToString().apply(connFile),
                true);

    }

    /**
     * Called when the user selects the tool bar File - Password - Set menu.
     */
    @FXML
    private void handleSetPassword() {
        connRoot.setPassword(getSelectedConnFile());
    }

    /**
     * Called when the user selects the tool bar File - Password - Enter menu.
     */
    @FXML
    private void handleEnterPassword() {
        // The tool bar menu is disabled if none or not an encrypted ConnFile is selected in Connection treeView.
        ConnFile connFile = getSelectedConnFile();

        connRoot.decryptPassword(connFile).populateSubUnit(connFile);

        // Refactored (below replaced by above).

        /*
        if (connFile != null && decryptConnFile(connFile)) {
            populateSubunit(connFile, loadConnDataFromConnFile(connFile));
        }
        */

    }

    /**
     * Called when the user selects the tool bar File - Password - Change menu.
     */
    @FXML
    private void handleChangePassword() {
        connRoot.changePassword(getSelectedConnFile());
    }

    /**
     * Called when the user selects the tool bar File - Password - Remove menu.
     */
    @FXML
    private void handleRemovePassword() {
        connRoot.removePassword(getSelectedConnFile());
    }

    //TODO File Save (nice to have)
    // Put ConnFile object state (also icon) in "Dirt" mode.
    // For now saving process is automatic.
    // If file save menu functionality is implemented,
    // keep automatic saving possibility through user preferences.

    /**
     * Called when the user selects the tool bar File - Close menu.
     */
    @FXML
    private void handleFileClose() {
        System.out.println("Close File..");
    }

    /**
     * Called when the user selects the tool bar File - Server Connection - New, menu.
     * Opens a dialog to edit details for a new connection.
     */
    @FXML
    private void handleNewConnection() {
        // The tool bar menu is disabled if none or not a decrypted ConnFile is selected in Connection treeView.
        if (getSelectedConnFile() != null) getSelectedConnFile().newConn();

        //TODO Find a solution for the ConnEditDialogController Test Conn button side effect that update the edited conn:
        //- pass a temporary copy of the edited conn for testing.
    }

    /**
     * Called when the user selects the tool bar File - Server Connection - Edit, menu.
     * Opens a dialog to edit details for the existing connection.
     */
    @FXML
    private void handleEditConnection() {
        // The tool bar menu is disabled if none or not a Conn is selected in Connection treeView,
        // so the item could only be a Conn -> (cast)
        Conn conn = (Conn) connTreeView.getSelectionModel().getSelectedItem().getValue();
        conn.editConnection();

        //TODO Find a solution for the ConnEditDialogController Test Conn button side effect that update the edited conn:
        //- pass a temporary copy of the edited conn for testing.
    }

    /**
     * Called when the user selects the tool bar Edit - Delete menu.
     */
    @FXML
    private void handleEditDelete() {
        System.out.println("Edit Delete..");
    }

    /**
     * Called when the user selects the tool bar Edit - Preferences menu.
     */
    @FXML
    private void handleEditPreferences() {
        PrefEditor.INSTANCE.supply();
    }

    /**
     * Opens an about dialog.
     */
    @FXML
    private void handleAbout() {

        AlertDialog.provide(MainApp.PRIMARY_STAGE,
                Alert.AlertType.INFORMATION,
                ALINF_ABOUT_TITLE,
                ALINF_ABOUT_HEADER,
                MainApp.TEXT_BUNDLE.getString("software.author")
                        + "\n"
                        + MainApp.TEXT_BUNDLE.getString("software.companyWebSite"),
                true);

    }

    /**
     * Fire close window event request
     */
    @FXML
    public void handleExit() {

        MainApp.PRIMARY_STAGE.fireEvent(
                new WindowEvent(
                        MainApp.PRIMARY_STAGE,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                )
        );

    }

    /**
     * Get ConnRoot treeView selected ConnFile object
     *
     * @return If selected item is an instance of ConnFile, return this object,
     * null otherwise
     */
    private ConnFile getSelectedConnFile() {

        try {
            Object selectedObject = connTreeView.getSelectionModel().getSelectedItem().getValue();

            if (selectedObject instanceof ConnFile) {
                return (ConnFile) selectedObject;
            }
        } catch (NullPointerException e) {
            return null;
        }

        return null;
    }

    /**
     * Check if a ConnFile object with given String name parameter
     * is present in ConnRoot treeView.
     *
     * @param name
     * @return ConnFile object if one with name attribute exist in ConnRoot treeView,
     * null otherwise.
     */
    public ConnFile getConnFile(String name) {
        // SRC: http://stackoverflow.com/questions/23407014/return-from-lambda-foreach-in-java
        return connRoot
                .getSubUnits()
                .stream()
                .filter(connFile -> connFile.getName().contains(name))
                .findFirst().orElse(null);
    }

    /**
     * Create connRoot description in a tree data structure,
     * to be used in a treeView : ConnRoot (root node) - ConnFile - Conn - Database - (...)
     */
    private ConnRoot createNetwork() {

        ConnRoot connRoot = new ConnRoot(ConnRoot.NETWORK);
        String[] prefKeys = null;

        connRoot.setRootLayoutController(this);

       // ### 1. Get preference keys.
       if (preferences != null) {
           try {
               prefKeys = preferences.keys();
           } catch (BackingStoreException e) {
               //System.err.println("createNetwork() method, unable to read backing store: " + e);
               //e.printStackTrace();
           } catch (IllegalStateException e) {
               //System.err.println("createNetwork() method, " + e);
               //System.out.println("createNetwork() method, node has been removed!");
           }
       }
       // #########################################################################

       // ### 2. Iterate through preference keys to create ConnFile object
       // and check if corresponding file exist.
       if (prefKeys != null) {

           for (String prefKey : prefKeys) {
               ConnFile connFile = new ConnFile(prefKey);

               String fileName = preferences.get(prefKey, null);

               if (fileName != null) {
                   connFile.setFileName(fileName);

                   File file = new File(fileName);

                   if(!file.exists() || file.isDirectory()) {
                       setBrokenAndAlertLoadData(connFile, file);
                   }

                   connFile.setParent(connRoot);
                   connFile.setRootLayoutController(this);
                   connRoot.getSubUnits().add(connFile);
               }
           }

       }
       // #########################################################################

       // ### 3. Iterate through connRoot subunits (ConnFile objects) to treat and populate
       // Conn objects list.
       if (!connRoot.getSubUnits().isEmpty()) connRoot.getSubUnits().forEach((subUnit) -> {
           if (!subUnit.isBroken()) treatSubUnit(subUnit, false);
       });
       // #########################################################################

       // Some sample data, debug mode
       //buildSampleData(connRoot);

       return connRoot;
    }

    /**
     * ConnFile subUnit ante populate treatment.
     *
     *  If ConnFile object is encrypted ->
     *  ask for password (if atStart/onOpen user preference is set
     *  otherwise let ConnFile object in an encrypted state)
     *  If not password protected or password OK, populate
     *  ConnFile object subunits with Conn objects.
     *
     * @param subUnit
     * @param updatePreference
     */
    private void treatSubUnit(ConnFile subUnit, boolean updatePreference) {
        List<Conn> listConn = loadConnDataFromConnFile(subUnit);

        // If loadConnDataFromConnFile raise an exception (file is damaged)
        // it set ConnFile object state to BROKEN and return null value in listConn.
        if (listConn != null) {

            // debug mode
            //System.out.println("String to decrypt: " + listConn.get(0).getPassword());

            // If first element (Conn) of the list is encrypted, also all others are (with same password)
            if (listConn.get(0).isPasswordEncrypted()) {
                subUnit.setEncrypted();
                if (Pref.isDecryptFilePassPopUpAtStartOrOnOpen()) decryptConnFile(subUnit);
            }

            // debug mode
                        /*
                        for (Conn conn: listConn) {
                            System.out.println(conn);
                        }
                        */

            if (subUnit.isClear() || subUnit.isDecrypted()) {
                populateSubunit(subUnit, listConn);
            }
        }

        // update preference
        if (updatePreference) {
            // debug mode
            //System.out.println("\nbefore:\n\n" + preferencesToString());

            preferences.put(subUnit.getName(), subUnit.getFileName());

            // debug mode
            //System.out.println("\nafter:\n\n" + preferencesToString());
        }
    }

    /**
     * Populate subUnit (ConnFile object)
     *
     * @param subUnit
     * @param listConn
     */
    public void populateSubunit(ConnFile subUnit, List<Conn> listConn) {

        // Set Conn object reference to his ConnFile parent object
        for (Conn conn : listConn) {
            conn.setParent(subUnit);
        }

        subUnit.getSubUnits().addAll(listConn);
    }

    /**
     * Populate tree with sample data,
     * for testing purpose.
     *
     * @param connRoot
     */
    private void buildSampleData(ConnRoot connRoot) {
        // Some sample data, debug mode
        ConnFile file1 = new ConnFile("file1");
        ConnFile file2 = new ConnFile("file2");
        ConnFile file3 = new ConnFile("file3");

        Conn connection1 = new Conn("connection1");
        Conn connection2 = new Conn("connection2");
        Conn connection3 = new Conn("connection3");
        Conn connection4 = new Conn("connection4");
        Conn connection5 = new Conn("connection5");

        DataBase dataBase1 = new DataBase("dataBase1");
        DataBase dataBase2 = new DataBase("dataBase2");
        DataBase dataBase3 = new DataBase("dataBase3");
        DataBase dataBase4 = new DataBase("dataBase4");
        DataBase dataBase5 = new DataBase("dataBase5");

        connection1.getSubUnits().add(dataBase1);

        connection2.getSubUnits().addAll(dataBase2, dataBase3);
        connection3.getSubUnits().addAll(dataBase2, dataBase3);

        connection4.getSubUnits().addAll(dataBase4, dataBase5);
        connection5.getSubUnits().addAll(dataBase4, dataBase5);

        file1.getSubUnits().add(connection1);
        file2.getSubUnits().addAll(connection2, connection3);
        file3.getSubUnits().addAll(connection4, connection5);

        connRoot.getSubUnits().addAll(file1, file2, file3);
    }

    /**
     * Iterate TreeView nodes to print items (debug mode).
     * SRC: http://stackoverflow.com/questions/28342309/iterate-treeview-nodes
     *
     * @param root tree root
     */
    private void printChildren(TreeItem<?> root){
        System.out.println("Current Parent: " + root.getValue());
        for(TreeItem<?> child: root.getChildren()){
            if(child.getChildren().isEmpty()){
                System.out.println(child.getValue());
            } else {
                printChildren(child);
            }
        }
    }

    /**
     * Loads connections (Conn) data from the specified ConnFile,
     * unmarshaled with JAXB.
     *
     * @param connFile
     * @return List of Conn objects
     */
    public List<Conn> loadConnDataFromConnFile(ConnFile connFile) {
        File file = new java.io.File(connFile.getFileName());

        // debug mode
        //System.out.println(file.toString());

        try {
            JAXBContext context = JAXBContext.newInstance(ConnListWrapper.class);

            // debug mode
            //System.out.println(context.toString());

            Unmarshaller um = context.createUnmarshaller();

            // Reading XML from the file and unmarshalling.
            ConnListWrapper wrapper = (ConnListWrapper) um.unmarshal(file);

            // debug mode
            /*
            for (Conn conn: wrapper.getConns()) {
                System.out.println(conn);
            }
            */

            return wrapper.getConns();

        } catch (Exception e) { // catches ANY exception
            setBrokenAndAlertLoadData(connFile, file);
        }

        return null;
    }

    /**
     * Set broken state to connFile parameter
     * and popup Error to load data on specified file.
     *
     * @param connFile
     * @param file
     */
    private void setBrokenAndAlertLoadData(ConnFile connFile, File file) {

        connFile.setBroken();

        if (Pref.isErrorLoadingFilePopUpAtStartOrOnOpen()) {
            AlertDialog.provide(MainApp.PRIMARY_STAGE,
                    Alert.AlertType.ERROR,
                    ALERR_LOAD_DATA_TITLE,
                    ALERR_LOAD_DATA_HEADER,
                    ALERR_LOAD_DATA_CONTENT + file.getPath(), true);
        }
    }

    /**
     * Decrypt ConnFile object
     *
     * @param connFile
     */
    public boolean decryptConnFile(ConnFile connFile) {

        String password = getConnFilePassword(connFile);

        if (password != null) {
            connFile.setPasswordProtected(true);
            connFile.setPassword(password);
            connFile.setDecrypted();
            return true;
        }

        return false;
    }

    /**
     * Ask user for password in a dialog.
     * This password is used at ConnFile level
     * to encrypt/decrypt Conn password
     *
     * @param connFile
     * @return password if correct, null otherwise
     */
    private String getConnFilePassword(ConnFile connFile) {

        boolean tryAgain;

        do {
            PasswordDialog pd = new PasswordDialog(connFile.getFileName());
            Optional<String> passwordDialogResult = pd.showAndWait();
            //result.ifPresent(password -> System.out.println(password));

            if (passwordDialogResult.isPresent()) {
                String password = passwordDialogResult.get();

                try {
                    Crypto crypto = new Crypto(password);
                    // If no exception thrown by line below, means that password is correct
                    // If first element (Conn (get(0))) of the list returned by loadConnDataFromConnFile method is encrypted,
                    // also all others are (with same password)
                    crypto.getDecrypted(loadConnDataFromConnFile(connFile).get(0).getPassword());

                    return password;

                } catch (Exception e) {

                    //e.printStackTrace();

                    Alert alert = AlertDialog.provide(MainApp.PRIMARY_STAGE,
                            Alert.AlertType.CONFIRMATION,
                            ALCNF_BAD_PASSWORD_TITLE,
                            ALCNF_BAD_PASSWORD_HEADER,
                            ALCNF_BAD_PASSWORD_CONTENT, false);

                    Optional<ButtonType> badPasswordDialogResult = alert.showAndWait();

                    if (badPasswordDialogResult.get() == ButtonType.OK) {
                        // ... user chose OK
                        tryAgain = true;

                    } else {
                        // ... user chose CANCEL or closed the dialog
                        tryAgain = false;
                    }
                }
            } else {
                tryAgain = false;
            }

        } while (tryAgain);

        return null;
    }

    /**
     * Debug helper method to get preferences
     * in a String format.
     *
     * @return user preferences in a String format
     */
    private String preferencesToString() {
        String[] prefKeys = null;
        String preferencesToString = "";

        if (preferences != null) {
            try {
                prefKeys = preferences.keys();
            } catch (BackingStoreException e) {
                //e.printStackTrace();
            } catch (IllegalStateException e) {
                //e.printStackTrace();
            }
        }

        if (prefKeys != null) {
            for (String prefKey : prefKeys) preferencesToString += "key: " + prefKey + " / value: " + preferences.get(prefKey, null) + "\n";
        }

        return preferencesToString;
    }

}
