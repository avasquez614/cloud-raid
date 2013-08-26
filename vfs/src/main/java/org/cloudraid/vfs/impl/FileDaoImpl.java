package org.cloudraid.vfs.impl;

import org.cloudraid.commons.exception.DaoException;
import org.cloudraid.commons.exception.DuplicateKeyException;
import org.cloudraid.vfs.api.File;
import org.cloudraid.vfs.api.FileDao;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link FileDao}, using Spring JDBC template.
 *
 * @author avasquez
 */
public class FileDaoImpl implements FileDao {

    private String selectFileByIdSql;
    private String selectFileByPathSql;
    private String updateFileSql;
    private String deleteFileSql;

    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert insertFile;
    private FileRowMapper fileRowMapper;

    public FileDaoImpl() {
        this.fileRowMapper = new FileRowMapper();
    }

    @Required
    public void setSelectFileByIdSql(String selectFileByIdSql) {
        this.selectFileByIdSql = selectFileByIdSql;
    }

    @Required
    public void setSelectFileByPathSql(String selectFileByPathSql) {
        this.selectFileByPathSql = selectFileByPathSql;
    }

    @Required
    public void setUpdateFileSql(String updateFileSql) {
        this.updateFileSql = updateFileSql;
    }

    @Required
    public void setDeleteFileSql(String deleteFileSql) {
        this.deleteFileSql = deleteFileSql;
    }

    @Required
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.insertFile = new SimpleJdbcInsert(dataSource).withTableName("Files").usingGeneratedKeyColumns("id");
    }

    @Override
    public File getFileById(Object id) throws DaoException {
        try {
            return jdbcTemplate.queryForObject(selectFileByIdSql, fileRowMapper, id);
        } catch (DataAccessException e) {
            throw new DaoException(e.getMessage(), e);
        }
    }

    @Override
    public File getFileByPath(String path) throws DaoException {
        try {
            return jdbcTemplate.queryForObject(selectFileByPathSql, fileRowMapper, path);
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
        params.put("createdDate", file.getCreatedDate());
        params.put("accessDate", file.getAccessDate());
        params.put("changeDate", file.getChangeDate());
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
            jdbcTemplate.update(updateFileSql,
                    file.getPath(),
                    file.getUid(),
                    file.getGuid(),
                    file.getSize(),
                    file.getMode(),
                    file.isDir(),
                    file.getCreatedDate(),
                    file.getAccessDate(),
                    file.getChangeDate(),
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
            jdbcTemplate.update(deleteFileSql, id);
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
            file.setCreatedDate(rs.getDate("createdDate"));
            file.setAccessDate(rs.getDate("accessDate"));
            file.setChangeDate(rs.getDate("changeDate"));
            file.setChunkSize(rs.getLong("chunkSize"));
            file.setParentDirId(rs.getObject("parentDirId"));
            file.setSymLinkTargetId(rs.getObject("symLinkTargetId"));

            return file;
        }

    }

}
