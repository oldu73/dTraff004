package ch.stageconcept.dtraff.connection.model;

import ch.stageconcept.dtraff.connection.util.*;
import ch.stageconcept.dtraff.main.view.RootLayoutController;
import ch.stageconcept.dtraff.preference.model.Pref;
import ch.stageconcept.dtraff.util.AlertDialog;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.codefx.libfx.listener.handle.ListenerHandle;
import org.codefx.libfx.listener.handle.ListenerHandles;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * Root class for Network.
 *
 * The treeView structure:
 * #######################
 *
 * Network
 * |
 * +-- ConnFile A
 * |    |-- Conn A
 * |    |    +-- DataBase A
 * |    |    +-- DataBase B
 * |    |-- Conn B
 * |
 * +-- ConnFile B
 * |    |-- Conn A
 * |    |-- Conn B
 *
 * #######################
 *
 * SRC: http://stackoverflow.com/questions/35009982/javafx-treeview-of-multiple-object-types-and-more
 * SRC: https://github.com/james-d/heterogeneous-tree-example
 *
 * @author james-d
 * Adapted by Olivier Durand
 */
public class Network extends ConnUnit<ConnFile> {

    // Fields ##############################################################################

    private static final String ICON_FILENAME = "network001.png";
    public static final String PREFS_PATH = "/ch/stageconcept/datatraffic/file";
    private static final String MENU_NEW_FILE = "New File";
    private static final String MENU_OPEN_FILE = ConnFile.MENU_OPEN_FILE;
    private static final String CONNFILE_DEFAULT_NAME = "default";

    // Alerts statics texts
    private static final String ALERR_LOAD_DATA_TITLE = "Error";
    private static final String ALERR_LOAD_DATA_HEADER = "Could not load data";
    private static final String ALERR_LOAD_DATA_CONTENT = "Could not load data from file(s):\n";

    private static final String ALCNF_BAD_PASSWORD_TITLE = ConnFile.MENU_ENTER_PASSWORD;
    private static final String ALCNF_BAD_PASSWORD_HEADER = "Bad password!";
    private static final String ALCNF_BAD_PASSWORD_CONTENT = "Try again?";

    private Stage stage;
    private RootLayoutController rootLayoutController;

    // User preferences (file node)
    private Preferences prefs = Preferences.userRoot().node(PREFS_PATH);
    private String[] prefKeys = null;

    private ListenerHandle subUnitsListenerHandle;

    // Predicates
    private static final Predicate<ConnFile> predicateSubUnitAll = subUnit -> true;
    private static final Predicate<ConnFile> predicateSubUnitBroken = subUnit -> subUnit.isBroken();
    private static final Predicate<ConnFile> predicateSubUnitNotBroken = subUnit -> !subUnit.isBroken();
    private static final Predicate<ConnFile> predicateSubUnitClear = subUnit -> subUnit.isClear();
    private static final Predicate<ConnFile> predicateSubUnitNotClear = subUnit -> !subUnit.isClear();
    private static final Predicate<ConnFile> predicateSubUnitEncrypted = subUnit -> subUnit.isEncrypted();
    private static final Predicate<ConnFile> predicateSubUnitNotEncrypted = subUnit -> !subUnit.isEncrypted();
    private static final Predicate<ConnFile> predicateSubUnitDecrypted = subUnit -> subUnit.isDecrypted();
    private static final Predicate<ConnFile> predicateSubUnitNotDecrypted = subUnit -> !subUnit.isDecrypted();

    // Unary Operators

    /**
     * unaryOperatorToEncrypted, UnaryOperator Functional Interface
     *
     * Sub Unit (ConnFile) check if corresponding OS file is encrypted
     * (has an encrypted password field) if so, set ConnFile instance state to ENCRYPTED.
     */
    private final UnaryOperator<ConnFile> unaryOperatorToEncrypted = subUnit -> {

        List<Conn> listConn = loadConnsFromSubUnit(subUnit);
        // debug mode
        //System.out.println("String to decrypt: " + listConn.get(0).getPassword());

        // If first element (Conn) of the list is encrypted, also all others are (with same password)
        if (listConn != null && listConn.get(0).isPasswordEncrypted()) subUnit.setEncrypted();

        return subUnit;
    };

    // #####################################################################################

    // Constructors ########################################################################

    /**
     * Constructor.
     * @param name
     * @param subUnits
     */
    public Network(String name, ObservableList<ConnFile> subUnits) {
        super(name, subUnits, ConnFile::new, ICON_FILENAME);

        ContextMenu contextMenu = new ContextMenu();

        MenuItem newFileMenuItem = new MenuItem(MENU_NEW_FILE);
        newFileMenuItem.setOnAction((ActionEvent t) -> newConnFile());

        MenuItem openFileMenuItem = new MenuItem(MENU_OPEN_FILE);
        openFileMenuItem.setOnAction((ActionEvent t) -> getRootLayoutController().openConnFile());

        contextMenu.getItems().addAll(newFileMenuItem, openFileMenuItem);
        this.setMenu(contextMenu);
    }

    /**
     * Constructor.
     * @param name
     */
    public Network(String name) {
        this(name, FXCollections.observableArrayList());
    }

    /**
     * Constructor.
     * @param name
     * @param stage
     * @param rootLayoutController
     */
    public Network(String name, Stage stage, RootLayoutController rootLayoutController) {
        this(name, FXCollections.observableArrayList());
        this.stage = stage;
        this.rootLayoutController = rootLayoutController;

        createSubUnits();
    }

    // #####################################################################################

    // Getters & Setters ###################################################################

    /**
     * rootLayoutController getter
     * @return rootLayoutController
     */
    public RootLayoutController getRootLayoutController() {
        return rootLayoutController;
    }

    /**
     * rootLayoutController setter
     * @param rootLayoutController
     */
    public void setRootLayoutController(RootLayoutController rootLayoutController) {
        this.rootLayoutController = rootLayoutController;
    }

    // #####################################################################################

    // Methods #############################################################################

    /**
     * Create network first sub level structure ConnFile instances list (subUnits),
     * to be used in a treeView : Network (root node) - ConnFile - Conn - Database - (...)
     *
     * Process:
     * ========
     *
     * - 1. Load user preferences keys
     * - 2. Create Sub Units list from keys
     * - 3. If list not empty
     *      - 4. Set nonexistent file in list to broken
     *      - 5. Set password protected file in list to encrypted
     */
    private void createSubUnits() {

        loadPrefKeys();
        createSubUnitsFromPrefKeys();

        if (!subUnits.isEmpty()) {
            setSubUnitsBroken();
            //setSubUnitsEncrypted();
            setSubUnits(predicateSubUnitNotBroken, unaryOperatorToEncrypted);
        }

    }

    /**
     * Load user preferences from BackingStore
     * to field prefKeys (String array).
     */
    private void loadPrefKeys() {

        if (prefs != null) {
            try {
                prefKeys = prefs.keys();
            } catch (BackingStoreException e) {
                //System.err.println("unable to read backing store: " + e);
                //e.printStackTrace();
            } catch (IllegalStateException e) {
                //System.err.println("..., " + e);
                //System.out.println("node has been removed!");
                //e.printStackTrace();
            }
        }

    }

    /**
     * Create Network Sub Units (ConnFile)
     * from prefKeys field.
     */
    private void createSubUnitsFromPrefKeys() {

        if (prefKeys != null) {

            for (String name : prefKeys) {

                String fileName = prefs.get(name, null);

                if (fileName != null) {
                    ConnFile connFile = new ConnFile(name, fileName, this, rootLayoutController);
                    subUnits.add(connFile);
                }
            }
        }

    }

    /**
     * Iterate through Sub Units (ConnFiles) to check if corresponding OS file is OK
     * (exist and not a directory), if not, set ConnFile instance state to BROKEN.
     */
    private void setSubUnitsBroken() {

        subUnits.forEach(subUnit -> {
            File file = new File(subUnit.getFileName());
            if(!file.exists() || file.isDirectory()) subUnit.setBroken();
        });

    }

    /**
     * Set predicate filtered subUnits sub list
     * to unaryOperator function.
     *
     * @param predicate
     * @param unaryOperator
     */
    private void setSubUnits(Predicate<ConnFile> predicate, UnaryOperator<ConnFile> unaryOperator) {
        subUnits.stream().filter(predicate).forEach(subUnit -> unaryOperator.apply(subUnit));
    }

    /**
     * Iterate through Sub Units (ConnFiles) to check if at least one match predicate.
     * SRC: http://www.concretepage.com/java/jdk-8/java-8-stream-allmatch-anymatch-nonematch-example
     *
     * @param predicate
     * @return true if subUnits field contains at least one ConnFile instance that match predicate,
     * false otherwise.
     */
    private boolean hasSubUnit(Predicate<ConnFile> predicate) {
        return subUnits.stream().anyMatch(predicate);
    }

    /**
     * Get Specified (filtered with predicate parameter)
     * Sub Unit (ConnFile) list, sorted on ConnFile.name
     * field in alphabetical order.
     *
     * @param predicate
     * @return subUnits field sub list of ConnFile instances
     * that match predicate.
     */
    private List<ConnFile> getSubUnitsSubList(Predicate<ConnFile> predicate) {
        return subUnits
                .stream()
                .sorted((connFile1, connFile2) -> connFile1.getName().compareTo(connFile2.getName()))
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Get names and file names (path) from ConnFile instances list
     * given by parameter to format them in a string output.
     * Name, file name represent an entry separated by a space, a hyphen and a space
     * and each entry is separated by a new line.
     *
     * SRC: https://ivarconr.wordpress.com/2013/11/20/java-8-joining-strings-with-stream-api/
     *
     * @param subUnits
     * @return string in format name - fileName (path) \n
     */
    private String namesFileNamesToString(List<ConnFile> subUnits) {
        return subUnits
                .stream()
                .map(connFile -> connFile.getName() + " - " + connFile.getFileName())
                .collect(Collectors.joining("\n"));
    }

    /**
     * Alert popup dialog to inform user
     * with broken files.
     */
    private void alertLoadFiles() {

        AlertDialog.provide(stage,
                Alert.AlertType.ERROR,
                ALERR_LOAD_DATA_TITLE,
                ALERR_LOAD_DATA_HEADER,
                ALERR_LOAD_DATA_CONTENT + namesFileNamesToString(getSubUnitsSubList(predicateSubUnitBroken)), true);

    }

    /**
     * Loads list of connections (Conn) data from the specified
     * subUnit (ConnFile) instance parameter, unmarshaled with JAXB.
     *
     * @param connFile
     * @return List of Conn instances
     */
    private List<Conn> loadConnsFromSubUnit(ConnFile connFile) {

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
            //e.printStackTrace();
        }

        return null;

    }

    /**
     * Load file error confirmation or enter encrypted file password,
     * at start is needed.
     *
     * @return true if user interaction (through popup (relative to ConnFile instances state
     * in subUnits field (list) and user preferences)) may be necessary either to confirm
     * error loading data from file or enter password for encrypted file (use case example:
     * display of Initializing... message in center of RootLayout at start),
     * false otherwise.
     */
    public boolean isUserActionNeededAtStart () {
        return (hasSubUnit(predicateSubUnitBroken) && Pref.isErrorLoadingFilePopUpAtStartOrOnOpen()) ||
                (hasSubUnit(predicateSubUnitEncrypted) && Pref.isDecryptFilePassPopUpAtStartOrOnOpen());
    }

    /**
     * On filled conditions (the "OnNeed" in method name ;-),
     * popup an alert message to inform user about broken files.
     */
    public void alertUserLoadFileErrorOnNeed() {
        if (hasSubUnit(predicateSubUnitBroken) && Pref.isErrorLoadingFilePopUpAtStartOrOnOpen()) alertLoadFiles();
    }

    /**
     * On filled conditions, ask user to enter password
     * for encrypted subUnit list through popup(s).
     */
    public void alertUserEnterFilePasswordOnNeed() {

    }

    /**
     * Set subUnit (ConnFile) to decrypted (and related fields),
     * if password (popup) is correct.
     *
     * @param connFile
     * @return true on correct password entered by user and operation
     * succeeded, false otherwise
     */
    public boolean setSubUnitDecrypted(ConnFile connFile) {

        String password = getSubUnitPassword(connFile);

        if (password != null) {
            connFile.setPasswordProtected(true);
            connFile.setPassword(password);
            connFile.setState(ConnFileState.DECRYPTED);
            return true;
        }

        return false;
    }

    /**
     * Ask user for password in a dialog.
     * This password is used at subUnit (ConnFile) level
     * to encrypt/decrypt connection (Conn) password
     *
     * @param connFile
     * @return password if correct, null otherwise
     */
    private String getSubUnitPassword(ConnFile connFile) {

        //TODO When TAB key is pressed to change focus on Cancel button and hit ENTER key
        //Bad password popup is displayed even if password field was let empty (OK button (which is highlighted) like behavior)
        //Also Bad Password dialog has an "Annuler" button who should be a "Cancel" button!

        boolean tryAgain;

        do {
            PasswordDialog passwordDialog = new PasswordDialog(connFile.getFileName());
            Optional<String> passwordDialogResult = passwordDialog.showAndWait();
            // debug mode
            //result.ifPresent(password -> System.out.println(password));

            if (passwordDialogResult.isPresent()) {
                String password = passwordDialogResult.get();

                try {
                    Crypto crypto = new Crypto(password);
                    // If no exception thrown by line below, means that password is correct.
                    // If first element (Conn (get(0))) of the list returned by loadConnsFromSubUnit
                    // method is encrypted, also all others are (with same password).
                    crypto.getDecrypted(loadConnsFromSubUnit(connFile).get(0).getPassword());

                    return password;

                } catch (Exception e) {

                    //e.printStackTrace();

                    Alert alert = AlertDialog.provide(stage,
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
     * New ConnFile object.
     * Create new File entry to Connections treeView.
     */
    public void newConnFile() {
        ConnFile connFile = new ConnFile(CONNFILE_DEFAULT_NAME);
        connFile.setRootLayoutController(rootLayoutController);
        if (ConnFileEditor.INSTANCE.supply(connFile)) {
            if (connFile.isPasswordProtected()) {
                connFile.setState(ConnFileState.DECRYPTED);
            }
            connFile.setParent(this);
            getSubUnits().add(connFile);
            prefs.put(connFile.getName(), connFile.getFileName());
        }
    }

    /**
     * Close ConnFile object.
     * Remove File entry from Connections treeView.
     *
     * @param connFile
     */
    public void closeConnFile(ConnFile connFile) {
        prefs.remove(connFile.getName());
        this.getSubUnits().remove(connFile);
    }

    /**
     * Sort subUnits, alphabetical/numerical order.
     */
    public void sortSubUnits() {

        // debug mode
        /*
        System.out.println("sort in..");
        System.out.println("/////////");
        getSubUnits().forEach((subUnit) -> {
            System.out.println("subUnit1: " + subUnit);
        });
        System.out.println("/////////");
        rootLayoutController.getConnectionTreeView().getRoot().getChildren().forEach((subUnit) -> {
            System.out.println("subUnit2: " + subUnit);
        });
        System.out.println("/////////");
        */

        // Provided by LibFX. If omitted:
        // - list change triggered listener which call sort method which change the list
        // that triggered listener which call sort method... and so on, until a nice StackOverflowError exception.
        // Workaround: - detach listener - do the job (sort list) - attach listener.
        subUnitsListenerHandle.detach();

        try {
            //FXCollections.sort(getSubUnits(), (ConnFile cf1, ConnFile cf2) -> cf1.getName().compareTo(cf2.getName()));
            //getSubUnits().sort((ConnFile cf1, ConnFile cf2) -> cf1.getName().compareTo(cf2.getName()));

            //TODO Improve comparator: - test1, test10, test2, test3.. in the list is not so nice
            // (because of one, one zero for test1, test10)

            rootLayoutController.getConnectionTreeView().getRoot().getChildren()
                    .sort((TreeItem<? super ConnFile> cf1, TreeItem<? super ConnFile> cf2) ->
                            ((ConnFile)(cf1.getValue())).getName().compareTo(((ConnFile)(cf2.getValue())).getName()));

            //rootLayoutController.getConnectionTreeView().refresh();
        } catch (StackOverflowError e) {
            System.err.println("The method Network.sortSubUnits() raise a StackOverflowError exception!");
        }

        subUnitsListenerHandle.attach();

        // debug mode
        /*
        System.out.println("..sort out");
        System.out.println("/////////");
        getSubUnits().forEach((subUnit) -> {
            System.out.println("subUnit1: " + subUnit);
        });
        System.out.println("/////////");
        rootLayoutController.getConnectionTreeView().getRoot().getChildren().forEach((subUnit) -> {
            System.out.println("subUnit2: " + subUnit);
        });
        System.out.println("/////////");
        */
    }

    /**
     * Sort subUnits on list change.
     *
     * SRC: https://www.javacodegeeks.com/2015/01/dont-remove-listeners-use-listenerhandles.html
     * SRC: http://libfx.codefx.org/
     * SRC: https://github.com/CodeFX-org/LibFX/wiki/ListenerHandle
     * SRC: http://java.developpez.com/faq/javafx/?page=Collections-observables
     */
    public void sortSubUnitsOnChangeListener() {

        /*
        ChangeListener<? extends ObservableList<ConnFile>> listener = (ChangeListener<ObservableList<ConnFile>>) (observable, oldValue, newValue) -> {
            System.out.println("changed " + oldValue + "->" + newValue);
            //myFunc();
        };
        */

        ListChangeListener<? super ConnFile> listener = (ListChangeListener<ConnFile>) (change) -> {
            // debug mode
            //System.out.println("\n" + change + "\n");
            sortSubUnits();
        };

        // LibFX way, enable to attach/detach listener on property
        // which is useful in sort method (cf. sortSubUnits() method for more documentation)
        // This immediately adds the listener to the property
        subUnitsListenerHandle = ListenerHandles.createAttached(getSubUnits(), listener);

        // Standard way
        /*
        getSubUnits().addListener(listener);

        // or

        getSubUnits().addListener((ListChangeListener.Change<? extends ConnFile> change) -> {
            // debug mode
            //System.out.println(change);
            sortSubUnits();
        });
        */
    }

    // #####################################################################################

}
