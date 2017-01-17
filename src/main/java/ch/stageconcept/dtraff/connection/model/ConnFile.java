package ch.stageconcept.dtraff.connection.model;

import ch.stageconcept.dtraff.connection.util.*;
import ch.stageconcept.dtraff.main.view.RootLayoutController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.ImageView;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;

//TODO javadoc

public class ConnFile extends ConnUnit<Conn> {

    // ### Attributes #####################################################################

    private static final String ICON_FILENAME = ConnFileState.CLEAR.getIconFileName();

    private static final String MENU_OPEN_FILE = "Open File";
    private static final String MENU_NEW_CONNECTION = "New Connection";
    public static final String MENU_ENTER_PASSWORD = "Enter Password";
    private static final String MENU_CLOSE_FILE = "Close File";

    private static final String ALERT_SAVE_DATA_TITLE = "Error";
    private static final String ALERT_SAVE_DATA_HEADER = "Could not save data";
    private static final String ALERT_SAVE_DATA_CONTENT = "Could not save data to file:\n";

    private static final String DIALOG_NEW_CONNECTION_TITLE = MENU_NEW_CONNECTION;

    // Reference to parent object
    private final ObjectProperty<Network> parent;

    private String fileName;
    private boolean isPasswordProtected = false;
    private String password;
    private final ObjectProperty<ConnFileState> state;
    private RootLayoutController rootLayoutController;

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
       state = new SimpleObjectProperty<>(ConnFileState.CLEAR);

       // Update icon when state change
       stateProperty().addListener((observable, oldvalue, newvalue) -> {
           setIcon(new ImageView(newvalue.getIconFileName()));
       });

       // treeView context menu
       ContextMenu contextMenu = new ContextMenu();

       // ### Open File Menu
       MenuItem openFileMenuItem = new MenuItem(MENU_OPEN_FILE);

       openFileMenuItem.setOnAction((ActionEvent t) -> openBrokenConnFile());

       // Disable context menu Open File if ConnFile object state is not broken
       openFileMenuItem.disableProperty().bind(Bindings.createBooleanBinding(() ->
               !getState().equals(ConnFileState.BROKEN), state));
       // ###################################################################

       // ### New Connection Menu
       MenuItem newConnectionMenuItem = new MenuItem(MENU_NEW_CONNECTION);

       newConnectionMenuItem.setOnAction((ActionEvent t) -> newConn());

       // Disable context menu New Connection if ConnFile object state is broken or encrypted
       newConnectionMenuItem.disableProperty().bind(Bindings.createBooleanBinding(() ->
               getState().equals(ConnFileState.BROKEN) || getState().equals(ConnFileState.ENCRYPTED), state));
       // ###################################################################

       // ### Enter password Menu
       MenuItem enterPasswordMenuItem = new MenuItem(MENU_ENTER_PASSWORD);

       enterPasswordMenuItem.setOnAction((ActionEvent t) -> {
           if (getRootLayoutController().decryptConnFile(this)) {
               getRootLayoutController().populateSubunit(this, getRootLayoutController().loadConnDataFromConnFile(this));
           }
       });

       // Disable context menu Enter password if ConnFile object state is not encrypted
       enterPasswordMenuItem.disableProperty().bind(Bindings.createBooleanBinding(() ->
               !getState().equals(ConnFileState.ENCRYPTED), state));
       // ###################################################################

       // ### Close File Menu
       MenuItem closeFileMenuItem = new MenuItem(MENU_CLOSE_FILE);

       closeFileMenuItem.setOnAction((ActionEvent t) -> {
            //System.out.println("Close file on:" + this.getName());
            this.getParent().closeConnFile(this);
       });
       // ###################################################################

       SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();

       contextMenu.getItems().addAll(openFileMenuItem,
               newConnectionMenuItem,
               enterPasswordMenuItem,
               separatorMenuItem,
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

    // ### Getters and Setters #####################################################################

    public Network getParent() {
        return parent.get();
    }

    public ObjectProperty<Network> parentProperty() {
        return parent;
    }

    public void setParent(Network parent) {
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

    public void setState(ConnFileState state) {
        this.state.set(state);
    }

    public RootLayoutController getRootLayoutController() {
        return rootLayoutController;
    }

    public void setRootLayoutController(RootLayoutController rootLayoutController) {
        this.rootLayoutController = rootLayoutController;
    }

    // ### Methods #####################################################################

    /**
     * Open broken state ConnFile object (file).
     * The broken state of object on which this method is called
     * is guaranteed by the disable property of the related contextual menu (Open File).
     */
    private void openBrokenConnFile() {
        rootLayoutController.connFileOpen(this);
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

        if (ConnEditor.INSTANCE.supply(conn, DIALOG_NEW_CONNECTION_TITLE)) {
            getSubUnits().add(conn);
            //this.createAndAddSubUnit("Hello, world!");
            saveConnDataToFile();
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

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(ALERT_SAVE_DATA_TITLE);
            alert.setHeaderText(ALERT_SAVE_DATA_HEADER);
            alert.setContentText(ALERT_SAVE_DATA_CONTENT + file.getPath());

            alert.showAndWait();
        }
    }

}
