package org.cloudraid.jlan.db;

import org.alfresco.jlan.debug.Debug;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.filesys.db.DBDeviceContext;
import org.alfresco.jlan.server.filesys.db.mysql.MySQLDBInterface;
import org.springframework.extensions.config.ConfigElement;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Extends {@link MySQLDBInterface} to add a table for IDA file fragments.
 *
 * @author Alfonso Vasquez
 */
public class CloudRaidMySqlDbInterface extends MySQLDBInterface {

    public static final String IDA_FILE_FRAGMENTS_TABLE_NAME = "CloudRaidIdaFileFragments";

    protected String idaFileFragmentsTableName;

    @Override
    public void initializeDatabase(DBDeviceContext dbCtx, ConfigElement params) throws InvalidConfigurationException {
        // Set the JDBC driver class, must be set before the connection pool is created
        setDriverName("com.mysql.jdbc.Driver");

        // Call the base class to do the main initialization
        super.initializeDatabase(dbCtx, params);

        // Configure our own properties
        configure(params);

        // Force the autoReconnect to be enabled
        if (getDSNString().indexOf("?autoReconnect=") == -1 && params.getChild("noAutoReconnect") == null) {
            setDSNString(getDSNString() + "?autoReconnect=true");
        }

        // Create the database connection pool
        try {
            createConnectionPool();
        } catch (Exception ex) {
            // DEBUG
            if (Debug.EnableError && hasDebug()) {
                Debug.println("[mySQL] Error creating connection pool, " + ex.toString());
            }

            // Rethrow the exception
            throw new InvalidConfigurationException("Failed to create connection pool, " + ex.getMessage());
        }

        // Check if the file system table exists
        Connection conn = null;
        try {
            // Open a connection to the database
            conn = getConnection();

            DatabaseMetaData dbMeta = conn.getMetaData();
            ResultSet rs = dbMeta.getTables("", "", "", null);

            boolean foundStruct = false;
            boolean foundStream = false;
            boolean foundRetain = false;
            boolean foundQueue = false;
            boolean foundTrans = false;
            boolean foundData = false;
            boolean foundJarData = false;
            boolean foundObjId = false;
            boolean foundSymLink = false;
            boolean foundIdaFragments = false;

            while (rs.next()) {
                // Get the table name
                String tblName = rs.getString("TABLE_NAME");

                // Check if we found the filesystem structure or streams table
                if (tblName.equalsIgnoreCase(getFileSysTableName()))
                    foundStruct = true;
                else if (hasStreamsTableName() && tblName.equalsIgnoreCase(getStreamsTableName()))
                    foundStream = true;
                else if (hasRetentionTableName() && tblName.equalsIgnoreCase(getRetentionTableName()))
                    foundRetain = true;
                else if (hasDataTableName() && tblName.equalsIgnoreCase(getDataTableName()))
                    foundData = true;
                else if (hasJarDataTableName() && tblName.equalsIgnoreCase(getJarDataTableName()))
                    foundJarData = true;
                else if (hasQueueTableName() && tblName.equalsIgnoreCase(getQueueTableName()))
                    foundQueue = true;
                else if (hasTransactionTableName() && tblName.equalsIgnoreCase(getTransactionTableName()))
                    foundTrans = true;
                else if (hasObjectIdTableName() && tblName.equalsIgnoreCase(getObjectIdTableName()))
                    foundObjId = true;
                else if (hasSymLinksTableName() && tblName.equalsIgnoreCase(getSymLinksTableName()))
                    foundSymLink = true;
                else if (tblName.equalsIgnoreCase(getIdaFileFragmentsTableName()))
                    foundIdaFragments = true;
            }
            // Check if the file system structure table should be created
            if (foundStruct == false) {
                // Create the file system structure table
                Statement stmt = conn.createStatement();

                stmt.execute(
                        "CREATE TABLE " + getFileSysTableName() +
                        " (FileId INTEGER AUTO_INCREMENT, DirId INTEGER, FileName VARCHAR(255) BINARY NOT NULL, " +
                        "FileSize BIGINT, CreateDate BIGINT, ModifyDate BIGINT, AccessDate BIGINT, ChangeDate BIGINT, " +
                        "ReadOnly BIT, Archived BIT, Directory BIT, SystemFile BIT, Hidden BIT, IsSymLink BIT, " +
                        "Uid INTEGER, Gid INTEGER, Mode INTEGER, Deleted BIT NOT NULL DEFAULT 0, " +
                        "PRIMARY KEY (FileId));"
                );

                // Create various indexes
                stmt.execute("ALTER TABLE " + getFileSysTableName() + " ADD UNIQUE INDEX IFileDirId (FileName,DirId);");
                stmt.execute("ALTER TABLE " + getFileSysTableName() + " ADD INDEX IDirId (DirId);");
                stmt.execute("ALTER TABLE " + getFileSysTableName() + " ADD INDEX IDir (DirId,Directory);");
                stmt.execute("ALTER TABLE " + getFileSysTableName() + " ADD UNIQUE INDEX IFileDirIdDir (FileName,DirId,Directory);");
                stmt.close();

                // DEBUG
                if (Debug.EnableInfo && hasDebug())
                    Debug.println("[mySQL] Created table " + getFileSysTableName());
            }
            // Check if the file streams table should be created
            if (isNTFSEnabled() && foundStream == false && getStreamsTableName() != null) {
                // Create the file streams table
                Statement stmt = conn.createStatement();

                stmt.execute(
                        "CREATE TABLE " + getStreamsTableName() +
                        " (StreamId INTEGER AUTO_INCREMENT, FileId INTEGER NOT NULL, " +
                        "StreamName VARCHAR(255) BINARY NOT NULL, StreamSize BIGINT, " +
                        "CreateDate BIGINT, ModifyDate BIGINT, AccessDate BIGINT, PRIMARY KEY (StreamId));"
                );

                // Create various indexes
                stmt.execute("ALTER TABLE " + getStreamsTableName() + " ADD INDEX IFileId (FileId);");
                stmt.close();

                // DEBUG
                if (Debug.EnableInfo && hasDebug())
                    Debug.println("[mySQL] Created table " + getStreamsTableName());
            }
            // Check if the retention table should be created
            if (isRetentionEnabled() && foundRetain == false && getRetentionTableName() != null) {
                // Create the retention period data table
                Statement stmt = conn.createStatement();

                stmt.execute(
                        "CREATE TABLE " + getRetentionTableName() +
                        " (FileId INTEGER NOT NULL, StartDate TIMESTAMP, EndDate TIMESTAMP, " +
                        "PurgeFlag TINYINT(1), PRIMARY KEY (FileId));"
                );
                stmt.close();

                // DEBUG
                if (Debug.EnableInfo && hasDebug())
                    Debug.println("[mySQL] Created table " + getRetentionTableName());
            }
            // Check if the file loader queue table should be created
            if (isQueueEnabled() && foundQueue == false && getQueueTableName() != null) {
                // Create the request queue data table
                Statement stmt = conn.createStatement();

                stmt.execute(
                        "CREATE TABLE " + getQueueTableName() +
                                " (FileId INTEGER NOT NULL, StreamId INTEGER NOT NULL, ReqType SMALLINT, " +
                                "SeqNo INTEGER AUTO_INCREMENT, TempFile TEXT, VirtualPath TEXT, QueuedAt TIMESTAMP, " +
                                "Attribs VARCHAR(512), PRIMARY KEY (SeqNo));"
                );

                stmt.execute("ALTER TABLE " + getQueueTableName() + " ADD INDEX IFileId (FileId);");
                stmt.execute("ALTER TABLE " + getQueueTableName() + " ADD INDEX IFileIdType (FileId, ReqType);");

                stmt.close();

                // DEBUG
                if (Debug.EnableInfo && hasDebug())
                    Debug.println("[mySQL] Created table " + getQueueTableName());
            }
            // Check if the file loader transaction queue table should be created
            if (isQueueEnabled() && foundTrans == false && getTransactionTableName() != null) {
                // Create the transaction request queue data table
                Statement stmt = conn.createStatement();

                stmt.execute(
                        "CREATE TABLE " + getTransactionTableName() +
                        " (FileId INTEGER NOT NULL, StreamId INTEGER NOT NULL, " +
                        "TranId INTEGER NOT NULL, ReqType SMALLINT, TempFile TEXT, VirtualPath TEXT, " +
                        "QueuedAt TIMESTAMP, Attribs VARCHAR(512), PRIMARY KEY (FileId,StreamId,TranId));"
                );

                stmt.close();

                // DEBUG
                if (Debug.EnableInfo && hasDebug())
                    Debug.println("[mySQL] Created table " + getTransactionTableName());
            }
            // Check if the file data table should be created
            if (isDataEnabled() && foundData == false && hasDataTableName()) {
                // Create the file data table
                Statement stmt = conn.createStatement();

                stmt.execute(
                        "CREATE TABLE " + getDataTableName() +
                        " (FileId INTEGER NOT NULL, StreamId INTEGER NOT NULL, FragNo INTEGER, FragLen INTEGER, " +
                        "Data LONGBLOB, JarFile BIT, JarId INTEGER);"
                );

                stmt.execute("ALTER TABLE " + getDataTableName() + " ADD INDEX IFileStreamId (FileId,StreamId);");
                stmt.execute("ALTER TABLE " + getDataTableName() + " ADD INDEX IFileId (FileId);");
                stmt.execute("ALTER TABLE " + getDataTableName() + " ADD INDEX IFileIdFrag (FileId,FragNo);");

                stmt.close();

                // DEBUG
                if (Debug.EnableInfo && hasDebug())
                    Debug.println("[mySQL] Created table " + getDataTableName());
            }
            // Check if the Jar file data table should be created
            if (isJarDataEnabled() && foundJarData == false && hasJarDataTableName()) {
                // Create the Jar file data table
                Statement stmt = conn.createStatement();

                stmt.execute(
                        "CREATE TABLE " + getJarDataTableName() +
                        " (JarId INTEGER AUTO_INCREMENT, Data LONGBLOB, PRIMARY KEY (JarId));"
                );

                stmt.close();

                // DEBUG
                if (Debug.EnableInfo && hasDebug())
                    Debug.println("[mySQL] Created table " + getJarDataTableName());
            }
            // Check if the file id/object id mapping table should be created
            if (isObjectIdEnabled() && foundObjId == false && hasObjectIdTableName()) {
                // Create the file id/object id mapping table
                Statement stmt = conn.createStatement();

                // ***** CLOUD RAID CHANGE *****
                // ObjecId size changed from 128 to 255 (doc states its size is 255)
                stmt.execute(
                        "CREATE TABLE " + getObjectIdTableName() +
                        " (FileId INTEGER NOT NULL, StreamId INTEGER NOT NULL, ObjectId VARCHAR(255), " +
                        "PRIMARY KEY (FileId,StreamId));"
                );

                stmt.close();

                // DEBUG
                if (Debug.EnableInfo && hasDebug())
                    Debug.println("[mySQL] Created table " + getObjectIdTableName());
            }
            // Check if the symbolic links table should be created
            if (isSymbolicLinksEnabled() && foundSymLink == false && hasSymLinksTableName()) {
                // Create the symbolic links table
                Statement stmt = conn.createStatement();

                stmt.execute(
                        "CREATE TABLE " + getSymLinksTableName() +
                        " (FileId INTEGER NOT NULL PRIMARY KEY, SymLink VARCHAR(8192));"
                );

                stmt.close();

                // DEBUG
                if (Debug.EnableInfo && hasDebug())
                    Debug.println("[mySQL] Created table " + getSymLinksTableName());
            }
            // Check if the IDA file fragments table should be created
            if (foundIdaFragments == false && hasIdaFileFragmentsTableName()) {
                // Create the IDA file fragments table
                Statement stmt = conn.createStatement();

                stmt.execute(
                        "CREATE TABLE " + getIdaFileFragmentsTableName() +
                        " (ObjectId VARCHAR(255) NOT NULL, FragmentId INTEGER AUTO_INCREMENT, " +
                        "CloudNodeUrl VARCHAR(2000), PRIMARY KEY (ObjectId, FragmentId));"
                );

                stmt.execute("ALTER TABLE " + getDataTableName() + " ADD INDEX IObjectId (ObjectId);");

                // DEBUG
                if (Debug.EnableInfo && hasDebug())
                    Debug.println("[mySQL] Created table " + getIdaFileFragmentsTableName());
            }
        } catch (Exception ex) {
            Debug.println("Error: " + ex.toString());
        } finally {
            // Release the database connection
            if (conn != null)
                releaseConnection(conn);
        }
    }

    /**
     * Returns the name of the IDA File Fragments table.
     */
    protected String getIdaFileFragmentsTableName() {
        return idaFileFragmentsTableName;
    }

    /**
     * Returns true if this interface has an IDA File Fragments table name.
     */
    protected boolean hasIdaFileFragmentsTableName() {
        return idaFileFragmentsTableName != null;
    }

    protected void configure(ConfigElement params) throws InvalidConfigurationException {
        ConfigElement configElement = params.getChild("IdaFileFragmentsTable");
        if (configElement != null) {
            idaFileFragmentsTableName = configElement.getValue();
        } else {
            idaFileFragmentsTableName = IDA_FILE_FRAGMENTS_TABLE_NAME;
        }
    }

}
