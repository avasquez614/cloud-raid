package org.cloudraid.vfs.api;

import java.util.Date;

/**
 * Represents a file or directory metadata in the virtual filesystem.
 *
 * @author avasquez
 */
public class File {

    private Object id;
    private String path;
    private int uid;
    private int guid;
    private long size;
    private long mode;
    private boolean dir;
    private Date createdDate;
    private Date accessDate;
    private Date changeDate;
    private long chunkSize;
    private Object parentDirId;
    private Object symLinkTargetId;

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getGuid() {
        return guid;
    }

    public void setGuid(int guid) {
        this.guid = guid;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getMode() {
        return mode;
    }

    public void setMode(long mode) {
        this.mode = mode;
    }

    public boolean isDir() {
        return dir;
    }

    public void setDir(boolean dir) {
        this.dir = dir;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getAccessDate() {
        return accessDate;
    }

    public void setAccessDate(Date accessDate) {
        this.accessDate = accessDate;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    public long getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(long chunkSize) {
        this.chunkSize = chunkSize;
    }

    public Object getParentDirId() {
        return parentDirId;
    }

    public void setParentDirId(Object parentDirId) {
        this.parentDirId = parentDirId;
    }

    public Object getSymLinkTargetId() {
        return symLinkTargetId;
    }

    public void setSymLinkTargetId(Object symLinkTargetId) {
        this.symLinkTargetId = symLinkTargetId;
    }

}
