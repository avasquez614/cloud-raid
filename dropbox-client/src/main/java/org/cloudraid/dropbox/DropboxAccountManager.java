package org.cloudraid.dropbox;

import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.dropbox.client2.session.WebAuthSession;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Java CLI application that handles Dropbox accounts linked to the Cloud RAID Dropbox app.
 *
 * @author avasquez
 */
public class DropboxAccountManager {

    public static final String CONTEXT_FILE = "dropboxContext.xml";

    public static void main(String[] args) throws DropboxException {
        if (args.length == 0) {
            printUsage(System.out);
        } else {
            String command = args[0];
            if (command.equals("reset")) {
                doReset(args);
            } else if (command.equals("link")) {
                doLink(args);
            } else {
                System.err.println("ERROR: Unknown command: '" + command + "'");
                System.err.println("Run with no arguments for help.");
                die();
            }
        }
    }

    private static void printUsage(PrintStream out) {
        out.println("Usage:");
        out.println("    ./dropboxaccts reset <app-key> <secret>   Initialize the context with the given app key.");
        out.println("    ./dropboxaccts link                       Link an account to the app.");
    }

    /**
     * Resets the context with a new app key
     */
    private static void doReset(String[] args) throws DropboxException {
        if (args.length != 3) {
            die("ERROR: 'reset' takes exactly two arguments.");
        }

        AppKeyPair appKeyPair = new AppKeyPair(args[1], args[2]);

        DropboxContext context = new DropboxContext(appKeyPair);
        try {
            context.save(CONTEXT_FILE);
        } catch (IOException e) {
            die("ERROR: unable to save to context file '" + CONTEXT_FILE + "': " + e.getMessage());
        }
    }

    /**
     * Link an account to the app.
     */
    private static void doLink(String[] args) throws DropboxException {
        if (args.length != 1) {
            die("ERROR: 'link' takes no arguments.");
        }

        // Load context.
        DropboxContext context = new DropboxContext();
        try {
            context.load(CONTEXT_FILE);
        } catch (IOException e) {
            die("ERROR: unable to read context file '" + CONTEXT_FILE + "': " + e.getMessage());
        }

        WebAuthSession was = new WebAuthSession(context.getAppKeyPair(), Session.AccessType.DROPBOX);
        // Make the user log in and authorize us.
        WebAuthSession.WebAuthInfo info = was.getAuthInfo();
        System.out.println("1. Go to: " + info.url);
        System.out.println("2. Allow access to this app.");
        System.out.println("3. Press ENTER.");

        try {
            while (System.in.read() != '\n') {}
        } catch (IOException ex) {
            die("I/O error: " + ex.getMessage());
        }

        // This will fail if the user didn't visit the above URL and hit 'Allow'.
        String uid = was.retrieveWebAccessToken(info.requestTokenPair);
        AccessTokenPair accessTokenPair = was.getAccessTokenPair();
        System.out.println("Link successful.");

        context.getLinkedAccounts().put(uid, accessTokenPair);
        try {
            context.save(CONTEXT_FILE);
        } catch (IOException e) {
            die("ERROR: unable to save to context file '" + CONTEXT_FILE + "': " + e.getMessage());
        }
    }

    private static void die(String message) {
        System.err.println(message);
        die();
    }

    private static void die() {
        System.exit(-1);
    }

}
