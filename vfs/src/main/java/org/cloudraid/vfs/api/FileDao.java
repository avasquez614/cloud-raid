package org.cloudraid.vfs.api;

import org.cloudraid.commons.exception.DaoException;

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
    File getFileById(Object id) throws DaoException;

    /**
     * Returns the file associated to the given path.
     *
     * @param path
     *          the path of the file or directory in the VFS
     * @return the file associated to the given path
     */
    File getFileByPath(String path) throws DaoException;

    /**
     * Saves the specified file. Also sets the generated ID in the file.
     *
     * @param file
     *          the file to save
     */
    void saveFile(File file) throws DaoException;

    /**
     * Updates the specified file.
     *
     * @param file
     *          the file to update
     */
    void updateFile(File file) throws DaoException;

    /**
     * Deletes the file associated to the given ID.
     *
     * @param id
     *          the ID of the file to delete
     */
    void deleteFile(Object id) throws DaoException;

}
