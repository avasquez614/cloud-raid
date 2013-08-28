package org.cloudraid.vfs.dao.jdbc;

import org.cloudraid.commons.exception.DaoException;
import org.cloudraid.commons.exception.DuplicateKeyException;
import org.cloudraid.vfs.api.File;
import org.cloudraid.vfs.dao.FileDao;
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
 * Default implementation of {@link org.cloudraid.vfs.dao.FileDao}, using Spring JDBC templates.
 *
 * @author avasquez
 */
public class JdbcFileDao implements FileDao {

    private String findByIdSql;
    private String findByPathSql;
    private String findDirChildrenByPathSql;
    private String updateSql;
    private String deleteSql;

    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert insertFile;
    private FileRowMapper fileRowMapper;

    public JdbcFileDao() {
        this.fileRowMapper = new FileRowMapper();
    }

    @Required
    public void setFindByIdSql(String findByIdSql) {
        this.findByIdSql = findByIdSql;
    }

    @Required
    public void setFindByPathSql(String findByPathSql) {
        this.findByPathSql = findByPathSql;
    }

    @Required
    public void setFindDirChildrenByPathSql(String findDirChildrenByPathSql) {
        this.findDirChildrenByPathSql = findDirChildrenByPathSql;
    }

    @Required
    public void setUpdateSql(String updateSql) {
        this.updateSql = updateSql;
    }

    @Required
    public void setDeleteSql(String deleteSql) {
        this.deleteSql = deleteSql;
    }

    @Required
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.insertFile = new SimpleJdbcInsert(dataSource).withTableName("Files").usingGeneratedKeyColumns("id");
    }

    @Override
    public File findFileById(Object id) throws DaoException {
        try {
            return jdbcTemplate.queryForObject(findByIdSql, fileRowMapper, id);
        } catch (DataAccessException e) {
            throw new DaoException(e.getMessage(), e);
        }
    }

    @Override
    public File findFileByPath(String path) throws DaoException {
        try {
            return jdbcTemplate.queryForObject(findByPathSql, fileRowMapper, path);
        } catch (DataAccessException e) {
            throw new DaoException(e.getMessage(), e);
        }
    }

    @Override
    public List<File> findDirChildrenByPath(String dirPath) throws DaoException {
        try {
            return jdbcTemplate.query(findDirChildrenByPathSql, fileRowMapper, dirPath);
        } catch (DataAccessException e) {
            throw new DaoException(e.getMessage(), e);
        }
    }

    @Override
    public void saveFile(File file) throws DaoException {
        Map<String, Object> params = new HashMap<>(12);
        params.put("path", file.getPath());
        params.put("uid", file.getUid());
        params.put("guid", file.getGuid());
        params.put("size", file.getSize());
        params.put("mode", file.getMode());
        params.put("isDir", file.isDir());
        params.put("lastAccess", file.getLastAccess());
        params.put("lastModified", file.getLastModified());
        params.put("lastStatusChange", file.getLastStatusChange());
        params.put("chunkSize", file.getChunkSize());
        params.put("parentDirId", file.getParentDirId());
        params.put("symLinkTargetId", file.getSymLinkTargetId());


        Object id = null;
        try {
            insertFile.executeAndReturnKey(params);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new DuplicateKeyException(e.getMessage(), e);
        } catch (DataAccessException e) {
            throw new DaoException(e.getMessage(), e);
        }

        file.setId(id);
    }

    @Override
    public void updateFile(File file) throws DaoException {
        try {
            jdbcTemplate.update(updateSql,
                    file.getPath(),
                    file.getUid(),
                    file.getGuid(),
                    file.getSize(),
                    file.getMode(),
                    file.isDir(),
                    file.getLastAccess(),
                    file.getLastModified(),
                    file.getLastStatusChange(),
                    file.getChunkSize(),
                    file.getParentDirId(),
                    file.getSymLinkTargetId());
        } catch (DataAccessException e) {
            throw new DaoException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(Object id) throws DaoException {
        try {
            jdbcTemplate.update(deleteSql, id);
        } catch (DataAccessException e) {
            throw new DaoException(e.getMessage(), e);
        }
    }

    private static class FileRowMapper implements RowMapper<File> {

        @Override
        public File mapRow(ResultSet rs, int rowNum) throws SQLException {
            File file = new File();
            file.setId(rs.getObject("id"));
            file.setPath(rs.getString("path"));
            file.setUid(rs.getInt("uid"));
            file.setGuid(rs.getInt("guid"));
            file.setSize(rs.getLong("size"));
            file.setMode(rs.getLong("mode"));
            file.setDir(rs.getBoolean("isDir"));
            file.setLastAccess(rs.getDate("lastAccess"));
            file.setLastModified(rs.getDate("lastModified"));
            file.setLastStatusChange(rs.getDate("lastStatusChange"));
            file.setChunkSize(rs.getLong("chunkSize"));
            file.setParentDirId(rs.getObject("parentDirId"));
            file.setSymLinkTargetId(rs.getObject("symLinkTargetId"));

            return file;
        }

    }

}
