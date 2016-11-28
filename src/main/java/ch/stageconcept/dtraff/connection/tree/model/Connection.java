package ch.stageconcept.dtraff.connection.tree.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.ImageView;

public class Connection extends ConnectionUnit<DataBase> {

    public Connection(String name, ObservableList<DataBase> subUnits) {
        super(name, subUnits, DataBase::new);

        this.setIcon(new ImageView("serverDefault001.gif"));
    }

   public Connection(String name) {
        this(name, FXCollections.observableArrayList());
    }

}
