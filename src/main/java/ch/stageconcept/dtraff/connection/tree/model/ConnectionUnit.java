package ch.stageconcept.dtraff.connection.tree.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.image.ImageView;

import java.util.function.Function;

//TODO javadoc

public class ConnectionUnit<T extends ConnectionUnit<?>> {
    
    private final StringProperty name = new SimpleStringProperty();
    private ObjectProperty<ImageView> icon;
    private ObjectProperty<ContextMenu> menu;
    private final Function<String, T> subUnitSupplier ;
    private final ObservableList<T> subUnits ;

    public ConnectionUnit(String name, ObservableList<T> subUnits, Function<String, T> subUnitSupplier, String iconFileName) {
        this.subUnits = subUnits ;
        this.subUnitSupplier = subUnitSupplier ;
        setName(name);
        this.icon = new SimpleObjectProperty<>(new ImageView(iconFileName));
        this.menu = new SimpleObjectProperty<>();
    }

    public ConnectionUnit(String name, ObservableList<T> subUnits, String iconFileName) {
        this(name, subUnits, n -> null, iconFileName);
    }
    
    public ConnectionUnit(String name, Function<String, T> subUnitSupplier) {
        this(name, FXCollections.observableArrayList(), subUnitSupplier, null);
    }

    public ConnectionUnit(String name) {
        this(name, FXCollections.observableArrayList(), n -> null, null) ;
    }

    @Override
    public String toString() {
        return "ConnectionUnit{" +
                "name=" + name +
                '}';
    }

    public final StringProperty nameProperty() {
        return this.name;
    }
    
    public final String getName() {
        return this.nameProperty().get();
    }
    
    public final void setName(final String name) {
        this.nameProperty().set(name);
    }

    public ImageView getIcon() {
        return icon.get();
    }

    public ObjectProperty<ImageView> iconProperty() {
        return icon;
    }

    public void setIcon(ImageView icon) {
        this.icon.set(icon);
    }

    public ContextMenu getMenu() {
        return menu.get();
    }

    public ObjectProperty<ContextMenu> menuProperty() {
        return menu;
    }

    public void setMenu(ContextMenu menu) {
        this.menu.set(menu);
    }

    public ObservableList<T> getSubUnits() {
        return subUnits;
    }
    
    public void createAndAddSubUnit(String name) {
        getSubUnits().add(subUnitSupplier.apply(name));
    }

}
