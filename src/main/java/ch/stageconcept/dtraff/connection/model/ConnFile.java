package ch.stageconcept.dtraff.connection.model;

import ch.stageconcept.dtraff.connection.util.*;
import ch.stageconcept.dtraff.main.MainApp;
import ch.stageconcept.dtraff.main.view.RootLayoutController;
import ch.stageconcept.dtraff.util.AlertDialog;
import ch.stageconcept.dtraff.util.I18N;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;

//TODO javadoc (cf. ConnRoot class header documentation)

public class ConnFile extends ConnUnit<Conn> {

    // ### Attributes #####################################################################

    // ConnFile file extension
    public static final String FILE_EXT = ".xml";

    private static final String ICON_FILENAME = ConnFileState.EMPTY_CLEAR.getIconFileName();

    // Reference to parent object
    private static final ObjectProperty<ConnRoot> parent = new SimpleObjectProperty<>();

    private String fileName;    // path
    private File file;
    private boolean isPasswordProtected = false;
    private String password;
    private final ObjectProperty<ConnFileState> state;
    private RootLayoutController rootLayoutController;
    private BooleanProperty menuPasswordDisabled;
    private BooleanProperty menuEnterPasswordDisabled;
    private BooleanProperty menuLockPasswordDisabled;
    private BooleanProperty menuSetPasswordDisabled;
    private BooleanProperty menuChangePasswordDisabled;
    private BooleanProperty menuFileRenameDisabled;
    private BooleanProperty menuFileRepairDisabled;
    private Menu passwordMenu;

    // ### Constructors #####################################################################

    /**
     * Constructor.
     *
     * @param name
     * @param subUnits
     */
   public ConnFile(String name, ObservableList<Conn> subUnits) {
       super(name, subUnits, Conn::new, ICON_FILENAME);

       // State
       state = new SimpleObjectProperty<>(ConnFileState.EMPTY_CLEAR);

       // Update icon when state change
       stateProperty().addListener((observable, oldvalue, newvalue) -> {
           setIcon(new ImageView(newvalue.getIconFileName()));
       });
   }

    /**
     * Constructor.
     *
     * @param name
     */
   public ConnFile(String name) {
       this(name, FXCollections.observableArrayList());
   }

    /**
     * Constructor.
     *
     * @param name
     * @param fileName
     * @param parent
     * @param rootLayoutController
     */
    public ConnFile(String name, String fileName, ConnRoot parent, RootLayoutController rootLayoutController) {
        this(name, FXCollections.observableArrayList());

        this.fileName = fileName;
        this.file = new File(fileName);
        setParent(parent);
        this.rootLayoutController = rootLayoutController;

        setContextMenu();
    }

    /**
     * Constructor (copy).
     *
     * @param connFile
     */
    public ConnFile(ConnFile connFile) {
        this(connFile.getName(), FXCollections.observableArrayList());

        this.fileName = connFile.getFileName();
        this.file = connFile.getFile();
        setParent(connFile.getParent());
        this.rootLayoutController = connFile.getRootLayoutController();

        setState(connFile.getState());
    }

    // ### Getters and Setters #####################################################################

    public ConnRoot getParent() {
        return parent.get();
    }

    public ObjectProperty<ConnRoot> parentProperty() {
        return parent;
    }

    public void setParent(ConnRoot parent) {
        this.parent.set(parent);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isPasswordProtected() {
        return isPasswordProtected;
    }

    public void setPasswordProtected(boolean passwordProtected) {
        isPasswordProtected = passwordProtected;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ConnFileState getState() {
        return state.get();
    }

    public ObjectProperty<ConnFileState> stateProperty() {
        return state;
    }

    public void setState(ConnFileState state) {
        this.state.set(state);
    }

    public RootLayoutController getRootLayoutController() {
        return rootLayoutController;
    }

    public void setRootLayoutController(RootLayoutController rootLayoutController) {
        this.rootLayoutController = rootLayoutController;
    }

    public BooleanProperty menuPasswordDisabledProperty() {
        return menuPasswordDisabled;
    }

    public BooleanProperty menuEnterPasswordDisabledProperty() {
        return menuEnterPasswordDisabled;
    }

    public BooleanProperty menuLockPasswordDisabledProperty() {
        return menuLockPasswordDisabled;
    }

    public BooleanProperty menuSetPasswordDisabledProperty() {
        return menuSetPasswordDisabled;
    }

    public BooleanProperty menuChangePasswordDisabledProperty() {
        return menuChangePasswordDisabled;
    }

    public BooleanProperty menuFileRenameDisabledProperty() {
        return menuFileRenameDisabled;
    }

    public BooleanProperty menuFileRepairDisabledProperty() {
        return menuFileRepairDisabled;
    }

    /**
     * Test BROKEN state
     * @return true on BROKEN state, false otherwise
     */
    public boolean isBroken() {
        return state.getValue().equals(ConnFileState.BROKEN);
    }

    /**
     * Set to BROKEN state
     */
    public void setBroken() {
        setState(ConnFileState.BROKEN);
    }

    /**
     * Test EMPTY_CLEAR state
     * @return true on EMPTY_CLEAR state, false otherwise
     */
    public boolean isEmptyClear() {
        return state.getValue().equals(ConnFileState.EMPTY_CLEAR);
    }

    /**
     * Set to EMPTY_CLEAR state
     */
    public void setEmptyClear() {
        setState(ConnFileState.EMPTY_CLEAR);
    }

    /**
     * Test CLEAR state
     * @return true on CLEAR state, false otherwise
     */
    public boolean isClear() {
        return state.getValue().equals(ConnFileState.CLEAR);
    }

    /**
     * Set to CLEAR state
     */
    public void setClear() {
        setState(ConnFileState.CLEAR);
    }

    /**
     * Test ENCRYPTED state
     * @return true on ENCRYPTED state, false otherwise
     */
    public boolean isEncrypted() {
        return state.getValue().equals(ConnFileState.ENCRYPTED);
    }

    /**
     * Set to ENCRYPTED state
     */
    public void setEncrypted() {
        setState(ConnFileState.ENCRYPTED);
    }

    /**
     * Test EMPTY_DECRYPTED state
     * @return true on EMPTY_DECRYPTED state, false otherwise
     */
    public boolean isEmptyDecrypted() {
        return state.getValue().equals(ConnFileState.EMPTY_DECRYPTED);
    }

    /**
     * Set to EMPTY_DECRYPTED state
     */
    public void setEmptyDecrypted() {
        setState(ConnFileState.EMPTY_DECRYPTED);
    }

    /**
     * Test DECRYPTED state
     * @return true on DECRYPTED state, false otherwise
     */
    public boolean isDecrypted() {
        return state.getValue().equals(ConnFileState.DECRYPTED);
    }

    /**
     * Set to DECRYPTED state
     */
    public void setDecrypted() {
        setState(ConnFileState.DECRYPTED);
    }

    public void setContextMenu() {

        // ConnFile instance in treeView contextual menu
        ContextMenu contextMenu = new ContextMenu();

        // ### New Connection MenuItem
        MenuItem newConnectionMenuItem = I18N.menuItemForKey("connFile.contextMenu.newConnection");
        newConnectionMenuItem.setOnAction((ActionEvent t) -> newConn());
        // Disable context menu New Connection if ConnFile object state is broken or encrypted
        newConnectionMenuItem.disableProperty().bind(Bindings.createBooleanBinding(() -> isBroken() || isEncrypted(), state));
        // ###################################################################

        // ### Password Menu

        menuEnterPasswordDisabled = new SimpleBooleanProperty();
        menuEnterPasswordDisabled.bind(Bindings.createBooleanBinding(() -> !isEncrypted(), state));

        menuLockPasswordDisabled = new SimpleBooleanProperty();
        menuLockPasswordDisabled.bind(Bindings.createBooleanBinding(() -> !isDecrypted(), state));

        menuSetPasswordDisabled = new SimpleBooleanProperty();
        menuSetPasswordDisabled.bind(Bindings.createBooleanBinding(() -> !isClear() && !isEmptyClear(), state));

        menuChangePasswordDisabled = new SimpleBooleanProperty();
        menuChangePasswordDisabled.bind(Bindings.createBooleanBinding(() -> !(isEncrypted() || isEmptyDecrypted() || isDecrypted()), state));

        menuPasswordDisabled = new SimpleBooleanProperty();
        menuPasswordDisabled.bind(menuEnterPasswordDisabled
                .and(menuLockPasswordDisabled
                        .and(menuSetPasswordDisabled
                                .and(menuChangePasswordDisabled))));

        passwordMenu = new Menu();

        /*
        passwordMenu = I18N.menuForKey("connFile.contextMenu.password");

        MenuItem setPasswordMenuItem = I18N.menuItemForKey("connFile.contextMenu.password.set");
        setPasswordMenuItem.setOnAction((ActionEvent t) -> setPassword());
        // Disable context menu Password - Set if ConnFile object state is not clear or empty clear
        setPasswordMenuItem.disableProperty().bind(menuSetPasswordDisabled);

        MenuItem enterPasswordMenuItem = I18N.menuItemForKey("connFile.contextMenu.password.enter");
        enterPasswordMenuItem.setOnAction((ActionEvent t) -> enterPassword());
        // Disable context menu Password - Enter if ConnFile object state is not encrypted
        enterPasswordMenuItem.disableProperty().bind(Bindings.createBooleanBinding(() -> !isEncrypted(), state));

        MenuItem changePasswordMenuItem = I18N.menuItemForKey("connFile.contextMenu.password.change");
        changePasswordMenuItem.setOnAction((ActionEvent t) -> changePassword());

        MenuItem removePasswordMenuItem = I18N.menuItemForKey("connFile.contextMenu.password.remove");
        removePasswordMenuItem.setOnAction((ActionEvent t) -> removePassword());

        passwordMenu.getItems().addAll(setPasswordMenuItem, enterPasswordMenuItem, changePasswordMenuItem, removePasswordMenuItem);
        */

        // ###################################################################

        // ### Rename File MenuItem
        MenuItem renameFileMenuItem = I18N.menuItemForKey("connFile.contextMenu.file.rename");
        renameFileMenuItem.setOnAction((ActionEvent t) -> rename());
        menuFileRenameDisabled = new SimpleBooleanProperty();
        menuFileRenameDisabled.bind(Bindings.createBooleanBinding(() -> isBroken() || isEncrypted(), state));
        // Disable context menu Rename File if ConnFile object state is broken or encrypted
        renameFileMenuItem.disableProperty().bind(menuFileRenameDisabled);
        // ###################################################################

        // ### Repair File MenuItem
        MenuItem repairFileMenuItem = I18N.menuItemForKey("connFile.contextMenu.file.repair");
        repairFileMenuItem.setOnAction((ActionEvent t) -> openBrokenConnFile());
        menuFileRepairDisabled = new SimpleBooleanProperty();
        menuFileRepairDisabled.bind(Bindings.createBooleanBinding(() -> !isBroken(), state));
        // Disable context menu Repair File if ConnFile object state is not broken
        repairFileMenuItem.disableProperty().bind(menuFileRepairDisabled);
        // ###################################################################

        // ### Close File MenuItem
        MenuItem closeFileMenuItem = I18N.menuItemForKey("connFile.contextMenu.file.close");
        closeFileMenuItem.setOnAction((ActionEvent t) -> closeConnFile());
        // ###################################################################

        contextMenu.getItems().addAll(
                newConnectionMenuItem,
                new SeparatorMenuItem(),
                passwordMenu,
                new SeparatorMenuItem(),
                renameFileMenuItem,
                repairFileMenuItem,
                closeFileMenuItem);

        this.setMenu(contextMenu);

        // Node can only be displayed once in Scene Graph.
        // So, when context menu show, remove and get passwordMenu from controller...
        getMenu().setOnShowing(e -> {
            ObservableList<MenuItem> menuItems = getMenu().getItems();
            int passwordMenuIndex = menuItems.indexOf(passwordMenu);
            menuItems.remove(passwordMenu);
            menuItems.add(passwordMenuIndex, rootLayoutController.getPasswordMenu());
        });

        // ...And, when context menu hide, remove and put passwordMenu to controller
        getMenu().setOnHiding(e -> {
            ObservableList<MenuItem> menuItems = getMenu().getItems();
            Menu rootLayoutControllerPasswordMenu = rootLayoutController.getPasswordMenu();
            int passwordMenuIndex = menuItems.indexOf(rootLayoutControllerPasswordMenu);
            menuItems.remove(rootLayoutControllerPasswordMenu);
            menuItems.add(passwordMenuIndex, passwordMenu);
            rootLayoutController.setPasswordMenu(rootLayoutControllerPasswordMenu);
        });

    }

    // ### Methods #####################################################################

    /**
     * Rename ConnFile object (file).
     */
    public void rename() {
        //TODO Try to factorize (how??), almost same method as Conn.renameConnection() method
        // Get treeView selected item
        TreeView<ConnUnit<?>> connTreeView = rootLayoutController.getConnTreeView();
        TreeItem<ConnUnit<?>> selectedItem = connTreeView.getSelectionModel().getSelectedItem();

        // Put selected item in edit mode
        connTreeView.edit(selectedItem);
    }

    /**
     * Open broken state ConnFile object (file).
     */
    private void openBrokenConnFile() {
        /*
        To be safe, the state is checked even it should be guaranteed
        by the method caller (in this case, the disable property
        of the related contextual menu (Open File)).
         */
        if (isBroken()) rootLayoutController.openBrokenConnFile(this);
    }

    /**
     * Close ConnFile
     */
    public void closeConnFile() {
        //System.out.println("Close file on:" + this.getName());
        this.getParent().closeConnFile(this);
    }

    /**
     * Create new Conn object (connection)
     */
    public void newConn() {
        //System.out.println("New Connection on:" + this.getName());

        // new Conn instance with default name value
        Conn conn = new Conn(DbType.INSTANCE.getDbDescriptorMap().get(DbType.MYSQL_KEY).getName());
        conn.setParent(this);

        // If ConnFile is password protected,
        // encrypt Conn password default value (root) with ConnFile password
        if (conn.getParent().isPasswordProtected()) {
            Crypto crypto = new Crypto(conn.getParent().getPassword());
            conn.setPassword(crypto.getEncrypted(conn.getPassword()));
        }

        if (ConnEditor.INSTANCE.supply(conn, MainApp.TEXT_BUNDLE.getString("connEditDialog.title.new"))) {
            getSubUnits().add(conn);
            //this.createAndAddSubUnit("Hello, world!");
            saveConnDataToFile();
        }

        // Change state
        if (getSubUnits() != null) {
            if (isEmptyClear()) setClear();
            else if (isEmptyDecrypted()) setDecrypted();
        }

    }

    /**
     * Saves the current connection data to file.
     *
     * @return true if ok, false otherwise
     */
    public boolean saveConnDataToFile() {

        File file = new java.io.File(this.fileName);

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            JAXBContext context = JAXBContext.newInstance(ConnListWrapper.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Wrapping our person data.
            ConnListWrapper wrapper = new ConnListWrapper();
            wrapper.setConns(this.getSubUnits());

            // debug mode
            /*
            for (Conn conn: wrapper.getConns()) {
                System.out.println(conn);
            }
            */

            // Marshalling and saving XML to the file.
            m.marshal(wrapper, file);

        } catch (Exception e) {

            // catches ANY exception
            // May happen if file is e.g. read only

            //e.printStackTrace();

            AlertDialog.provide(MainApp.PRIMARY_STAGE,
                    Alert.AlertType.ERROR,
                    MainApp.TEXT_BUNDLE.getString("connFile.alertSaveData.title"),
                    MainApp.TEXT_BUNDLE.getString("connFile.alertSaveData.header"),
                    MainApp.TEXT_BUNDLE.getString("connFile.alertSaveData.content") + file.getPath(), true);

            return false;
        }

        return true;
    }

    /**
     * Check if a ConnFile object with given String name parameter
     * is present in ConnRoot.
     *
     * @param name
     * @return ConnFile object if one with name attribute exist in ConnRoot,
     * null otherwise.
     */
    public static ConnFile getFromConnRoot(String name) {
        // SRC: http://stackoverflow.com/questions/23407014/return-from-lambda-foreach-in-java
        try {
            return parent.get()
                    .getSubUnits()
                    .stream()
                    .filter(connFile -> connFile.getName().equalsIgnoreCase(name))
                    .findFirst().orElse(null);
        } catch (NullPointerException ex) {
            return null;
        }
    }

    /**
     * Check if a ConnFile object with given String name parameter
     * is present in ConnRoot.
     *
     * @param name
     * @return true if one ConnFile instance with name attribute exist in ConnRoot,
     * false otherwise.
     */
    public static boolean isInConnRoot(String name) {
        return getFromConnRoot(name) != null;
    }

    /**
     * Check if a file is present in folder.
     *
     * @param name
     * @param folder
     * @return true if one file with name exist in folder,
     * false otherwise.
     */
    public static boolean isFileInFolder(String name, String folder) {

        File file = new File(folder, name + ConnFile.FILE_EXT);

        return file.exists() && !file.isDirectory();
    }

    /**
     * Check if a file is present in folder.
     *
     * @return true if one file with name exist in folder,
     * false otherwise.
     */
    public boolean isFileInFolder() {
        return file.exists() && !file.isDirectory();
    }

    /**
     * New file name from connFile with newName.
     *
     * @param connFile
     * @param newName
     * @return new file name
     */
    public static String newFileName(ConnFile connFile, String newName) {

        // Be aware that name can be contained in path,
        // so pattern below is used to clearly targeting
        // name of the file at the end of the path (fileName).
        String pattern = File.separator + "%s" + FILE_EXT;

        String target = String.format(pattern, connFile.getName());
        String replacement = String.format(pattern, newName);

        return connFile.getFileName().replace(target, replacement);
    }

}
