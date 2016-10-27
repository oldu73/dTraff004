package ch.stageconcept.dtraff.connection.util;

import java.util.HashMap;

/**
 * Enum class to get database description from key value.
 *
 * @author Olivier Durand
 */
public enum DbType {

    INSTANCE;

    // Connection and DB Keys - Values ###

    // Default values
    private static final String DEFAULT_NAM = "default";
    private static final String DEFAULT_HOS = "127.0.0.1";
    private static final String DEFAULT_USE = "root";
    private static final String DEFAULT_PAS = "root";

    // MariaDB
    private static final String MARIADB_KEY = "mariadb";
    private static final String MARIADB_DEN = "MariaDB";
    private static final String MARIADB_DRI = "org.mariadb.jdbc.Driver";
    private static final int MARIADB_POR = 3306;

    // MySQL
    private static final String MYSQL_KEY = "mysql";
    private static final String MYSQL_DEN = "MySQL";
    private static final String MYSQL_DRI = "com.mysql.jdbc.Driver";
    private static final int MYSQL_POR = 3306;

    // PostgreSQL
    private static final String POSTGRESQL_KEY = "postgresql";
    private static final String POSTGRESQL_DEN = "PostgreSQL";
    private static final String POSTGRESQL_DRI = "org.postgresql.Driver";
    private static final int POSTGRESQL_POR = 5432;

    private final HashMap<String, DbDescriptor> dbDescriptorHashMap;

    private DbType() {
        DbDescriptor mariaDb = new DbDescriptor(DEFAULT_NAM, MARIADB_DEN, DEFAULT_HOS, MARIADB_POR, DEFAULT_USE, DEFAULT_PAS, MARIADB_DRI);
        DbDescriptor mySql = new DbDescriptor(DEFAULT_NAM, MYSQL_DEN, DEFAULT_HOS, MYSQL_POR, DEFAULT_USE, DEFAULT_PAS, MYSQL_DRI);
        DbDescriptor postgreSql = new DbDescriptor(DEFAULT_NAM, POSTGRESQL_DEN, DEFAULT_HOS, POSTGRESQL_POR, DEFAULT_USE, DEFAULT_PAS, POSTGRESQL_DRI);

        dbDescriptorHashMap = new HashMap<>();

        dbDescriptorHashMap.put(MARIADB_KEY, mariaDb);
        dbDescriptorHashMap.put(MYSQL_KEY, mySql);
        dbDescriptorHashMap.put(POSTGRESQL_KEY, postgreSql);
    }

    public HashMap<String, DbDescriptor> getDbDescriptorHashMap() {
        return dbDescriptorHashMap;
    }

}
