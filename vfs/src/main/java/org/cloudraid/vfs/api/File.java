package org.cloudraid.vfs.api;

import java.util.Date;

/**
 * Represents a file in the virtual filesystem.
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
    private Date lastAccess;
    private Date lastModified;
    private Date lastStatusChange;
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

    public Date getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(Date lastAccess) {
        this.lastAccess = lastAccess;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Date getLastStatusChange() {
        return lastStatusChange;
    }

    public void setLastStatusChange(Date lastStatusChange) {
        this.lastStatusChange = lastStatusChange;
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
