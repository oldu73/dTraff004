package ch.stageconcept.dtraff.main.util;

import ch.stageconcept.dtraff.connection.unit.model.DbConnect;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

/**
 * Created by oldu7 on 10.11.2016.
 */
public class SimpleConnectionTreeCell extends TreeCell {

    private final ContextMenu contextMenu = new ContextMenu();

    public SimpleConnectionTreeCell() {
        MenuItem editMenuItem = new MenuItem("Edit");
        contextMenu.getItems().add(editMenuItem);
        editMenuItem.setOnAction((ActionEvent t) -> {
            final DbConnect dbConnect = (DbConnect) this.getItem();
            dbConnect.getMainApp().showConnectionEditDialog(dbConnect);
        });
    }

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
                setContextMenu(contextMenu);
            } else {
                setText(item.toString());
                setGraphic(null);
            }
        }
    }
}