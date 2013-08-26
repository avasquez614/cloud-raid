package org.cloudraid.vfs.api;

import org.cloudraid.commons.exception.DaoException;

import java.util.List;

/**
 * Data access object for {@link File}s.
 *
 * @author avasquez
 */
public interface FileDao {

    /**
     * Returns the file associated to the given ID.
     *
     * @param id
     *          the ID of the file
     * @return the file associated to the given ID, or null if not found
     */
    File findById(Object id) throws DaoException;

    /**
     * Returns the file associated to the given path.
     *
     * @param path
     *          the path of the file or directory in the VFS
     * @return the file associated to the given path
     */
    File findByPath(String path) throws DaoException;

    /**
     * Returns the files and directories under the directory specified by the path.
     *
     * @param dirPath
     *          the path of the directory
     * @return the list of children
     */
    List<File> findDirChildrenByPath(String dirPath) throws DaoException;

    /**
     * Saves the specified file. Also sets the generated ID in the file.
     *
     * @param file
     *          the file to save
     */
    void save(File file) throws DaoException;

    /**
     * Updates the specified file.
     *
     * @param file
     *          the file to update
     */
    void update(File file) throws DaoException;

    /**
     * Deletes the file associated to the given ID.
     *
     * @param id
     *          the ID of the file to delete
     */
    void delete(Object id) throws DaoException;

}
