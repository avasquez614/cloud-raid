package org.cloudraid.vfs.dao;

import org.cloudraid.commons.exception.DaoException;
import org.cloudraid.vfs.api.Chunk;

import java.util.List;

/**
 * Data access object for {@link org.cloudraid.vfs.api.Chunk}s.
 *
 * @author avasquez
 */
public interface ChunkDao {

    /**
     * Returns the chunk associated to the given ID.
     *
     * @param id
     *          the ID of the chunk
     * @return the chunk associated to the given ID, or null if not found
     */
    Chunk findChunkById(Object id) throws DaoException;

    /**
     * Returns all chunks of the file associated to the given ID.
     *
     * @param fileId
     *          the file ID
     * @return all the chunks of the file, or null if no chunks for the file
     */
    List<Chunk> findChunksByFileId(Object fileId) throws DaoException;

    /**
     * Saves the specified chunk. Also sets the generated ID in the chunk.
     *
     * @param chunk
     *          the chunk to save
     */
    void saveChunk(Chunk chunk) throws DaoException;

    /**
     * Updates the specified chunk.
     *
     * @param chunk
     *          the chunk to update
     */
    void updateChunk(Chunk chunk) throws DaoException;

    /**
     * Deletes the chunk associated to the given ID.
     *
     * @param id
     *          the ID of the chunk to delete
     */
    void deleteChunk(Object id) throws DaoException;

}
