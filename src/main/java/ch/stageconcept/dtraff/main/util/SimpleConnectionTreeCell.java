package ch.stageconcept.dtraff.main.util;

import ch.stageconcept.dtraff.connection.model.DbConnect;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by oldu7 on 10.11.2016.
 */
public class SimpleConnectionTreeCell extends TreeCell {

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);
        setGraphic(null);
        if (!empty && item != null) {
            if (item instanceof DbConnect) {
                final DbConnect dbConnect = (DbConnect) item;
                final String text = String.format("%s", dbConnect.getName());
                setText(text);
                setGraphic(dbConnect.getIcon());
            } else {
                setText(item.toString());
                setGraphic(null);
            }
        }
    }
}