package ch.stageconcept.dtraff.connection.model;

import javafx.collections.FXCollections;

//TODO javadoc (cf. ConnRoot class header documentation)

public class DataBase extends ConnUnit<ConnUnit<?>> {

    private static final String ICON_FILENAME = "database001.png";

    public DataBase(String name) {
        super(name, FXCollections.emptyObservableList(), ICON_FILENAME);
    }

    @Override
    public void createAndAddSubUnit(String name) {
        throw new UnsupportedOperationException("DataBase has no sub units");
    }
}
