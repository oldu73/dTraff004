package ch.stageconcept.dtraff.connection.tree.model;

import ch.stageconcept.dtraff.connection.tree.util.ConnectionEditor;
import ch.stageconcept.dtraff.connection.unit.util.DbType;
import ch.stageconcept.dtraff.connection.unit.view.ConnectionEditDialogController;
import ch.stageconcept.dtraff.main.MainApp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

//TODO javadoc

public class File extends ConnectionUnit<Connection> {

    private static final String ICON_FILENAME = "file001.png";

   public File(String name, ObservableList<Connection> subUnits) {
       super(name, subUnits, Connection::new, ICON_FILENAME);

       // treeView context menu
       ContextMenu contextMenu = new ContextMenu();
       MenuItem newConnectionMenuItem = new MenuItem("New Connection");
       newConnectionMenuItem.setOnAction((ActionEvent t) -> {
           //System.out.println("New Connection on:" + this.getName());

           // new Connection instance with default name value
           Connection connection = new Connection(DbType.INSTANCE.getDbDescriptorMap().get(DbType.MYSQL_KEY).getName());

           if (ConnectionEditor.INSTANCE.supply(connection)) subUnits.add(connection);

           //this.createAndAddSubUnit("Hello, world!");
       });
       contextMenu.getItems().add(newConnectionMenuItem);
       this.setMenu(contextMenu);
   }

   public File(String name) {
       this(name, FXCollections.observableArrayList());
   }

}
