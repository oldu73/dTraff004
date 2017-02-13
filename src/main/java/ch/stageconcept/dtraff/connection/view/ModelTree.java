package ch.stageconcept.dtraff.connection.view;

import static java.util.stream.Collectors.toList;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import ch.stageconcept.dtraff.connection.model.ConnRoot;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;

//TODO javadoc (cf. connection.model.ConnRoot class header documentation)

public class ModelTree<T> {

    // Font styles
    private static final String FONT_STYLE_NORMAL = "-fx-font-style: normal";
    private static final String FONT_STYLE_ITALIC = "-fx-font-style: italic";

    private final TreeView<T> treeView ;
    
    private final Function<T, ObservableList<? extends T>> children ;

    public ModelTree(T rootItem, Function<T, ObservableList<? extends T>> children, 
                     Function<T, ObservableValue<String>> text,
                     Function<T, ObservableValue<ImageView>> icon,
                     Function<T, ObservableValue<ContextMenu>> menu,
                     Function<T, PseudoClass> pseudoClassMap) {

        this.children = children ;
        
        treeView = new TreeView<>(createTreeItem(rootItem));
        
        treeView.setCellFactory(tv -> new TreeCell<T>() {
            
            private Set<PseudoClass> pseudoClassesSet = new HashSet<>();
            
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                textProperty().unbind();
                graphicProperty().unbind();
                contextMenuProperty().unbind();
                
                pseudoClassesSet.forEach(pc -> pseudoClassStateChanged(pc, false));
                if (empty) {
                    setText("");
                    setGraphic(null);
                    setContextMenu(null);
                } else {
                    textProperty().bind(text.apply(item));
                    graphicProperty().bind(icon.apply(item));
                    contextMenuProperty().bind(menu.apply(item));

                    PseudoClass itemPC = pseudoClassMap.apply(item);
                    if (itemPC != null) {
                        pseudoClassStateChanged(itemPC, true);
                        pseudoClassesSet.add(itemPC);
                    }

                    // Because of UI frozen behavior (depends of machine) due to file chooser,
                    // we manage to inform user of running process (opening file) through treeView
                    // root item icon and designation. Here we intercept the designation change that happen
                    // when ConnRoot instance state change and set text style to italic during this process.
                    textProperty().addListener(nv -> {
                        if (item.getClass().equals(ConnRoot.class) && ((ConnRoot) item).isOpeningFile()) setStyle(FONT_STYLE_ITALIC);
                        else setStyle(FONT_STYLE_NORMAL);
                    });

                }
            }
        });
    }
    
    public ModelTree(T t, Function<T, ObservableList<? extends T>> children, Function<T, ObservableValue<String>> text, Function<T, ObservableValue<ImageView>> icon, Function<T, ObservableValue<ContextMenu>> menu) {
        this(t, children, text, icon, menu, x -> null);
    }

    public TreeView<T> getTreeView() {
        return treeView ;
    }
    
    private TreeItem<T> createTreeItem(T t) {
        TreeItem<T> item = new TreeItem<>(t);
        children.apply(t).stream().map(this::createTreeItem).forEach(item.getChildren()::add);
        
        children.apply(t).addListener((Change<? extends T> change) -> {
            while (change.next()) {
                
                if (change.wasAdded()) {
                    item.getChildren().addAll(change.getAddedSubList().stream()
                            .map(this::createTreeItem).collect(toList()));
                }
                if (change.wasRemoved()) {
                    item.getChildren().removeIf(treeItem -> change.getRemoved()
                            .contains(treeItem.getValue()));
                }
            }
        });
        
        return item ;
    }
    
}
