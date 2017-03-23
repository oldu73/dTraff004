package ch.stageconcept.dtraff.connection.view;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.prefs.Preferences;

import ch.stageconcept.dtraff.connection.model.Conn;
import ch.stageconcept.dtraff.connection.model.ConnFile;
import ch.stageconcept.dtraff.connection.model.ConnRoot;
import ch.stageconcept.dtraff.connection.model.ConnUnit;
import ch.stageconcept.dtraff.main.MainApp;
import ch.stageconcept.dtraff.main.view.RootLayoutController;
import ch.stageconcept.dtraff.util.StringUtil;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.log4j.Logger;

//TODO javadoc (cf. connection.model.ConnRoot class header documentation)

public class ModelTree<T> {

    private Logger logger = Logger.getLogger(this.getClass());

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

            private BiPredicate<T, Class> isClass = (item, clazz) -> item.getClass().equals(clazz);
            private Predicate<T> isConnRoot = item -> isClass.test(item, ConnRoot.class);
            private Predicate<T> isConnFile = item -> isClass.test(item, ConnFile.class);
            private Predicate<T> isConn = item -> isClass.test(item, Conn.class);

            @Override
            public void startEdit() {

                T item = getItem();
                boolean okToEdit = true;

                if (isConnRoot.test(item)) {
                    okToEdit = false;
                }

                if (isConnFile.test(item)) {
                    ConnFile connFile = (ConnFile) item;
                    okToEdit = okToEdit && !connFile.isBroken() && !connFile.isEncrypted();
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

                    if (t.getCode() == KeyCode.ENTER) {

                        T item = getItem();
                        String newName = textField.getText();
                        boolean okToCommit = true;

                        if (isConnFile.test(item)) {

                            ConnFile connFile = (ConnFile) item;

                            if (ConnFile.isInConnRoot(newName)) {
                                logger.info(MainApp.TEXT_BUNDLE.getString("logger.connFile.alreadyPresentInTreeView")
                                        + StringUtil.nameFileNameToString(ConnFile.getFromConnRoot(newName)));
                                okToCommit = false;
                            } else {

                                String folder = connFile.getFileName().replace(File.separator + connFile.getName() + ConnFile.FILE_EXT, "");

                                if (ConnFile.isFileInFolder(newName, folder)) {
                                    logger.info(MainApp.TEXT_BUNDLE.getString("logger.connFile.alreadyPresentInFolder")
                                            + StringUtil.nameFileNameToString(newName, folder));
                                    okToCommit = false;
                                }
                            }

                        }

                        //TODO If is Conn check that a conn with this "new" name does not exist in ConnFile container

                        if (okToCommit) commitEdit(item);
                        else cancelEdit();
                    }
                    else if (t.getCode() == KeyCode.ESCAPE) cancelEdit();
                });
            }

            private String getString() {
                if (getItem().getClass().equals(ConnFile.class)) return getItem() == null ? "" : ((ConnFile) getItem()).getName();
                if (getItem().getClass().equals(Conn.class)) return getItem() == null ? "" : ((Conn) getItem()).getName();
                else return "not applicable!";
            }

            @Override
            public void commitEdit(T newValue) {

                super.commitEdit(newValue);

                String newName = textField.getText();

                if (StringUtil.notNullAndLengthGreaterThanZero(newName)) {

                    // ### ConnFile #####################################################
                    if (isConnFile.test(newValue)) {

                        ConnFile connFile = (ConnFile) newValue;
                        String prefKeyToRemove = connFile.getName();
                        String newFileName = ConnFile.newFileName(connFile, newName);

                        if (!connFile.isEmptyClear() && !connFile.isEmptyDecrypted()) {
                            if (connFile.isFileInFolder()) {

                                boolean fileRenameOk = connFile.getFile().renameTo(new File(newFileName));

                                if (fileRenameOk) {

                                    connFile.setName(newName);
                                    connFile.setFileName(newFileName);
                                    connFile.setFile(new File(newFileName));

                                    // The treeView selection doesn't follow item renaming, so we store selection before sorting,
                                    // clear selection, sort and reselect previously selected.

                                    // Get treeView selected item
                                    TreeView<ConnUnit<?>> connTreeView = connFile.getRootLayoutController().getConnTreeView();
                                    TreeItem<ConnUnit<?>> selectedItem = connTreeView.getSelectionModel().getSelectedItem();
                                    connTreeView.getSelectionModel().clearSelection();
                                    connFile.getParent().sortSubUnits();
                                    // After sort (above) reselect just modified connFile in treeView
                                    connFile.getRootLayoutController().getConnTreeView().getSelectionModel().select(selectedItem);

                                    // update preference
                                    prefs.remove(prefKeyToRemove);
                                    prefs.put(newName, newFileName);

                                } else {
                                    logger.info(MainApp.TEXT_BUNDLE.getString("logger.renameFileFail") + StringUtil.nameFileNameToString(connFile));
                                }
                            } else {
                                // Means, connFile file is not in folder
                                logger.info(MainApp.TEXT_BUNDLE.getString("logger.renameFileFail") + StringUtil.nameFileNameToString(connFile));
                            }
                        } else {
                            // Means, connFile is EmptyClear or connFile is EmptyDecrypted

                            connFile.setName(newName);
                            connFile.setFileName(newFileName);
                            connFile.setFile(new File(newFileName));

                            // update preference
                            prefs.remove(prefKeyToRemove);
                            prefs.put(newName, newFileName);
                        }
                    }

                    // ### Conn #####################################################
                    else if (isConn.test(newValue)) {
                        Conn conn = (Conn) newValue;
                        String currentName = conn.getName();
                        conn.setName(newName);

                        // If saveConnDataToFile method fail (returned false), rollback conn.name
                        if (!conn.getParent().saveConnDataToFile()) conn.setName(currentName);
                    }

                }

                textField = null;

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
