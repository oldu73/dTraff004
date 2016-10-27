package ch.stageconcept.dtraff.connection.model;

import javafx.beans.property.*;

import java.sql.Connection;
import java.sql.ResultSet;

/**
 * Model class for a server connection.
 *
 * @author Olivier Durand
 */
public class DbConnect {

    // Attributes
    // #####################################################################

    private final StringProperty driver;
    private final StringProperty host;
    private final IntegerProperty port;
    private final StringProperty user;
    private final StringProperty password;
    private final ObjectProperty<Connection> connection;
    private final ObjectProperty<ResultSet> resultSet;

    //TODO Connection name field
    //TODO Server Type (MySQL, MariaDB, PostgreSQL)
    //TODO Save password option
    //TODO SSL

    // Constructors
    // #####################################################################

    /**
     * Default constructor.
     */
    public DbConnect() {
        this(null, null, 0, null, null);
    }

    /**
     * Constructor.
     *
     * @param driver
     * @param host
     * @param port
     * @param user
     * @param password
     */
    public DbConnect(String driver, String host, Integer port, String user, String password) {
        this.driver = new SimpleStringProperty(driver);
        this.host = new SimpleStringProperty(host);
        this.port = new SimpleIntegerProperty(port);
        this.user = new SimpleStringProperty(user);
        this.password = new SimpleStringProperty(password);
        connection = null;
        resultSet = null;
    }

    // Methods
    // #####################################################################

    // Getters and Setters
    // #####################################################################

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
