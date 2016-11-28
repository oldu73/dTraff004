package ch.stageconcept.dtraff.connection.tree.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;

public class Network extends ConnectionUnit<File> {

    public Network(String name, ObservableList<File> subUnits) {
        super(name, subUnits, File::new);

        this.setIcon(new ImageView("network001.gif"));

        ContextMenu contextMenu = new ContextMenu();
        MenuItem newFileMenuItem = new MenuItem("New File");
        newFileMenuItem.setOnAction((ActionEvent t) -> {
            System.out.println("New File");
        });
        contextMenu.getItems().add(newFileMenuItem);
        this.setMenu(contextMenu);
    }

    public Network(String name) {
        this(name, FXCollections.observableArrayList());
    }

}
