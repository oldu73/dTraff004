package ch.stageconcept.dtraff.connection.model;

import ch.stageconcept.dtraff.connection.util.ConnFileEditor;
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

    public Network(String name, ObservableList<ConnFile> subUnits) {
        super(name, subUnits, ConnFile::new, ICON_FILENAME);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem newFileMenuItem = new MenuItem("New File");
        newFileMenuItem.setOnAction((ActionEvent t) -> {
            ConnFile file = new ConnFile("default");
            if (ConnFileEditor.INSTANCE.supply(file)) {
                if (file.isPasswordProtected()) {
                    file.setIcon(new ImageView("fileUnLock001.png"));
                }
                file.setParent(this);
                subUnits.add(file);
                Preferences preferences = Preferences.userRoot().node(PREFS_PATH);
                preferences.put(file.getName(), file.getFileName());
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
