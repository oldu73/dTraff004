package ch.stageconcept.dtraff.connection.model;

import ch.stageconcept.dtraff.connection.util.FileEditor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import java.util.prefs.Preferences;

//TODO javadoc

public class Network extends ConnectionUnit<File> {

    private static final String ICON_FILENAME = "network001.gif";

    private Preferences preferences;
    public static final String PREFS_PATH = "/ch/stageconcept/datatraffic/file";

    public Network(String name, ObservableList<File> subUnits) {
        super(name, subUnits, File::new, ICON_FILENAME);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem newFileMenuItem = new MenuItem("New File");
        newFileMenuItem.setOnAction((ActionEvent t) -> {
            File file = new File("default");
            if (FileEditor.INSTANCE.supply(file)) {
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

}
