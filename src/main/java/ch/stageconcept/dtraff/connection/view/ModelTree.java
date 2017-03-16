package ch.stageconcept.dtraff.connection.view;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.prefs.Preferences;

import ch.stageconcept.dtraff.connection.model.Conn;
import ch.stageconcept.dtraff.connection.model.ConnFile;
import ch.stageconcept.dtraff.connection.model.ConnRoot;
import ch.stageconcept.dtraff.connection.model.ConnUnit;
import ch.stageconcept.dtraff.util.StringUtil;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

//TODO javadoc (cf. connection.model.ConnRoot class header documentation)

public class ModelTree<T> {

    // User preferences (file node)
    private Preferences prefs = Preferences.userRoot().node(ConnRoot.PREFS_PATH);

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

            private TextField textField;

            private Set<PseudoClass> pseudoClassesSet = new HashSet<>();

            @Override
            public void startEdit() {

                boolean okToEdit = true;

                if (getItem().getClass().equals(ConnRoot.class)) {
                    okToEdit = false;
                }

                if (getItem().getClass().equals(ConnFile.class)) {
                    okToEdit = okToEdit && !((ConnFile) getItem()).isBroken() && !((ConnFile) getItem()).isEncrypted();
                }

                if (okToEdit) {
                    super.startEdit();

                    if (textField == null) createTextField();

                    textProperty().unbind();
                    graphicProperty().unbind();
                    contextMenuProperty().unbind();

                    setText(null);
                    setGraphic(textField);
                    textField.selectAll();
                }

            }

            private void createTextField() {
                textField = new TextField(getString());
                textField.setOnKeyReleased((KeyEvent t) -> {
                    if (t.getCode() == KeyCode.ENTER) commitEdit();
                    else if (t.getCode() == KeyCode.ESCAPE) cancelEdit();
                });
            }

            private String getString() {
                if (getItem().getClass().equals(ConnFile.class)) return getItem() == null ? "" : ((ConnFile) getItem()).getName();
                if (getItem().getClass().equals(Conn.class)) return getItem() == null ? "" : ((Conn) getItem()).getName();
                else return "not applicable!";
            }

            private void commitEdit() {

                T item = getItem();

                boolean isConnFile = getItem().getClass().equals(ConnFile.class);
                boolean isConn = !isConnFile && getItem().getClass().equals(Conn.class);

                String newName = textField.getText();

                if (isConnFile) {
                    ConnFile connFile = (ConnFile) getItem();

                    connFile.setName(newName);

                    /*
                    if (connFile.getFile().exists() && !connFile.getFile().isDirectory()) {

                        String prefKeyToRemove = connFile.getName();
                        String newFileName = connFile.getFileName().replace(connFile.getName(), newName);
                        boolean fileRenameOk = connFile.getFile().renameTo(new File(newFileName));

                        if (fileRenameOk) {

                            connFile.setName(newName);
                            connFile.setFileName(newFileName);
                            //((ConnFile) getItem()).getParent().sortSubUnits();

                            // update preference
                            prefs.remove(prefKeyToRemove);
                            prefs.put(newName, newFileName);

                        } else {
                            System.out.println("Rename file fail: " + newFileName);
                        }

                    }
*/
                }
                else if (isConn) {
                    Conn conn = (Conn) getItem();
                    conn.setName(newName);
                }

                super.commitEdit(getItem());

                /*
                String newName = textField.getText();

                if (StringUtil.notNullAndLengthGreaterThanZero(newName)) {

                    if (getItem().getClass().equals(ConnFile.class)) {

                        if (((ConnFile) getItem()).getFile().exists() && !((ConnFile) getItem()).getFile().isDirectory()) {

                            String prefKeyToRemove = ((ConnFile) getItem()).getName();
                            String newFileName = ((ConnFile) getItem()).getFileName().replace(((ConnFile) getItem()).getName(), newName);
                            boolean fileRenameOk = ((ConnFile) getItem()).getFile().renameTo(new File(newFileName));

                            if (fileRenameOk) {

                                ((ConnFile) getItem()).setName(newName);
                                ((ConnFile) getItem()).setFileName(newFileName);
                                setGraphic(((ConnFile) getItem()).getIcon());
                                //((ConnFile) getItem()).getParent().sortSubUnits();

                                textProperty().bind(text.apply(getItem()));
                                graphicProperty().bind(icon.apply(getItem()));
                                contextMenuProperty().bind(menu.apply(getItem()));

                                // update preference
                                prefs.remove(prefKeyToRemove);
                                prefs.put(newName, newFileName);

                            } else {
                                System.out.println("Rename file fail: " + newFileName);
                            }

                        }

                    }

                    if (getItem().getClass().equals(Conn.class)) {
                        ((Conn) getItem()).setName(newName);
                        ((Conn) getItem()).getParent().saveConnDataToFile();
                        setGraphic(((Conn) getItem()).getIcon());
                    }

                }

                super.commitEdit(getItem());

                textField = null;

                //super.cancelEdit();
*/
            }

            @Override
            public void cancelEdit() {

                super.cancelEdit();

                if (getItem().getClass().equals(ConnFile.class)) {
                    setText(((ConnFile) getItem()).getName());
                    setGraphic(((ConnFile) getItem()).getIcon());
                }

                if (getItem().getClass().equals(Conn.class)) {
                    setText(((Conn) getItem()).getName());
                    setGraphic(((Conn) getItem()).getIcon());
                }

                textField = null;

            }

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
                } else if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
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
                    // root item icon and designation. Here we listen the ConnRoot instance state change
                    // and set text style to italic during this process.
                    if (item.getClass().equals(ConnRoot.class)) {
                        ConnRoot connRoot = (ConnRoot) item;
                        connRoot.stateProperty().addListener(nv -> {
                            if (connRoot.isOpeningFile()) setStyle(FONT_STYLE_ITALIC);
                            else setStyle(FONT_STYLE_NORMAL);
                        });
                    }

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
