package ch.stageconcept.dtraff.connection.tree.model;

import javafx.collections.FXCollections;
import javafx.scene.image.ImageView;

public class DataBase extends ConnectionUnit<ConnectionUnit<?>> {

    public DataBase(String name) {
        super(name, FXCollections.emptyObservableList());

        this.setIcon(new ImageView("database001.png"));
    }

    @Override
    public void createAndAddSubUnit(String name) {
        throw new UnsupportedOperationException("DataBase has no sub units");
    }
}
