package ch.stageconcept.dtraff.connection.model;

import ch.stageconcept.dtraff.connection.util.ConnEditor;
import ch.stageconcept.dtraff.connection.util.DbDescriptor;
import ch.stageconcept.dtraff.connection.util.DbType;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.jasypt.util.text.StrongTextEncryptor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Model class for a server connection represented in a treeView.
 *
 * @author Olivier Durand
 */

// SRC: http://stackoverflow.com/questions/10012542/moxy-jaxb-how-to-exclude-elements-from-marshalling
// SRC: http://stackoverflow.com/questions/15415136/jaxb-xmlattribute-xmlvalue-real-example

@XmlAccessorType(XmlAccessType.NONE)
public class Conn extends ConnUnit<DataBase> {

    // Attributes
    // #####################################################################

    private static final String ICON_FILENAME = "serverDefault001.gif";
    private static final int VALID_CONNECTION_CHECK_TIMEOUT_SECONDS = 5;
    private static final String MENU_EDIT_CONNECTION = "Edit Connection";

    // Reference to parent object
    private final ObjectProperty<ConnFile> parent;

    /**
     * Instance of StrongTextEncryptor which is a utility class for easily performing high-strength encryption of texts
     */
    private static final StrongTextEncryptor strongTextEncryptor = new StrongTextEncryptor();
    private final BooleanProperty passwordEncrypted;

    private final StringProperty key;   // Key value to retrieve corresponding DbDescriptor object in DbType HashMap
    private final StringProperty denomination;  // End user representation, MySQL instead of key value, mysql
    private final StringProperty host;
    private final IntegerProperty port;
    private final StringProperty user;
    private final StringProperty password;
    private final StringProperty driver;
    private final StringProperty baseUrl;

    private final ObjectProperty<Connection> connection;
    private final ObjectProperty<ResultSet> resultSet;

    // Constructors
    // #####################################################################

    /**
     * Constructor.
     *
     * @param name
     * @param subUnits
     */
    public Conn(String name, ObservableList<DataBase> subUnits) {

        //#####
        super(name, subUnits, DataBase::new, ICON_FILENAME);

        //#####
        // treeView context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editConnectionMenuItem = new MenuItem(MENU_EDIT_CONNECTION);
        editConnectionMenuItem.setOnAction((ActionEvent t) -> {
            ConnEditor.INSTANCE.supply(this);});
        contextMenu.getItems().add(editConnectionMenuItem);
        this.setMenu(contextMenu);

        //#####
        // Create object with default values chosen to be MySQL

        String dbDefaultTypeKey = DbType.MYSQL_KEY;

        DbDescriptor dbDescriptor = DbType.INSTANCE.getDbDescriptorMap().get(dbDefaultTypeKey);

        this.key = new SimpleStringProperty(dbDefaultTypeKey);

        this.denomination = new SimpleStringProperty(dbDescriptor.getDenomination());
        this.host = new SimpleStringProperty(dbDescriptor.getHost());
        this.port = new SimpleIntegerProperty(dbDescriptor.getPort());
        this.user = new SimpleStringProperty(dbDescriptor.getUser());
        this.password = new SimpleStringProperty(dbDescriptor.getPassword());
        this.driver = new SimpleStringProperty(dbDescriptor.getDriver());

        this.baseUrl = new SimpleStringProperty();

        this.connection = new SimpleObjectProperty<>();
        this.resultSet = new SimpleObjectProperty<>();
        this.parent = new SimpleObjectProperty<>();
        this.passwordEncrypted = new SimpleBooleanProperty(false);
    }

    /**
     * Constructor.
     *
     * @param name
     */
   public Conn(String name) {
        this(name, FXCollections.observableArrayList());
    }

    /**
     * Default Constructor (for JAXB).
     *
     */
    public Conn() {
        this(null, FXCollections.observableArrayList());
    }

    // Methods
    // #####################################################################

    /**
     * Establish database connection.
     * @return true if the connection is OK
     */
    public boolean doConnect() throws SQLException {

        if (getConnection() == null || getConnection().isClosed()) {
            setConnection(DriverManager.getConnection(getBaseUrl(), getUser(), getPassword()));

            // SRC: http://stackoverflow.com/questions/7764671/java-jdbc-connection-status
            return getConnection().isValid(VALID_CONNECTION_CHECK_TIMEOUT_SECONDS);
        }

        return false;
    }

    /**
     * Close database connection.
     */
    public void undoConnect() throws SQLException {
        if (getConnection() != null && !getConnection().isClosed()) {
            getConnection().close();
            //System.out.println("Conn closed!");
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("nam: " + this.getName()
                + "\nkey: " + this.getKey()
                + "\nden: " + this.getDenomination()
                + "\nhst: " + this.getHost()
                + "\nprt: " + this.getPort()
                + "\nusr: " + this.getUser()
                + "\npwd: " + this.getPassword()
                + "\ndrv: " + this.getDriver()
                + "\nurl: " + this.getBaseUrl()
                + "\n_____________________________________"
                + "\n");
        return sb.toString();
    }

    // Getters and Setters
    // #####################################################################

    @XmlElement
    public boolean isPasswordEncrypted() {
        return passwordEncrypted.get();
    }

    public BooleanProperty passwordEncryptedProperty() {
        return passwordEncrypted;
    }

    public void setPasswordEncrypted(boolean passwordEncrypted) {
        this.passwordEncrypted.set(passwordEncrypted);
    }

    public ConnFile getParent() {
        return parent.get();
    }

    public ObjectProperty<ConnFile> parentProperty() {
        return parent;
    }

    public void setParent(ConnFile parent) {
        this.parent.set(parent);
    }

    @XmlElement
    public String getBaseUrl() {
            return baseUrl.get();
        }

    public StringProperty baseUrlProperty() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl.set(baseUrl);
    }

    @XmlElement
    public String getKey() {
        return key.get();
    }

    public StringProperty keyProperty() {
        return key;
    }

    public void setKey(String key) {
        this.key.set(key);
    }

    @XmlElement
    public String getDenomination() {
        return denomination.get();
    }

    public StringProperty denominationProperty() {
        return denomination;
    }

    public void setDenomination(String denomination) {
        this.denomination.set(denomination);
    }

    public java.sql.Connection getConnection() {
        return connection.get();
    }

    public ObjectProperty<java.sql.Connection> connectionProperty() {
        return connection;
    }

    public void setConnection(java.sql.Connection connection) {
        this.connection.set(connection);
    }

    public ResultSet getResultSet() {
        return resultSet.get();
    }

    public ObjectProperty<ResultSet> resultSetProperty() {
        return resultSet;
    }

    public void setResultSet(ResultSet resultSet) {
        this.resultSet.set(resultSet);
    }

    @XmlElement
    public String getDriver() {
        return driver.get();
    }

    public StringProperty driverProperty() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver.set(driver);
    }

    @XmlElement
    public String getHost() {
        return host.get();
    }

    public StringProperty hostProperty() {
        return host;
    }

    public void setHost(String host) {
        this.host.set(host);
    }

    @XmlElement
    public int getPort() {
        return port.get();
    }

    public IntegerProperty portProperty() {
        return port;
    }

    public void setPort(int port) {
        this.port.set(port);
    }

    @XmlElement
    public String getUser() {
        return user.get();
    }

    public StringProperty userProperty() {
        return user;
    }

    public void setUser(String user) {
        this.user.set(user);
    }

    @XmlElement
    public String getPassword() {
        return password.get();
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public void setPassword(String password) {
        /*
        if (this.getParent().isPasswordProtected()) {
            this.setPasswordEncrypted(true);
            strongTextEncryptor.setPassword(this.getParent().getPassword());
            this.password.set(strongTextEncryptor.encrypt(password));
        } else {
            this.password.set(password);
        }
        */

        this.password.set(password);

    }

}
