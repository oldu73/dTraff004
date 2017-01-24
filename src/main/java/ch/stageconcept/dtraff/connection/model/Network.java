package ch.stageconcept.dtraff.connection.model;

import ch.stageconcept.dtraff.connection.util.ConnFileEditor;
import ch.stageconcept.dtraff.connection.util.ConnFileState;
import ch.stageconcept.dtraff.connection.util.ConnListWrapper;
import ch.stageconcept.dtraff.main.view.RootLayoutController;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import org.codefx.libfx.listener.handle.ListenerHandle;
import org.codefx.libfx.listener.handle.ListenerHandles;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

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

    private Stage stage;
    private RootLayoutController rootLayoutController;

    // User preferences (file node)
    private Preferences prefs = Preferences.userRoot().node(PREFS_PATH);
    private String[] prefKeys = null;

    private ListenerHandle subUnitsListenerHandle;

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

        create();
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
     * Create network description in a tree data structure,
     * to be used in a treeView : Network (root node) - ConnFile - Conn - Database - (...)
     *
     * @return true if user interaction (through popup (relative to user preferences))
     * may be necessary either to confirm error loading data from file or enter password
     * for encrypted file (use case example: display of Initializing... message in center of RootLayout at start),
     * false otherwise.
     */
    public boolean create() {
        // True if popup may be raised
        boolean askUserToAct = false;

        loadPrefKeys();
        createSubUnitsFromPrefKeys();

        if (!subUnits.isEmpty()) {
            checkSubUnitsBrokenFiles();
            checkSubUnitsEncryptedFiles();

        }

        return askUserToAct;
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
    private void checkSubUnitsBrokenFiles() {

        subUnits.forEach(subUnit -> {
            File file = new File(subUnit.getFileName());
            if(!file.exists() || file.isDirectory()) {
                subUnit.setBroken();
            }
        });

    }

    /**
     * Iterate through Sub Units (ConnFiles) to check if corresponding OS file is encrypted
     * (has an encrypted password field) if so, set ConnFile instance state to ENCRYPTED.
     */
    private void checkSubUnitsEncryptedFiles() {

        subUnits.stream().filter(subUnit -> !subUnit.isBroken()).forEach(subUnit -> {

            List<Conn> listConn = loadConnFromConnFile(subUnit);
            // debug mode
            //System.out.println("String to decrypt: " + listConn.get(0).getPassword());

            // If first element (Conn) of the list is encrypted, also all others are (with same password)
            if (listConn != null && listConn.get(0).isPasswordEncrypted()) subUnit.setEncrypted();

        });

    }

    /**
     * Loads connections (Conn) data from the specified ConnFile
     * instance parameter, unmarshaled with JAXB.
     *
     * @param connFile
     * @return List of Conn instances
     */
    public List<Conn> loadConnFromConnFile(ConnFile connFile) {

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
