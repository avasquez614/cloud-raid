package org.cloudraid.vfs.api;

/**
 * Represents a file chunk. Big files (probably several MB) are divided in chunks to more efficiently manage them.
 *
 * @author avasquez
 */
public class Chunk {

    private Object id;
    private String uuid;
    private int index;
    private Object fileId;

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Object getFileId() {
        return fileId;
    }

    public void setFileId(Object fileId) {
        this.fileId = fileId;
    }

}
