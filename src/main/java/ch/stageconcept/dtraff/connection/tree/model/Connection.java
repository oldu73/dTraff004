package ch.stageconcept.dtraff.connection.tree.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

//TODO javadoc

public class Connection extends ConnectionUnit<DataBase> {

    private static final String ICON_FILENAME = "serverDefault001.gif";

    public Connection(String name, ObservableList<DataBase> subUnits) {
        super(name, subUnits, DataBase::new, ICON_FILENAME);
    }

   public Connection(String name) {
        this(name, FXCollections.observableArrayList());
    }

}
