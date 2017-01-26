package ch.stageconcept.dtraff.main.view;

import ch.stageconcept.dtraff.connection.model.*;
import ch.stageconcept.dtraff.connection.util.*;
import ch.stageconcept.dtraff.connection.view.ModelTree;
import ch.stageconcept.dtraff.main.MainApp;
import ch.stageconcept.dtraff.preference.model.Pref;
import ch.stageconcept.dtraff.preference.util.PrefEditor;
import ch.stageconcept.dtraff.util.AlertDialog;
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
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
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

    // ConnRoot treeView root denomination
    private static final String NETWORK = "Network";

    // ConnRoot treeView CSS resource
    private static final String CONNROOT_TREEVIEW_CSS = "/connRootTreeView.css";

    // Initializing static text
    private static final String LABEL_INITIALIZING = "Initializing...";

    // Alerts statics texts
    private static final String ALINF_ABOUT_TITLE = "Data Traffic";
    public static final String ALINF_ABOUT_HEADER = ALINF_ABOUT_TITLE + Release.NUMBER.getValue();
    private static final String ALINF_ABOUT_CONTENT = "Author: Olivier Durand\nWebsite: http://www.stageconcept.ch";

    private static final String ALERR_LOAD_DATA_TITLE = "Error";
    private static final String ALERR_LOAD_DATA_HEADER = "Could not load data";
    private static final String ALERR_LOAD_DATA_CONTENT = "Could not load data from file:\n";

    private static final String ALCNF_BAD_PASSWORD_TITLE = ConnFile.MENU_ENTER_PASSWORD;
    private static final String ALCNF_BAD_PASSWORD_HEADER = "Bad password!";
    private static final String ALCNF_BAD_PASSWORD_CONTENT = "Try again?";

    private static final String ALINF_FILE_ALREADY_PRESENT_TITLE = "File Open";
    private static final String ALINF_FILE_ALREADY_PRESENT_HEADER = "File entry already present";
    public static final String ALINF_FILE_ALREADY_PRESENT_CONTENT = "A file entry with specified name is already present:\n";

    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private Label initializingLabel;

    @FXML
    private MenuItem fileNewMenuItem;

    @FXML
    private MenuItem fileOpenMenuItem;

    @FXML
    private MenuItem fileEnterPasswordMenuItem;

    @FXML
    private MenuItem fileCloseMenuItem;

    @FXML
    private MenuItem serverConnectionMenuItem;

    @FXML
    private MenuItem newServerConnectionMenuItem;

    @FXML
    private MenuItem editServerConnectionMenuItem;

    @FXML
    private MenuItem editDeleteMenuItem;

    @FXML
    private MenuItem editPreferencesMenuItem;

    private ConnRoot connRoot;    // ConnRoot description to be used in a treeView : ConnRoot (root node) - ConnFile - Conn - Database - (...)
    private ModelTree<ConnUnit<?>> connectionTree;
    private TreeView<ConnUnit<?>> connectionTreeView;
    private Preferences preferences = Preferences.userRoot().node(ConnRoot.PREFS_PATH);  // User preferences
    private ObjectProperty<ConnFileState> selectedConnFileState = new SimpleObjectProperty<>();

    // Getters and Setters
    // #####################################################################

    public BorderPane getRootBorderPane() {
        return rootBorderPane;
    }

    public Label getInitializingLabel() {
        return initializingLabel;
    }

    public TreeView<ConnUnit<?>> getConnectionTreeView() {
        return connectionTreeView;
    }

// Methods
    // #####################################################################

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() { /* not used! */ }

    /**
     * Initialization called from outside.
     *
     * Implemented in order to wait until Main application
     * window appear before launching initialization process.
     * The goal to reach is, about ConnFile objects, to warn user about broken one
     * or ask user password for encrypted one after that Main window is displayed.
     */
    public void subInitialize() {

        connRoot = new ConnRoot(NETWORK, MainApp.primaryStage, this);

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
     * Ante initialize
     *
     * With initialization label on main
     * window background displayed (and animated).
     */
    private void anteInitialize() {

        connectionTree = new ModelTree<>(connRoot,
                ConnUnit::getSubUnits,
                ConnUnit::nameProperty,
                ConnUnit::iconProperty,
                ConnUnit::menuProperty,
                unit -> PseudoClass.getPseudoClass(unit.getClass().getSimpleName().toLowerCase()));

        //TODO on close/delete connRoot subUnits.. maintain treeView and related ObservableList synchronized.

        connectionTreeView = connectionTree.getTreeView();

        connRoot.sortSubUnitsOnChangeListener();
        // Initial sort
        connRoot.sortSubUnits();

        // CSS pseudo class treeView style.
        // !WARNING! In order to use file that reside in resources folder, don’t forget to add a slash before file name!
        connectionTreeView.getStylesheets().add(getClass().getResource(CONNROOT_TREEVIEW_CSS).toExternalForm());

        connectionTreeView.getRoot().setExpanded(true);

    }

    /**
     * Post initialize
     *
     * After initialization label on main
     * window background disappear (fade out).
     */
    private void postInitialize() {

        rootBorderPane.setLeft(connectionTreeView);

        // debug mode
        //printChildren(connectionTreeView.getRoot());

        //TODO add double click behavior on broken file to open fileChooser.
        //TODO refactor string format (cf. ConnRoot namesFileNamesToString) in enter password dialog.
        // Double click on ConnRoot treeView
        connectionTreeView.setOnMouseClicked((event) ->
        {
            if(event.getClickCount() == 2 &&
                    connectionTreeView.getSelectionModel().getSelectedItem().getValue() instanceof ConnFile) {

                //TreeItem<?> item = connectionTreeView.getSelectionModel().getSelectedItem();
                ConnFile connFile = (ConnFile) connectionTreeView.getSelectionModel().getSelectedItem().getValue();

                if (connFile.isEncrypted() && decryptConnFile(connFile)) {
                    populateSubunit(connFile, loadConnDataFromConnFile(connFile));
                }
            }
        });

        // ### Tool bar menu ###
        // #####################

        // Some File - menus disable property initial state
        newServerConnectionMenuItem.setDisable(true);
        fileEnterPasswordMenuItem.setDisable(true);

        // Some File - menus disable property setting if the ConnRoot treeView
        // selected item is not a ConnFile object and other menu specific related conditions
        connectionTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.getValue() instanceof ConnFile) {
                // Following line is related on double click (see above and below)
                selectedConnFileState.bind(((ConnFile) newValue.getValue()).stateProperty());

                // debug mode
                //System.out.print("1 ");

                // ### File - Server Connection - New: Disable if the ConnRoot treeView
                // selected item is not a clear or decrypted ConnFile object
                if (((ConnFile) newValue.getValue()).isClear() ||
                        ((ConnFile) newValue.getValue()).isDecrypted()) {
                    newServerConnectionMenuItem.setDisable(false);

                    // debug mode
                    //System.out.print("2 ");

                } else {
                    newServerConnectionMenuItem.setDisable(true);

                    // debug mode
                    //System.out.print("3 ");

                }
                // ############################################################

                // ### File - Enter Password: Disable if the ConnRoot treeView
                // selected item is not an encrypted ConnFile object
                if (((ConnFile) newValue.getValue()).isEncrypted()) {
                    fileEnterPasswordMenuItem.setDisable(false);
                } else {
                    fileEnterPasswordMenuItem.setDisable(true);
                }
                // ############################################################

            } else {
                // ConnRoot treeView selected item is NOT a ConnFile object
                newServerConnectionMenuItem.setDisable(true);
                fileEnterPasswordMenuItem.setDisable(true);

                // debug mode
                //System.out.print("4 ");

            }
        });

        // After double click on an encrypted file to enter correct password,
        // the serverConnectionMenuItem remain disabled until treeView selection changed!
        // So, the bellowing lines deserve to track ConnFile (in ConnRoot treeView)
        // selected object state changes in order to update the newServerConnectionMenuItem disabled status.
        selectedConnFileState.addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && (oldValue.equals(ConnFileState.ENCRYPTED) && newValue.equals(ConnFileState.DECRYPTED))) {
                newServerConnectionMenuItem.setDisable(false);
                fileEnterPasswordMenuItem.setDisable(true);

                // debug mode
                //System.out.print("5 ");

            }
        });

        /*
        // Old version with BooleanBinding
        newServerConnectionMenuItem.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        connectionTreeView.getSelectionModel().getSelectedItem() == null ||
                                !(connectionTreeView.getSelectionModel().getSelectedItem().getValue() instanceof ConnFile) ||
                                (connectionTreeView.getSelectionModel().getSelectedItem().getValue() instanceof ConnFile &&
                                        (((ConnFile) connectionTreeView.getSelectionModel().getSelectedItem().getValue()).isBroken() ||
                                                ((ConnFile) connectionTreeView.getSelectionModel().getSelectedItem().getValue()).isEncrypted())),
                connectionTreeView.getSelectionModel().selectedItemProperty()));
        */

        // Disable tool bar menu File - Server Connection - Edit if no item or not a Conn object instance are selected in ConnRoot treeView
        editServerConnectionMenuItem.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        connectionTreeView.getSelectionModel().getSelectedItem() == null ||
                                !(connectionTreeView.getSelectionModel().getSelectedItem().getValue() instanceof Conn),
                connectionTreeView.getSelectionModel().selectedItemProperty()));

        // Disable tool bar menu File - Server Connection if File - Server Connection - New and Edit are disabled
        serverConnectionMenuItem.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        newServerConnectionMenuItem.isDisable() && editServerConnectionMenuItem.isDisable(),
                newServerConnectionMenuItem.disableProperty(),
                editServerConnectionMenuItem.disableProperty()));

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

        final IntegerProperty i = new SimpleIntegerProperty(12);
        Timeline timeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(
                Duration.seconds(0.4),
                event -> {
                    if (i.get() > LABEL_INITIALIZING.length()) {
                        i.set(12);
                        timeline.playFromStart();
                    } else {
                        initializingLabel.setText(LABEL_INITIALIZING.substring(0, i.get()));
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
     * Called when the user selects the tool bar File - New menu.
     */
    @FXML
    private void handleFileNew() {
        connRoot.newConnFile();
    }

    /**
     * Called when the user selects the tool bar File - Open menu.
     */
    @FXML
    private void handleFileOpen() {
        ConnFile connFile = getSelectedConnFile();
        if (connFile != null && connFile.isBroken()) openBrokenConnFile(connFile);
        else openConnFile();
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
                    connFile.setClear();

                    treatSubUnit(connFile, true);
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
        //TODO put in "Thread" like, when the fileChooser close, the UI is frozen for a while (depends of machine)
        FileChooser fileChooser = new FileChooser();
        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);
        // Show open file dialog
        return fileChooser.showOpenDialog(MainApp.primaryStage);
    }

    /**
     * Alert ConnFile already present in treeView.
     * @param connFile
     */
    private void alertAlreadyPresent(ConnFile connFile) {
        AlertDialog.provide(MainApp.primaryStage,
                Alert.AlertType.INFORMATION,
                ALINF_FILE_ALREADY_PRESENT_TITLE,
                ALINF_FILE_ALREADY_PRESENT_HEADER,
                ALINF_FILE_ALREADY_PRESENT_CONTENT + connFile.getFileName(), true);
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

            if (getConnFile(name) != null) alertAlreadyPresent(getConnFile(name));
            else {
                // Open ConnFile (file) and create new ConnRoot treeView entry
                ConnFile connFile = new ConnFile(name);
                connFile.setFileName(fileName);
                connFile.setParent(connRoot);
                connFile.setRootLayoutController(this);
                connRoot.getSubUnits().add(connFile);
                treatSubUnit(connFile, true);
            }
        }
    }

    /**
     * Called when the user selects the tool bar File - Enter Password menu.
     */
    @FXML
    private void handleFileEnterPassword() {
        // The tool bar menu is disabled if none or not an encrypted ConnFile is selected in Connection treeView.
        ConnFile connFile = getSelectedConnFile();

        if (connFile != null && decryptConnFile(connFile)) {
            populateSubunit(connFile, loadConnDataFromConnFile(connFile));
        }
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
        Conn conn = (Conn) connectionTreeView.getSelectionModel().getSelectedItem().getValue();
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
        AlertDialog.provide(MainApp.primaryStage,
                Alert.AlertType.INFORMATION,
                ALINF_ABOUT_TITLE,
                ALINF_ABOUT_HEADER,
                ALINF_ABOUT_CONTENT, true);
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        System.exit(0);
    }

    /**
     * Get ConnRoot treeView selected ConnFile object
     *
     * @return If selected item is an instance of ConnFile, return this object,
     * null otherwise
     */
    private ConnFile getSelectedConnFile() {

        try {
            Object selectedObject = connectionTreeView.getSelectionModel().getSelectedItem().getValue();

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

        ConnRoot connRoot = new ConnRoot(NETWORK);
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
            AlertDialog.provide(MainApp.primaryStage,
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

        //TODO When TAB key is pressed to change focus on Cancel button and hit ENTER key
        //Bad password popup is displayed even if password field was let empty (OK button (which is highlighted) like behavior)
        //Also Bad Password dialog has an "Annuler" button who should be a "Cancel" button!

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

                    Alert alert = AlertDialog.provide(MainApp.primaryStage,
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
