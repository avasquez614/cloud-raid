package org.cloudraid.jlan.db;

import org.alfresco.jlan.debug.Debug;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.filesys.db.DBDeviceContext;
import org.alfresco.jlan.server.filesys.db.mysql.MySQLDBInterface;
import org.cloudraid.ida.persistance.api.FragmentMetaData;
import org.cloudraid.ida.persistance.api.FragmentMetaDataRepository;
import org.cloudraid.ida.persistance.exception.RepositoryException;
import org.springframework.extensions.config.ConfigElement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Extends {@link MySQLDBInterface} to add a table for IDA fragment records.
 *
 * @author avasquez
 */
public class CloudRaidMySqlDbInterface extends MySQLDBInterface implements FragmentMetaDataRepository {

    public static final String DEFAULT_IDA_FRAGMENTS_TABLE_NAME = "CloudRaidIdaFragments";

    protected String idaFragmentsTableName;

    /**
     * Just like {@link MySQLDBInterface#initializeDatabase(org.alfresco.jlan.server.filesys.db.DBDeviceContext,
     * org.springframework.extensions.config.ConfigElement)}, but also adds a table for IDA fragment records.
     *
     * @param dbCtx
     * @param params
     * @throws InvalidConfigurationException
     */
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
                else if (tblName.equalsIgnoreCase(getIdaFragmentsTableName()))
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
            // Check if the IDA fragments table should be created
            if (foundIdaFragments == false && hasIdaFragmentsTableName()) {
                // Create the IDA fragments table
                Statement stmt = conn.createStatement();

                stmt.execute(
                        "CREATE TABLE " + getIdaFragmentsTableName() +
                        " (ObjectId VARCHAR(255) NOT NULL, FragmentNumber INTEGER NOT NULL, " +
                        "FragmentRepositoryUrl VARCHAR(2000) NOT NULL, PRIMARY KEY (ObjectId, FragmentNumber));"
                );

                stmt.execute("ALTER TABLE " + getIdaFragmentsTableName() + " ADD INDEX IObjectId (ObjectId);");

                // DEBUG
                if (Debug.EnableInfo && hasDebug())
                    Debug.println("[mySQL] Created table " + getIdaFragmentsTableName());
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
     * Returns the name of the IDA fragments table.
     */
    protected String getIdaFragmentsTableName() {
        return idaFragmentsTableName;
    }

    /**
     * Returns true if this interface has an IDA fragments table name.
     */
    protected boolean hasIdaFragmentsTableName() {
        return idaFragmentsTableName != null;
    }

    /**
     * Configures the current object properties according to the params.
     *
     * @param params
     * @throws InvalidConfigurationException
     */
    protected void configure(ConfigElement params) throws InvalidConfigurationException {
        ConfigElement configElement = params.getChild("IdaFragmentsTable");
        if (configElement != null) {
            idaFragmentsTableName = configElement.getValue();
        } else {
            idaFragmentsTableName = DEFAULT_IDA_FRAGMENTS_TABLE_NAME;
        }
    }

    /**
     * Inserts the given {@link FragmentMetaData} into the database.
     *
     * @param metaData
     *          the metaData to insert.
     * @throws RepositoryException
     */
    @Override
    public void saveFragmentMetaData(FragmentMetaData metaData) throws RepositoryException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(
                    "INSERT INTO " + getIdaFragmentsTableName() +
                    "VALUES (?, ?, ?)"
            );

            pstmt.setString(1, metaData.getDataId());
            pstmt.setInt(2, metaData.getFragmentNumber());
            pstmt.setString(3, metaData.getRepositoryUrl());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("SQL error while trying to insert metadata record " + metaData, e);
        } finally {
            closeQuietly(pstmt);
            closeQuietly(conn);
        }
    }

    /**
     * Selects the metadata for all fragments of the given data ID.
     *
     * @param dataId
     *          the data ID
     * @return the list of fragment metadata
     * @throws RepositoryException
     */
    @Override
    public List<FragmentMetaData> getAllFragmentMetaDataForData(String dataId) throws RepositoryException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        List<FragmentMetaData> fragmentsMetaData = new ArrayList<FragmentMetaData>();

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(
                    "SELECT * " +
                    "FROM " + getIdaFragmentsTableName() +
                    "WHERE ObjectId = ?"
            );

            pstmt.setString(1, dataId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                fragmentsMetaData.add(mapFragmentMetaDataRow(rs));
            }

            return fragmentsMetaData;
        } catch (SQLException e) {
            throw new RepositoryException("SQL error while querying metadata for fragments of data ID '" + dataId + "'");
        } finally {
            closeQuietly(pstmt);
            closeQuietly(conn);
        }
    }

    /**
     * Selects the single metadata record for the given data ID and fragment number.
     *
     * @param dataId
     *          the data ID
     * @param fragmentNumber
     *          the fragment number
     * @return the single metadata record
     * @throws RepositoryException
     */
    @Override
    public FragmentMetaData getFragmentMetaData(String dataId, int fragmentNumber) throws RepositoryException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        FragmentMetaData metaData = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(
                    "SELECT * " +
                    "FROM " + getIdaFragmentsTableName() +
                    "WHERE ObjectId = ? AND FragmentNumber = ?"
            );

            pstmt.setString(1, dataId);
            pstmt.setInt(2, fragmentNumber);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                metaData = mapFragmentMetaDataRow(rs);
            }

            return metaData;
        } catch (SQLException e) {
            throw new RepositoryException("SQL error while querying metadata for data ID '" + dataId + "' and fragment " +
                    "number " + fragmentNumber);
        } finally {
            closeQuietly(pstmt);
            closeQuietly(conn);
        }
    }

    protected FragmentMetaData mapFragmentMetaDataRow(ResultSet rs) throws SQLException {
        String dataId = rs.getString("ObjectId");
        int fragmentNumber = rs.getInt("FragmentNumber");
        String repositoryUrl = rs.getString("FragmentRepositoryUrl");

        return new FragmentMetaData(dataId, fragmentNumber, repositoryUrl);
    }

    protected void closeQuietly(PreparedStatement pstmt) {
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
            }
        }
    }

    protected void closeQuietly(Connection conn) {
        if (conn != null) {
            releaseConnection(conn);
        }
    }

}
