package ch.stageconcept.dtraff.connection.model;

import ch.stageconcept.dtraff.connection.util.ConnFileEditor;
import ch.stageconcept.dtraff.connection.util.ConnFileState;
import ch.stageconcept.dtraff.main.view.RootLayoutController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.codefx.libfx.listener.handle.ListenerHandle;
import org.codefx.libfx.listener.handle.ListenerHandles;

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
    private Preferences preferences = Preferences.userRoot().node(PREFS_PATH);

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
        try {
            getSubUnits().sort((ConnFile cf1, ConnFile cf2) -> cf1.getName().compareTo(cf2.getName()));
        } catch (StackOverflowError e) {
            System.err.println("The method Network.sortSubUnits() raise a StackOverflowError exception!");
        }
    }

    /**
     * Sort subUnits on list change.
     */
    public void sortSubUnitsOnChangeListener() {

        //TODO Post a 2slash.ch article on methodology once working

        //TODO check intensively open/close file, it have now suspect behavior

        // SRC: https://www.javacodegeeks.com/2015/01/dont-remove-listeners-use-listenerhandles.html
        // SRC: http://libfx.codefx.org/
        // SRC: https://github.com/CodeFX-org/LibFX/wiki/ListenerHandle

        /*
        ChangeListener<? extends ObservableList<ConnFile>> listener = (ChangeListener<ObservableList<ConnFile>>) (observable, oldValue, newValue) -> {
            System.out.println("changed " + oldValue + "->" + newValue);
            //myFunc();
        };
        */

        ListChangeListener<? super ConnFile> listener = (ListChangeListener<ConnFile>) (change) -> {
            System.out.println(change);
            //myFunc();
        };

        //getSubUnits().addListener(listener);

        // this immediately adds the listener to the property
        subUnitsListenerHandle = ListenerHandles.createAttached(getSubUnits(), listener);


        /*
        // SRC: http://java.developpez.com/faq/javafx/?page=Collections-observables
        getSubUnits().addListener((ListChangeListener.Change<? extends ConnFile> change) -> {
            // debug mode
            System.out.println(change);
            sortSubUnits();
        });
        */
    }

}
