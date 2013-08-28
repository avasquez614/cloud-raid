package org.cloudraid.vfs.dao.jdbc;

import org.cloudraid.commons.exception.DaoException;
import org.cloudraid.commons.exception.DuplicateKeyException;
import org.cloudraid.vfs.api.Chunk;
import org.cloudraid.vfs.dao.ChunkDao;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link ChunkDao}, using Spring JDBC templates.
 *
 * @author avasquez
 */
public class JdbcChunkDao implements ChunkDao {

    private String findChunkByIdSql;
    private String findChunksByFileIdSql;
    private String updateChunkSql;
    private String deleteChunkSql;

    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert insertChunk;
    private ChunkRowMapper rowMapper;

    public JdbcChunkDao() {
        rowMapper = new ChunkRowMapper();
    }

    @Required
    public void setFindChunkByIdSql(String findChunkByIdSql) {
        this.findChunkByIdSql = findChunkByIdSql;
    }

    @Required
    public void setFindChunksByFileIdSql(String findChunksByFileIdSql) {
        this.findChunksByFileIdSql = findChunksByFileIdSql;
    }

    @Required
    public void setUpdateChunkSql(String updateChunkSql) {
        this.updateChunkSql = updateChunkSql;
    }

    @Required
    public void setDeleteChunkSql(String deleteChunkSql) {
        this.deleteChunkSql = deleteChunkSql;
    }

    @Required
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.insertChunk = new SimpleJdbcInsert(dataSource).withTableName("Chunks").usingGeneratedKeyColumns("id");
    }

    @Override
    public Chunk findChunkById(Object id) throws DaoException {
        try {
            return jdbcTemplate.queryForObject(findChunkByIdSql, rowMapper, id);
        } catch (DataAccessException e) {
            throw new DaoException(e.getMessage(), e);
        }
    }

    @Override
    public List<Chunk> findChunksByFileId(Object fileId) throws DaoException {
        try {
            return jdbcTemplate.query(findChunksByFileIdSql, rowMapper, fileId);
        } catch (DataAccessException e) {
            throw new DaoException(e.getMessage(), e);
        }
    }

    @Override
    public void saveChunk(Chunk chunk) throws DaoException {
        Map<String, Object> params = new HashMap<>(3);
        params.put("uuid", chunk.getUuid());
        params.put("index", chunk.getIndex());
        params.put("fileId", chunk.getFileId());

        Object id = null;
        try {
            insertChunk.executeAndReturnKey(params);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new DuplicateKeyException(e.getMessage(), e);
        } catch (DataAccessException e) {
            throw new DaoException(e.getMessage(), e);
        }

        chunk.setId(id);
    }

    @Override
    public void updateChunk(Chunk chunk) throws DaoException {
        try {
            jdbcTemplate.update(updateChunkSql,
                    chunk.getUuid(),
                    chunk.getIndex(),
                    chunk.getFileId(),
                    chunk.getId());
        } catch (DataAccessException e) {
            throw new DaoException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteChunk(Object id) throws DaoException {
        try {
            jdbcTemplate.update(deleteChunkSql, id);
        } catch (DataAccessException e) {
            throw new DaoException(e.getMessage(), e);
        }
    }

    private static class ChunkRowMapper implements RowMapper<Chunk> {

        @Override
        public Chunk mapRow(ResultSet rs, int rowNum) throws SQLException {
            Chunk chunk = new Chunk();
            chunk.setId(rs.getObject("id"));
            chunk.setUuid(rs.getString("uuid"));
            chunk.setIndex(rs.getInt("index"));
            chunk.setFileId(rs.getObject("fileId"));

            return chunk;
        }

    }

}
