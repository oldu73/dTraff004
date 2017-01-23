package ch.stageconcept.dtraff.connection.model;

import ch.stageconcept.dtraff.connection.util.ConnFileEditor;
import ch.stageconcept.dtraff.connection.util.ConnFileState;
import ch.stageconcept.dtraff.main.view.RootLayoutController;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import org.codefx.libfx.listener.handle.ListenerHandle;
import org.codefx.libfx.listener.handle.ListenerHandles;

import java.io.File;
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

    private static final String ICON_FILENAME = "network001.png";
    public static final String PREFS_PATH = "/ch/stageconcept/datatraffic/file";
    private static final String MENU_NEW_FILE = "New File";
    private static final String MENU_OPEN_FILE = ConnFile.MENU_OPEN_FILE;
    private static final String CONNFILE_DEFAULT_NAME = "default";

    private RootLayoutController rootLayoutController;

    // User preferences (file node)
    private Preferences preferences = Preferences.userRoot().node(PREFS_PATH);
    private String[] prefKeys = null;

    private ListenerHandle subUnitsListenerHandle;

    /**
     * Constructor
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
     * Constructor
     * @param name
     */
    public Network(String name) {
        this(name, FXCollections.observableArrayList());
    }

    /**
     * rootLayoutController setter
     * @param rootLayoutController
     */
    public void setRootLayoutController(RootLayoutController rootLayoutController) {
        this.rootLayoutController = rootLayoutController;
    }

    /**
     * rootLayoutController getter
     * @return rootLayoutController
     */
    public RootLayoutController getRootLayoutController() {
        return rootLayoutController;
    }

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

        if (isLoadPrefKeysOk()) {
            createSubUnitsFromPrefKeys();
            askUserToAct = !areSubUnitsFilesOk();

        }

        return askUserToAct;
    }

    /**
     * Load user preferences from BackingStore
     * to field prefKeys (String array).
     *
     * @return true if load is ok,
     * false otherwise.
     */
    private boolean isLoadPrefKeysOk() {

        if (preferences != null) {
            try {
                prefKeys = preferences.keys();
                return true;
            } catch (BackingStoreException e) {
                //System.err.println("unable to read backing store: " + e);
                //e.printStackTrace();
            } catch (IllegalStateException e) {
                //System.err.println("..., " + e);
                //System.out.println("node has been removed!");
                //e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Create Network Sub Units (ConnFile)
     * from prefKeys field.
     */
    private void createSubUnitsFromPrefKeys() {
        if (prefKeys != null) {

            for (String prefKey : prefKeys) {
                ConnFile connFile = new ConnFile(prefKey);

                String fileName = preferences.get(prefKey, null);

                if (fileName != null) {
                    connFile.setFileName(fileName);

                    connFile.setParent(this);
                    connFile.setRootLayoutController(rootLayoutController);
                    getSubUnits().add(connFile);
                }
            }

        }
    }

    private boolean areSubUnitsFilesOk() {

        return false;
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
            preferences.put(connFile.getName(), connFile.getFileName());
        }
    }

    /**
     * Close ConnFile object.
     * Remove File entry from Connections treeView.
     *
     * @param connFile
     */
    public void closeConnFile(ConnFile connFile) {
        preferences.remove(connFile.getName());
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

}
