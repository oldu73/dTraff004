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

    private static final String ICON_FILENAME = ConnFileState.EMPTY_CLEAR.getIconFileName();

    // Reference to parent object
    private final ObjectProperty<ConnRoot> parent;

    private String fileName;    // path
    private boolean isPasswordProtected = false;
    private String password;
    private final ObjectProperty<ConnFileState> state;
    private RootLayoutController rootLayoutController;
    private BooleanProperty menuSetPasswordDisabled;

    // ### Constructors #####################################################################

    /**
     * Constructor.
     *
     * @param name
     * @param subUnits
     */
   public ConnFile(String name, ObservableList<Conn> subUnits) {
       super(name, subUnits, Conn::new, ICON_FILENAME);

       this.parent = new SimpleObjectProperty<>();

       // State
       state = new SimpleObjectProperty<>(ConnFileState.EMPTY_CLEAR);

       // Update icon when state change
       stateProperty().addListener((observable, oldvalue, newvalue) -> {
           setIcon(new ImageView(newvalue.getIconFileName()));
       });

       menuSetPasswordDisabled = new SimpleBooleanProperty();
       menuSetPasswordDisabled.bind(Bindings.createBooleanBinding(() -> !isClear() && !isEmptyClear(), state));

       // treeView context menu
       ContextMenu contextMenu = new ContextMenu();

       // ### New Connection MenuItem
       MenuItem newConnectionMenuItem = I18N.menuItemForKey("connFile.contextMenu.newConnection");
       newConnectionMenuItem.setOnAction((ActionEvent t) -> newConn());
       // Disable context menu New Connection if ConnFile object state is broken or encrypted
       newConnectionMenuItem.disableProperty().bind(Bindings.createBooleanBinding(() -> isBroken() || isEncrypted(), state));
       // ###################################################################

       // ### Password Menu
       Menu passwordMenu = I18N.menuForKey("connFile.contextMenu.password");

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
       // ###################################################################

       // ### Repair File MenuItem
       MenuItem repairFileMenuItem = I18N.menuItemForKey("connFile.contextMenu.file.repair");
       repairFileMenuItem.setOnAction((ActionEvent t) -> openBrokenConnFile());
       // Disable context menu Open File if ConnFile object state is not broken
       repairFileMenuItem.disableProperty().bind(Bindings.createBooleanBinding(() -> !isBroken(), state));
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
               repairFileMenuItem,
               closeFileMenuItem);

       this.setMenu(contextMenu);

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
        setParent(parent);
        this.rootLayoutController = rootLayoutController;
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

    private void setState(ConnFileState state) {
        this.state.set(state);
    }

    public RootLayoutController getRootLayoutController() {
        return rootLayoutController;
    }

    public void setRootLayoutController(RootLayoutController rootLayoutController) {
        this.rootLayoutController = rootLayoutController;
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

    public boolean isMenuSetPasswordDisabled() {
        return menuSetPasswordDisabled.get();
    }

    public BooleanProperty menuSetPasswordDisabledProperty() {
        return menuSetPasswordDisabled;
    }

    // ### Methods #####################################################################

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
     * Set password
     */
    private void setPassword() {
        parent.get().setPassword(this);
    }

    /**
     * Enter password
     */
    private void enterPassword() {

        parent.get().decryptPassword(this).populateSubUnit(this);

        // Refactored (below replaced by above).

        /*
        if (getRootLayoutController().decryptConnFile(this)) {
            getRootLayoutController().populateSubunit(this, getRootLayoutController().loadConnDataFromConnFile(this));
        }
        */

    }

    /**
     * Change password
     */
    private void changePassword() {
        parent.get().changePassword(this);
    }

    /**
     * Remove password
     */
    private void removePassword() {
        parent.get().removePassword(this);
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
            else setDecrypted();
        }

    }

    /**
     * Saves the current connection data to file.
     *
     */
    public void saveConnDataToFile() {

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

        } catch (Exception e) { // catches ANY exception

            //e.printStackTrace();

            AlertDialog.provide(MainApp.PRIMARY_STAGE,
                    Alert.AlertType.ERROR,
                    MainApp.TEXT_BUNDLE.getString("connFile.alertSaveData.title"),
                    MainApp.TEXT_BUNDLE.getString("connFile.alertSaveData.header"),
                    MainApp.TEXT_BUNDLE.getString("connFile.alertSaveData.content") + file.getPath(), true);

        }
    }

}
