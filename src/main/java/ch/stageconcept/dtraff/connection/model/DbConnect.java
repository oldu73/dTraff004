package ch.stageconcept.dtraff.connection.model;

import ch.stageconcept.dtraff.connection.util.DbDescriptor;
import ch.stageconcept.dtraff.connection.util.DbType;
import javafx.beans.property.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Model class for a server connection.
 *
 * @author Olivier Durand
 */
public class DbConnect {

    // Attributes
    // #####################################################################

    private static final int VALID_CONNECTION_CHECK_TIMEOUT_SECONDS = 5;

    private final StringProperty key;   // Key value to retrieve corresponding DbDescriptor object in DbType HashMap
    private final StringProperty name;  // Connection name
    private final StringProperty denomination;  // End user representation, MySQL instead of key value, mysql
    private final StringProperty host;
    private final IntegerProperty port;
    private final StringProperty user;
    private final StringProperty password;
    private final StringProperty driver;

    private final StringProperty baseUrl;

    private final ObjectProperty<Connection> connection;
    private final ObjectProperty<ResultSet> resultSet;

    //TODO Save password option
    //TODO SSL

    // Constructors
    // #####################################################################

    /**
     * Default constructor.
     */
    public DbConnect() {

        // Create object with default values chosen to be MySQL

        String dbDefaultTypeKey = DbType.MYSQL_KEY;

        DbDescriptor dbDescriptor = DbType.INSTANCE.getDbDescriptorMap().get(dbDefaultTypeKey);

        this.key = new SimpleStringProperty(dbDefaultTypeKey);

        this.name = new SimpleStringProperty(dbDescriptor.getName());
        this.denomination = new SimpleStringProperty(dbDescriptor.getDenomination());
        this.host = new SimpleStringProperty(dbDescriptor.getHost());
        this.port = new SimpleIntegerProperty(dbDescriptor.getPort());
        this.user = new SimpleStringProperty(dbDescriptor.getUser());
        this.password = new SimpleStringProperty(dbDescriptor.getPassword());
        this.driver = new SimpleStringProperty(dbDescriptor.getDriver());

        this.baseUrl = new SimpleStringProperty();

        this.connection = new SimpleObjectProperty<>();
        this.resultSet = new SimpleObjectProperty<>();
    }

    /**
     * Constructor.
     *
     * @param key
     * @param name
     * @param denomination
     * @param host
     * @param port
     * @param user
     * @param password
     * @param driver
     */
    public DbConnect(String key, String name, String denomination, String host, Integer port, String user, String password, String driver) {
        this.key = new SimpleStringProperty(key);
        this.name = new SimpleStringProperty(name);
        this.denomination = new SimpleStringProperty(denomination);
        this.host = new SimpleStringProperty(host);
        this.port = new SimpleIntegerProperty(port);
        this.user = new SimpleStringProperty(user);
        this.password = new SimpleStringProperty(password);
        this.driver = new SimpleStringProperty(driver);

        this.baseUrl = new SimpleStringProperty();

        this.connection = new SimpleObjectProperty<>();
        this.resultSet = new SimpleObjectProperty<>();
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
                //System.out.println("Connection closed!");
            }
    }

    // Getters and Setters
    // #####################################################################

    public String getBaseUrl() {
        return baseUrl.get();
    }

    public StringProperty baseUrlProperty() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl.set(baseUrl);
    }

    public String getKey() {
        return key.get();
    }

    public StringProperty keyProperty() {
        return key;
    }

    public void setKey(String key) {
        this.key.set(key);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getDenomination() {
        return denomination.get();
    }

    public StringProperty denominationProperty() {
        return denomination;
    }

    public void setDenomination(String denomination) {
        this.denomination.set(denomination);
    }

    public Connection getConnection() {
        return connection.get();
    }

    public ObjectProperty<Connection> connectionProperty() {
        return connection;
    }

    public void setConnection(Connection connection) {
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

    public String getDriver() {
        return driver.get();
    }

    public StringProperty driverProperty() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver.set(driver);
    }

    public String getHost() {
        return host.get();
    }

    public StringProperty hostProperty() {
        return host;
    }

    public void setHost(String host) {
        this.host.set(host);
    }

    public int getPort() {
        return port.get();
    }

    public IntegerProperty portProperty() {
        return port;
    }

    public void setPort(int port) {
        this.port.set(port);
    }

    public String getUser() {
        return user.get();
    }

    public StringProperty userProperty() {
        return user;
    }

    public void setUser(String user) {
        this.user.set(user);
    }

    public String getPassword() {
        return password.get();
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public void setPassword(String password) {
        this.password.set(password);
    }
}
