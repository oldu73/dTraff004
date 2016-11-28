package ch.stageconcept.dtraff.connection.tree.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;

public class File extends ConnectionUnit<Connection> {

   public File(String name, ObservableList<Connection> subUnits) {
        super(name, subUnits, Connection::new);

       this.setIcon(new ImageView("file001.png"));

       ContextMenu contextMenu = new ContextMenu();
       MenuItem newConnectionMenuItem = new MenuItem("New Connection");
       newConnectionMenuItem.setOnAction((ActionEvent t) -> {
           System.out.println("New Connection");
       });
       contextMenu.getItems().add(newConnectionMenuItem);
       this.setMenu(contextMenu);
    }

    public File(String name) {
        this(name, FXCollections.observableArrayList());
    }

}
