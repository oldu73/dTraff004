package ch.stageconcept.dtraff.connection.tree.model;

import javafx.collections.FXCollections;

//TODO javadoc

public class DataBase extends ConnectionUnit<ConnectionUnit<?>> {

    private static final String ICON_FILENAME = "database001.png";

    public DataBase(String name) {
        super(name, FXCollections.emptyObservableList(), ICON_FILENAME);
    }

    @Override
    public void createAndAddSubUnit(String name) {
        throw new UnsupportedOperationException("DataBase has no sub units");
    }
}
