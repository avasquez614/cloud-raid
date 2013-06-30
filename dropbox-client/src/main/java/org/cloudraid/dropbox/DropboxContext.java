package org.cloudraid.dropbox;

import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds Dropbox state information like app key pair and linked accounts.
 *
 * @author avasquez
 */
public class DropboxContext {

    private AppKeyPair appKeyPair;
    private Map<String, AccessTokenPair> linkedAccounts;

    public DropboxContext() {
    }

    public DropboxContext(AppKeyPair appKeyPair) {
        this.appKeyPair = appKeyPair;
        this.linkedAccounts = new HashMap<String, AccessTokenPair>();
    }

    public DropboxContext(AppKeyPair appKeyPair, Map<String, AccessTokenPair> linkedAccounts) {
        this.appKeyPair = appKeyPair;
        this.linkedAccounts = linkedAccounts;
    }

    /**
     * Returns the Cloud Raid Dropbox app key/secret pair.
     */
    public AppKeyPair getAppKeyPair() {
        return appKeyPair;
    }

    /**
     * Returns the list of linked accounts.
     */
    public Map<String, AccessTokenPair> getLinkedAccounts() {
        return linkedAccounts;
    }

    /**
     * Returns the {@link AccessTokenPair} for the given uid.
     */
    public AccessTokenPair getAccessTokenPair(String uid) {
        return linkedAccounts.get(uid);
    }

    /**
     * Loads the context from an XML file.
     *
     * @param fileName
     *          the name of the file to load the context from
     * @return the loaded context
     * @throws IOException
     */
    public void load(String fileName) throws IOException {
        XStream xstream = new XStream();
        addXStreamAliases(xstream);

        Reader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileName));
            xstream.fromXML(reader, this);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Saves the context to an XML file.
     *
     * @param fileName
     *          the name of the file to save the context to
     * @throws IOException
     */
    public void save(String fileName) throws IOException {
        XStream xstream = new XStream();
        addXStreamAliases(xstream);

        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            xstream.toXML(this, writer);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    private void addXStreamAliases(XStream xstream) {
        xstream.alias("dropboxContext", DropboxContext.class);
        xstream.alias("appKeyPair", AppKeyPair.class);
        xstream.alias("accessTokenPair", AccessTokenPair.class);
    }

}
