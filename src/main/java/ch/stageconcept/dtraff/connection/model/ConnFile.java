package ch.stageconcept.dtraff.connection.model;

import ch.stageconcept.dtraff.connection.util.ConnEditor;
import ch.stageconcept.dtraff.connection.util.ConnListWrapper;
import ch.stageconcept.dtraff.connection.util.Crypto;
import ch.stageconcept.dtraff.connection.util.DbType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;

//TODO javadoc

public class ConnFile extends ConnUnit<Conn> {

    // ### Attributes #####################################################################

    private static final String ICON_FILENAME = "file001.png";

    // Reference to parent object
    private final ObjectProperty<Network> parent;

    private String fileName;
    private boolean isPasswordProtected = false;
    private String password;

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
       MenuItem newConnectionMenuItem = new MenuItem("New Connection");
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

       MenuItem closeFileMenuItem = new MenuItem("Close File");

       closeFileMenuItem.setOnAction((ActionEvent t) -> {
            //System.out.println("Close file on:" + this.getName());
            this.getParent().closeFile(this);
       });

       contextMenu.getItems().addAll(closeFileMenuItem, newConnectionMenuItem);
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

            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not save data");
            alert.setContentText("Could not save data to file:\n" + file.getPath());

            alert.showAndWait();
        }
    }
}
