package ch.stageconcept.dtraff.connection.model;

import ch.stageconcept.dtraff.connection.util.ConnFileEditor;
import ch.stageconcept.dtraff.connection.util.ConnFileState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;

import java.util.prefs.Preferences;

//TODO javadoc

public class Network extends ConnUnit<ConnFile> {

    private static final String ICON_FILENAME = "network001.png";
    public static final String PREFS_PATH = "/ch/stageconcept/datatraffic/file";
    private static final String MENU_NEW_FILE = "New File";
    private static final String CONNFILE_DEFAULT_NAME = "default";

    public Network(String name, ObservableList<ConnFile> subUnits) {
        super(name, subUnits, ConnFile::new, ICON_FILENAME);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem newFileMenuItem = new MenuItem(MENU_NEW_FILE);
        newFileMenuItem.setOnAction((ActionEvent t) -> {
            ConnFile connFile = new ConnFile(CONNFILE_DEFAULT_NAME);
            if (ConnFileEditor.INSTANCE.supply(connFile)) {
                if (connFile.isPasswordProtected()) {
                    connFile.setState(ConnFileState.DECRYPTED);
                }
                connFile.setParent(this);
                subUnits.add(connFile);
                Preferences preferences = Preferences.userRoot().node(PREFS_PATH);
                preferences.put(connFile.getName(), connFile.getFileName());
            }
        });
        contextMenu.getItems().add(newFileMenuItem);
        this.setMenu(contextMenu);
    }

    public Network(String name) {
        this(name, FXCollections.observableArrayList());
    }

    public void closeFile(ConnFile connFile) {
        Preferences preferences = Preferences.userRoot().node(PREFS_PATH);
        preferences.remove(connFile.getName());
        this.getSubUnits().remove(connFile);
    }

}
