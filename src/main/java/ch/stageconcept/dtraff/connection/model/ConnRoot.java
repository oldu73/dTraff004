package ch.stageconcept.dtraff.connection.model;

import ch.stageconcept.dtraff.connection.util.*;
import ch.stageconcept.dtraff.main.MainApp;
import ch.stageconcept.dtraff.main.view.RootLayoutController;
import ch.stageconcept.dtraff.preference.model.Pref;
import ch.stageconcept.dtraff.util.AlertDialog;
import ch.stageconcept.dtraff.util.I18N;
import ch.stageconcept.dtraff.util.StringUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.codefx.libfx.listener.handle.ListenerHandle;
import org.codefx.libfx.listener.handle.ListenerHandles;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

//TODO refactor (clean) doc

//TODO Manage empty file (state, icon, ...). What about empty encrypted file?

/**
 * Root class.
 *
 * The treeView structure:
 * #######################
 *
 * ConnRoot
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
 * JavaFX TreeView of multiple object types? (and more)
 * SRC: http://stackoverflow.com/questions/35009982/javafx-treeview-of-multiple-object-types-and-more
 * ANSWER FROM: James_D
 * GITHUB: - heterogeneous-tree-example - https://github.com/james-d/heterogeneous-tree-example
 *
 * @author james-d
 * Adapted by Olivier Durand
 */
public class ConnRoot extends ConnUnit<ConnFile> {

    // Fields ##############################################################################

    // ConnRoot treeView root denomination
    public static final String NETWORK = "Network";

    // Icon resource file name and size
    private static final String ICON_FILENAME = "network001.png";
    private static final int ICON_HEIGHT = 16;
    private static final int ICON_WIDTH = ICON_HEIGHT;

    public static final String PREFS_PATH = "/ch/stageconcept/datatraffic/file";
    private static final String CONNFILE_DEFAULT_NAME = "default";

    private static Stage stage;
    private RootLayoutController rootLayoutController;

    // User preferences (file node)
    private Preferences prefs = Preferences.userRoot().node(PREFS_PATH);
    private String[] prefKeys = null;

    private ListenerHandle subUnitsListenerHandle;

    private final ObjectProperty<ConnRootState> state;
    private String passwordToCheck = null;

    // Functional interface implementations, Predicate, Consumer, Function, ...

    /**
     * Check if subUnit.fileName represent a valid OS file:
     * - exist
     * - is not a directory
     */
    private static Predicate<ConnFile> isFileInSubUnitOk = subUnit -> subUnit.getFile().exists() && !subUnit.getFile().isDirectory();

    /**
     * Check if subUnit.fileName represent a not empty OS file
     * SRC: http://stackoverflow.com/questions/7190618/most-efficient-way-to-check-if-a-file-is-empty-in-java-on-windows
     */
    private static Predicate<ConnFile> isFileInSubUnitNotEmpty = subUnit -> {

        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(subUnit.getFileName()));
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            return false;
        }

        try {
            if (br.readLine() != null) {
                br.close(); // !!! MOST IMPORTANT !!!
                return true;
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }

        return false;
    };

    /**
     * Check if subUnit file is damaged
     */
    private static Predicate<ConnFile> isFileInSubUnitNotDamaged = subUnit -> {
        if (loadConnsFromSubUnit(subUnit) == null) return false;
        return true;
    };

    /**
     * Is subUnit OS file's fileName represented, ready to populate
     * subUnit.subUnits (Conns)?
     */
    private static Predicate<ConnFile> isFileInSubUnitReadyToPopulate = subUnit ->
            subUnit != null &&
                    !(subUnit.isBroken() || subUnit.isEncrypted()) &&
                    !(subUnit.getName() == null || subUnit.getName().length() == 0) &&
                    !(subUnit.getFileName() == null || subUnit.getFileName().length() == 0) &&
                    isFileInSubUnitOk.and(isFileInSubUnitNotEmpty).and(isFileInSubUnitNotDamaged).test(subUnit);

    /**
     * Is subUnit state empty clear or empty decrypted?
     *
     * SRC: https://www.leveluplunch.com/java/examples/java-util-function-predicate-example/
     */
    private static Predicate<ConnFile> isSubUnitEmptyClearOrDecrypted =
            ((Predicate<ConnFile>) ConnFile::isEmptyClear).or(ConnFile::isEmptyDecrypted);

    /**
     * Compare subUnit (ConnFile) subUnit (Conn) password
     * against String password
     */
    private static BiPredicate<ConnFile, String> isConnPasswordOk = (ConnFile connFile, String password) -> {
        try {

            Crypto crypto = new Crypto(password);
            // If no exception thrown by line below, means that password is correct.
            // If first element (Conn (get(0))) of the list returned by loadConnsFromSubUnit
            // method is encrypted, also all others are (with same password).
            crypto.getDecrypted(loadConnsFromSubUnit(connFile).get(0).getPassword());

            return true;

        } catch (Exception e) {

            //e.printStackTrace();

        }
        return false;
    };

    /**
     * Compare subUnit (ConnFile) password
     * against String password
     */
    private static BiPredicate<ConnFile, String> isConnFilePasswordOk =
            (ConnFile connFile, String password) -> password.equals(connFile.getPassword());

    /**
     * Set broken if OS file nonexistent or directory or damaged
     */
    private static Consumer<ConnFile> setBrokenIfFileNotExistOrDirectoryOrDamaged = subUnit -> {
        if(!isFileInSubUnitOk.and(isFileInSubUnitNotDamaged).test(subUnit)) subUnit.setBroken();
    };

    /**
     * Set encrypted if OS file password field is encrypted
     */
    private static Consumer<ConnFile> setEncryptedIfFilePasswordIsEncrypted = subUnit -> {
        List<Conn> listConn = loadConnsFromSubUnit(subUnit);

        // If first element (Conn) of the list is encrypted, also all others are (with same password)
        if (listConn != null && listConn.get(0).isPasswordEncrypted()) subUnit.setEncrypted();
    };

    /**
     * Set clear if not encrypted
     */
    private static Consumer<ConnFile> setClearIfNotEncrypted = subUnit -> {
        if (!subUnit.isEncrypted()) subUnit.setClear();
    };

    /**
     * Set encrypted if encrypted, clear else
     */
    private static Consumer<ConnFile> setEncryptedOrClear = subUnit -> {
        // SRC: http://www.java2s.com/Tutorials/Java/java.util.function/Consumer/1040__Consumer.andThen.htm
        setEncryptedIfFilePasswordIsEncrypted.andThen(setClearIfNotEncrypted).accept(subUnit);
    };

    /**
     * Set decrypted if password (popup) ok
     */
    private static Consumer<ConnFile> setDecryptedIfPasswordOk = subUnit -> {

        String password = getSubUnitPassword(subUnit, isConnPasswordOk);

        setPasswordAndState(subUnit,
                password,
                password != null ? ConnFileState.DECRYPTED : subUnit.getState());
    };

    /**
     * Set decrypted passwords to each Conn in ConnFile
     */
    private static Consumer<ConnFile> setDecryptedPasswords = subUnit -> {
        subUnit.getSubUnits().forEach(conn -> {
            Crypto crypto = new Crypto(conn.getParent().getPassword());
            conn.setPassword(crypto.getDecrypted(conn.getPassword()));
            conn.setPasswordEncrypted(false);
        });
    };

    /**
     * Output subUnit (ConnFile) name and fileName (path) fields
     * in format "name - fileName" (name, fileName entries
     * separated by a space, a hyphen and a space)
     *
     * SRC: http://www.studytrails.com/java/java8/Java8_Lambdas_FunctionalProgramming/
     */
    private static Function<ConnFile, String> nameFileNameToString = subUnit -> StringUtil.nameFileNameToString(subUnit);

    // #####################################################################################

    // Constructors ########################################################################

    /**
     * Constructor.
     * @param name
     * @param subUnits
     */
    public ConnRoot(String name, ObservableList<ConnFile> subUnits) {
        super(name, subUnits, ConnFile::new, ICON_FILENAME);

        // State
        state = new SimpleObjectProperty<>(ConnRootState.QUITE);

        // Update icon when state change
        stateProperty().addListener((observable, oldvalue, newvalue) -> {
            ImageView icon = new ImageView(newvalue.getIconFileName());
            icon.setFitHeight(ICON_HEIGHT);
            icon.setFitWidth(ICON_WIDTH);
            setIcon(icon);
        });

        ContextMenu contextMenu = new ContextMenu();

        MenuItem newFileMenuItem = I18N.menuItemForKey("connRoot.contextMenu.file.new");
        newFileMenuItem.setOnAction(newConnFile());

        MenuItem openFileMenuItem = I18N.menuItemForKey("connRoot.contextMenu.file.open");
        openFileMenuItem.setOnAction((ActionEvent t) -> getRootLayoutController().openConnFile());

        contextMenu.getItems().addAll(newFileMenuItem, openFileMenuItem);
        this.setMenu(contextMenu);
    }

    /**
     * Constructor.
     * @param name
     */
    public ConnRoot(String name) {
        this(name, FXCollections.observableArrayList());
    }

    /**
     * Constructor.
     * @param name
     * @param stage
     * @param rootLayoutController
     */
    public ConnRoot(String name, Stage stage, RootLayoutController rootLayoutController) {
        this(name, FXCollections.observableArrayList());
        this.stage = stage;
        this.rootLayoutController = rootLayoutController;

        // Load user preferences keys
        loadPrefKeys();
        // Create subUnits from preferences keys
        createSubUnitsFromPrefKeys();
        // Set subUnits state
        if (!subUnits.isEmpty()) subUnits.forEach(subUnit -> setSubUnitState(subUnit));

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

    public String getPasswordToCheck() {
        return passwordToCheck;
    }

    /**
     * subUnits contains at least one broken file
     * and user preference to warn at start is set
     *
     * @return Boolean.TRUE if subUnits has broken,
     * Boolean.FALSE otherwise
     */
    public Supplier<Boolean> hasBrokenAndIsPref() {
        return () -> hasSubUnit(ConnFile::isBroken) && Pref.isErrorLoadingFilePopUpAtStartOrOnOpen();
    }

    /**
     * subUnits contains at least one encrypted file
     * and user preference to ask pass at start is set
     *
     * @return Boolean.TRUE if subUnits has encrypted,
     * Boolean.FALSE otherwise
     */
    public Supplier<Boolean> hasEncryptedAndIsPref() {
        return () -> hasSubUnit(ConnFile::isEncrypted) && Pref.isDecryptFilePassPopUpAtStartOrOnOpen();
    }

    /**
     * subUnits contains at least one empty file, clear or decrypted
     * and user preference to alert for empty files on exit is set
     *
     * @return Boolean.TRUE if subUnits has empty clear/decrypted,
     * Boolean.FALSE otherwise
     */
    public Supplier<Boolean> hasEmptyAndIsPref() {
        return () -> (hasSubUnit(ConnFile::isEmptyClear) || hasSubUnit(ConnFile::isEmptyDecrypted)) && Pref.isWarnRemoveEmptyFileOnClose();
    }

    public ConnRootState getState() {
        return state.get();
    }

    public ObjectProperty<ConnRootState> stateProperty() {
        return state;
    }

    private void setState(ConnRootState state) {
        this.state.set(state);
    }

    public static Function<ConnFile, String> getNameFileNameToString() {
        return nameFileNameToString;
    }

    // #####################################################################################

    // Methods #############################################################################

    /**
     * Test QUITE state
     * @return true on QUITE state, false otherwise
     */
    public boolean isQuite() {
        return state.getValue().equals(ConnRootState.QUITE);
    }

    /**
     * Set to QUITE state
     */
    public void setQuite() {
        setState(ConnRootState.QUITE);  // ! Order matter (listener): 1st state, 2nd name !
        this.nameProperty().unbind();
        // set treeView denomination to default value
        this.nameProperty().bind(I18N.createStringBinding("network.network"));
    }

    /**
     * Test OPENING_FILE state
     * @return true on OPENING_FILE state, false otherwise
     */
    public boolean isOpeningFile() {
        return state.getValue().equals(ConnRootState.OPENING_FILE);
    }

    /**
     * Set to OPENING_FILE state
     */
    public void setOpeningFile() {
        setState(ConnRootState.OPENING_FILE);   // ! Order matter (listener): 1st state, 2nd name !
        this.nameProperty().unbind();
        // set treeView denomination to waiting message value
        this.nameProperty().bind(I18N.createStringBinding("network.openingFile"));
    }

    /**
     * Create new ConnRoot treeView entry (ConnFile instance)
     *
     * @param name
     * @param fileName
     * @param rootLayoutController
     */
    public void createSubUnit(String name, String fileName, RootLayoutController rootLayoutController) {

        ConnFile connFile = new ConnFile(name, fileName, this, rootLayoutController);
        getSubUnits().add(connFile);

        treatSubUnit(connFile);

    }

    /**
     * SubUnit (ConnFile) common treatment
     *
     * @param connFile
     */
    public void treatSubUnit(ConnFile connFile) {

        setSubUnitState(connFile);

        if (connFile.isBroken() && Pref.isErrorLoadingFilePopUpAtStartOrOnOpen()) alertLoadFile(connFile);
        if (connFile.isEncrypted() && Pref.isDecryptFilePassPopUpAtStartOrOnOpen()) setDecryptedIfPasswordOk.accept(connFile);

        populateSubUnit(connFile);
        // update preference
        prefs.put(connFile.getName(), connFile.getFileName());

    }

    /**
     * Set subUnit (ConnFile instance) state
     *
     * Process:
     * ========
     *
     * - 1. Set nonexistent file to broken
     * - 2. Set password protected file to encrypted
     */
    private void setSubUnitState(ConnFile connFile) {

        setBrokenIfFileNotExistOrDirectoryOrDamaged.accept(connFile);

        if (!connFile.isBroken()) setEncryptedOrClear.accept(connFile);

        /*
        setSubUnits(getSubUnitsSubList(cf -> true), setBrokenIfFileNotExistOrDirectoryOrDamaged);
        setSubUnits(getSubUnitsSubList(((Predicate<ConnFile>) ConnFile::isBroken).negate()), setEncryptedOrClear);
        */

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
     * Create ConnRoot Sub Units (ConnFile)
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
     * Set list to consumer function.
     *
     * @param list
     * @param consumer
     */
    private void setSubUnits(List<ConnFile> list, Consumer<ConnFile> consumer) {
        list.forEach(subUnit -> consumer.accept(subUnit));
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
     * (cf. nameFileNameToString function), and each entry is separated by a new line.
     *
     * SRC: https://ivarconr.wordpress.com/2013/11/20/java-8-joining-strings-with-stream-api/
     *
     * @param subUnits
     * @return string in format name - fileName (path) \n
     */
    private String namesFileNamesToString(List<ConnFile> subUnits) {
        return subUnits
                .stream()
                .map(subUnit -> nameFileNameToString.apply(subUnit))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Alert popup dialog to inform user
     * with broken file.
     */
    public void alertLoadFile(ConnFile connFile) {

        AlertDialog.provide(stage,
                Alert.AlertType.ERROR,
                MainApp.TEXT_BUNDLE.getString("connRoot.alerrLoadFile.title"),
                MainApp.TEXT_BUNDLE.getString("connRoot.alerrLoadFile.header"),
                MainApp.TEXT_BUNDLE.getString("connRoot.alerrLoadFile.content")
                        + "\n" + nameFileNameToString.apply(connFile), true);

    }

    /**
     * Alert popup dialog to inform user
     * with broken files.
     */
    public void alertLoadFiles() {

        AlertDialog.provide(stage,
                Alert.AlertType.ERROR,
                MainApp.TEXT_BUNDLE.getString("connRoot.alerrLoadFiles.title"),
                MainApp.TEXT_BUNDLE.getString("connRoot.alerrLoadFiles.header"),
                MainApp.TEXT_BUNDLE.getString("connRoot.alerrLoadFiles.content")
                        + "\n" + namesFileNamesToString(getSubUnitsSubList(ConnFile::isBroken)), true);

    }

    /**
     * Alert popup dialog to inform user
     * with empty clear/decrypted files.
     *
     * @return true if it's ok to leave application
     * with empty file(s) knowing that they will
     * be removed from list,
     * false for getting back to application.
     */
    public boolean alertEmptyFiles() {

        //TODO empty file warning dialog with don't warn again check box in lower left corner (-> set user pref), (nice to have).

        Alert alert = AlertDialog.provide(stage,
                Alert.AlertType.CONFIRMATION,
                MainApp.TEXT_BUNDLE.getString("alcnfEmptyFile.title"),
                MainApp.TEXT_BUNDLE.getString("alcnfEmptyFile.header"),
                MainApp.TEXT_BUNDLE.getString("alcnfEmptyFile.content") + "\n" + namesFileNamesToString(getSubUnitsSubList(isSubUnitEmptyClearOrDecrypted)), false);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            // ... user chose OK
            return true;
        } else {
            // ... user chose CANCEL or closed the dialog
        }

        return false;

    }

    /**
     * Remove empty files.
     */
    public void removeEmptyFiles() {
        getSubUnitsSubList(isSubUnitEmptyClearOrDecrypted).forEach(ConnFile::closeConnFile);
    }

    /**
     * Loads list of connections (Conn) data from the specified
     * subUnit (ConnFile) instance parameter, unmarshaled with JAXB.
     *
     * @param connFile
     * @return List of Conn instances
     */
    private static List<Conn> loadConnsFromSubUnit(ConnFile connFile) {

        File file = new java.io.File(connFile.getFileName());

        /*
        File fileSource = new java.io.File(connFile.getFileName());

        // SRC: http://www.javapractices.com/topic/TopicAction.do?Id=62
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(1000000);

        String fileExt = ConnFile.FILE_EXT;
        String tmpFileName = connFile.getFileName().replace(fileExt, "");

        File fileDest = new java.io.File(tmpFileName + "-tmp-" + randomInt + fileExt);
        */

        // debug mode
        //System.out.println(file.toString());

        try {

            //copyFileUsingStream(fileSource, fileDest);

            //InputStream inputStream = new FileInputStream(connFile.getFileName());

            JAXBContext context = JAXBContext.newInstance(ConnListWrapper.class);

            // debug mode
            //System.out.println(context.toString());

            Unmarshaller um = context.createUnmarshaller();

            // Reading XML from the file and unmarshalling.
            ConnListWrapper wrapper = (ConnListWrapper) um.unmarshal(file);
            //ConnListWrapper wrapper = (ConnListWrapper) um.unmarshal(fileDest);
            //ConnListWrapper wrapper = (ConnListWrapper) um.unmarshal(inputStream);

            // debug mode
            /*
            for (Conn conn: wrapper.getConns()) {
                System.out.println(conn);
            }
            */

            //inputStream.close();

            //fileDest.delete();

            return wrapper.getConns();

        } catch (Exception e) { // catches ANY exception

            //e.printStackTrace();

            return null;

        }

    }

    /**
     * Copy file.
     * SRC: http://www.journaldev.com/861/java-copy-file
     *
     * @param source
     * @param dest
     * @throws IOException
     */
    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
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
        return hasBrokenAndIsPref().get() || hasEncryptedAndIsPref().get();
    }

    /**
     * Decrypt subUnits passwords with user password.
     */
    public void decryptPasswords() {
        setSubUnits(getSubUnitsSubList(ConnFile::isEncrypted), setDecryptedIfPasswordOk);
    }

    /**
     * Decrypt subUnit password with user password.
     *
     * @return this (ConnRoot instance) to allow method chaining
     * SRC: http://stackoverflow.com/questions/21180269/how-to-achieve-method-chaining-in-java
     */
    public ConnRoot decryptPassword(ConnFile connFile) {
        if (connFile.isEncrypted()) setDecryptedIfPasswordOk.accept(connFile);
        return this;
    }

    /**
     * Ask user for password in a dialog.
     * This password is used at subUnit (ConnFile) level
     * to encrypt/decrypt connection (Conn) password
     *
     * @param connFile
     * @param passwordOkBP
     * @return password if correct, null otherwise
     */
    private static String getSubUnitPassword(ConnFile connFile, BiPredicate<ConnFile, String> passwordOkBP) {

        //TODO When TAB key is pressed to change focus on Cancel button and hit ENTER key
        //Bad password popup is displayed even if password field was let empty and hit enter key (not click)
        // on cancel button, (OK button (which is highlighted) like behavior).

        boolean tryAgain;

        do {
            PasswordDialog passwordDialog = new PasswordDialog(nameFileNameToString.apply(connFile));
            Optional<String> passwordDialogResult = passwordDialog.showAndWait();
            // debug mode
            //result.ifPresent(password -> System.out.println(password));

            if (passwordDialogResult.isPresent()) {
                String password = passwordDialogResult.get();

                if (passwordOkBP.test(connFile, password)) return password;
                else {
                    Alert alert = AlertDialog.provide(stage,
                            Alert.AlertType.CONFIRMATION,
                            MainApp.TEXT_BUNDLE.getString("alcnfBadPassword.title"),
                            MainApp.TEXT_BUNDLE.getString("alcnfBadPassword.header"),
                            MainApp.TEXT_BUNDLE.getString("alcnfBadPassword.content"), false);

                    Optional<ButtonType> badPasswordDialogResult = alert.showAndWait();

                    if (badPasswordDialogResult.get() == ButtonType.OK) {
                        // ... user chose OK
                        tryAgain = true;

                    } else {
                        // ... user chose CANCEL or closed the dialog
                        tryAgain = false;
                    }
                }

            } else tryAgain = false;

        } while (tryAgain);

        return null;

    }

    /**
     * Set subUnit (ConnFile instance) password
     *
     * @param connFile
     */
    public void setPassword(ConnFile connFile) {
        ConnFilePasswordContainerEditor.INSTANCE.supply(connFile, MainApp.TEXT_BUNDLE.getString("connFilePasswordContainerDialog.title.set"));
    }

    /**
     * Set password and state
     *
     * @param connFile
     * @param password
     * @param connFileState
     */
    private static void setPasswordAndState(ConnFile connFile, String password, ConnFileState connFileState) {

        connFile.setPasswordProtected(password != null);
        connFile.setPassword(password);
        if (connFileState != null) connFile.setState(connFileState);

    }

    /**
     * Lock subUnit (ConnFile instance) password
     *
     * @param connFile
     */
    public void lockPassword(ConnFile connFile) {
        setPasswordAndState(connFile, null, ConnFileState.ENCRYPTED);
        connFile.getSubUnits().clear();
    }

    /**
     * Change subUnit (ConnFile instance) password
     *
     * @param connFile
     */
    public void changePassword(ConnFile connFile) {

        boolean askForPassword = true;

        switch (connFile.getState()) {

            case ENCRYPTED:

                passwordCommon(new ConnFile(connFile),
                        isConnPasswordOk,
                        ConnFileState.DECRYPTED,
                        true,
                        setDecryptedPasswords,
                        ConnFileState.CLEAR,
                        askForPassword,
                        !askForPassword);

                break;

            case EMPTY_DECRYPTED:

                passwordCommon(connFile,
                        isConnFilePasswordOk,
                        null,
                        false,
                        null,
                        null,
                        askForPassword,
                        !askForPassword);

                break;

            case DECRYPTED:

                passwordCommon(connFile,
                        isConnFilePasswordOk,
                        null,
                        false,
                        setDecryptedPasswords,
                        null,
                        askForPassword,
                        !askForPassword);

                break;

            default:
                //
                break;
        }

    }

    /**
     * Password common treatment
     *
     * @param connFile
     * @param passwordOkBP
     * @param anteState
     * @param doPopulateSubUnit
     * @param coreTreatmentCS
     * @param postState
     * @param askForPassword
     * @param saveConnDataToFile
     */
    private void passwordCommon(ConnFile connFile,
                                BiPredicate<ConnFile, String> passwordOkBP,
                                ConnFileState anteState,
                                boolean doPopulateSubUnit,
                                Consumer<ConnFile> coreTreatmentCS,
                                ConnFileState postState,
                                boolean askForPassword,
                                boolean saveConnDataToFile) {

        passwordToCheck = getSubUnitPassword(connFile, passwordOkBP);

        //OLD
        //if (anteState != null) setPasswordAndState(connFile, passwordToCheck, anteState);

        if (passwordToCheck != null) {

            //NEW
            if (anteState != null) setPasswordAndState(connFile, passwordToCheck, anteState);

            if (doPopulateSubUnit) populateSubUnit(connFile);

            if (coreTreatmentCS != null) coreTreatmentCS.accept(connFile);

            setPasswordAndState(connFile, null, postState);

            if (askForPassword) ConnFilePasswordContainerEditor.INSTANCE.supply(connFile, MainApp.TEXT_BUNDLE.getString("connFilePasswordContainerDialog.title.change"));
            else if (saveConnDataToFile) connFile.saveConnDataToFile();

            passwordToCheck = null;
        }

    }

    /**
     * Remove subUnit (ConnFile instance) password
     *
     * @param connFile
     */
    public void removePassword(ConnFile connFile) {

        boolean askForPassword = false;

        switch (connFile.getState()) {

            case ENCRYPTED:

                passwordCommon(connFile,
                        isConnPasswordOk,
                        ConnFileState.CLEAR,
                        true,
                        setDecryptedPasswords,
                        null,
                        askForPassword,
                        !askForPassword);

                break;

            case EMPTY_DECRYPTED:

                passwordCommon(connFile,
                        isConnFilePasswordOk,
                        ConnFileState.EMPTY_CLEAR,
                        false,
                        null,
                        null,
                        askForPassword,
                        false);

                break;

            case DECRYPTED:

                passwordCommon(connFile,
                        isConnFilePasswordOk,
                        ConnFileState.CLEAR,
                        false,
                        setDecryptedPasswords,
                        null,
                        askForPassword,
                        !askForPassword);

                break;

            default:
                //
                break;
        }

    }

    //TODO refactor (at least, review (name newConnFile vs newSubUnit?)): newConnFile
    /**
     * New ConnFile object.
     * Create new File entry to Connections treeView.
     */
    public EventHandler<ActionEvent> newConnFile() {
        return event -> {
            ConnFile connFile = new ConnFile(CONNFILE_DEFAULT_NAME);
            connFile.setRootLayoutController(rootLayoutController);
            if (ConnFileEditor.INSTANCE.supply(connFile)) {
                if (connFile.isPasswordProtected()) connFile.setEmptyDecrypted();
                connFile.setParent(this);
                getSubUnits().add(connFile);
                connFile.setContextMenu();
                prefs.put(connFile.getName(), connFile.getFileName());
            }
        };
    }

    //TODO refactor (at least, review): closeConnFile
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

    //TODO refactor (at least, review): sortSubUnits
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
        rootLayoutController.getConnTreeView().getRoot().getChildren().forEach((subUnit) -> {
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

            //rootLayoutController.getConnTreeView().getRoot().getChildren().sort(Comparator.comparing((TreeItem<? super ConnFile> cf1) -> ((ConnFile)(cf1.getValue())).getName(),Comparator.naturalOrder()));

            Comparator<TreeItem<? super ConnFile>> connFileComparator =
                    (cf1, cf2) -> ((ConnFile)(cf1.getValue())).getName().compareTo(((ConnFile)(cf2.getValue())).getName());

            rootLayoutController.getConnTreeView().getRoot().getChildren().sort(connFileComparator);

            /*
            rootLayoutController.getConnTreeView().getRoot().getChildren()
                    .sort((TreeItem<? super ConnFile> cf1, TreeItem<? super ConnFile> cf2) ->
                            ((ConnFile)(cf1.getValue())).getName().compareTo(((ConnFile)(cf2.getValue())).getName()));
            */

            //rootLayoutController.getConnTreeView().refresh();
        } catch (StackOverflowError e) {
            System.err.println("The method ConnRoot.sortSubUnits() raise a StackOverflowError exception!");
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
        rootLayoutController.getConnTreeView().getRoot().getChildren().forEach((subUnit) -> {
            System.out.println("subUnit2: " + subUnit);
        });
        System.out.println("/////////");
        */
    }

    //TODO refactor (at least, review): sortSubUnitsOnChangeListener
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

    /**
     * Populate subUnits
     */
    public void populateSubUnits() {
        subUnits.forEach(connFile -> populateSubUnit(connFile));
    }

    /**
     * Populate subUnit
     *
     * @param subUnit
     */
    public void populateSubUnit(ConnFile subUnit) {
        if (isFileInSubUnitReadyToPopulate.test(subUnit)) {
            List<Conn> list = loadConnsFromSubUnit(subUnit);

            if (list != null) {
                // Set Conn object reference to his ConnFile parent object
                list.forEach(conn -> conn.setParent(subUnit));
                subUnit.getSubUnits().addAll(list);
            }
        }
    }

    // #####################################################################################

}
