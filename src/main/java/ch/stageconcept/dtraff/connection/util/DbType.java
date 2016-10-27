package ch.stageconcept.dtraff.connection.util;

/**
 * Enum class to get database driver representation string from key value
 *
 * @author Olivier Durand
 */
public enum DbType {

    INSTANCE;

    //TODO Add default port number and every other fields with default values

    // DB Keys and Values
    private static final String K_MARIADB = "MariaDB";
    private static final String V_MARIADB = "org.mariadb.jdbc.Driver";

    private static final String K_MYSQL = "MySQL";
    private static final String V_MYSQL = "com.mysql.jdbc.Driver";

    private static final String K_POSTGRESQL = "PostgreSQL";
    private static final String V_POSTGRESQL = "org.postgresql.Driver";

}
