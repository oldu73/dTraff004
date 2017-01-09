package ch.stageconcept.dtraff.connection.model;

import ch.stageconcept.dtraff.connection.util.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;

//TODO javadoc

public class ConnFile extends ConnUnit<Conn> {

    // ### Attributes #####################################################################

    private static final String ICON_FILENAME = ConnFileState.CLEAR.getIconFileName();

    private static final String MENU_NEW_CONNECTION = "New Connection";
    private static final String MENU_CLOSE_FILE = "Close File";

    private static final String ALERT_SAVE_DATA_TITLE = "Error";
    private static final String ALERT_SAVE_DATA_HEADER = "Could not save data";
    private static final String ALERT_SAVE_DATA_CONTENT = "Could not save data to file:\n";

    // Reference to parent object
    private final ObjectProperty<Network> parent;

    private String fileName;
    private boolean isPasswordProtected = false;
    private String password;
    private final ObjectProperty<ConnFileState> state;

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

       // treeView context menu
       ContextMenu contextMenu = new ContextMenu();
       MenuItem newConnectionMenuItem = new MenuItem(MENU_NEW_CONNECTION);
       newConnectionMenuItem.setOnAction((ActionEvent t) -> {
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

           if (ConnEditor.INSTANCE.supply(conn)) {
                subUnits.add(conn);
                //this.createAndAddSubUnit("Hello, world!");
                saveConnDataToFile();
           }

       });

       MenuItem closeFileMenuItem = new MenuItem(MENU_CLOSE_FILE);

       closeFileMenuItem.setOnAction((ActionEvent t) -> {
            //System.out.println("Close file on:" + this.getName());
            this.getParent().closeFile(this);
       });

       contextMenu.getItems().addAll(closeFileMenuItem, newConnectionMenuItem);
       this.setMenu(contextMenu);

       state = new SimpleObjectProperty<>(ConnFileState.CLEAR);

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

    // ### Methods #####################################################################

    /**
     * Saves the current connection data to file.
     *
     */
    private void saveConnDataToFile() {

        //TODO update file when conn object has been edited

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
