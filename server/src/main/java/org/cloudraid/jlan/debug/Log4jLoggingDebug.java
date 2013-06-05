package org.cloudraid.jlan.debug;

import org.alfresco.jlan.debug.Debug;
import org.alfresco.jlan.debug.DebugInterface;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.extensions.config.ConfigElement;

/**
 * {@link DebugInterface} that uses Log4j.
 */
public class Log4jLoggingDebug implements DebugInterface {

    private static final Logger logger = Logger.getLogger(Log4jLoggingDebug.class);

    protected ThreadLocal<StringBuilder> printBufferHolder;
    protected int logLevel;

    public Log4jLoggingDebug() {
        printBufferHolder = new ThreadLocal<StringBuilder>() {

            @Override
            protected StringBuilder initialValue() {
                return new StringBuilder();
            }

        };

        logLevel = Debug.Debug;
    }

    @Override
    public void close() {
    }

    @Override
    public void debugPrint(String str) {
        debugPrint(str, Debug.Debug);
    }

    @Override
    public void debugPrint(String str, int level) {
        if (level <= getLogLevel()) {
            printBufferHolder.get().append(str);
        }
    }

    @Override
    public void debugPrintln(String str) {
        debugPrintln(str, Debug.Debug);
    }

    @Override
    public void debugPrintln(String str, int level) {
        if (level <= getLogLevel()) {
            StringBuilder printBuffer = printBufferHolder.get();

            logOutput(printBuffer.append(str).toString(), level, null);

            printBufferHolder.set(new StringBuilder());
        }
    }

    @Override
    public void debugPrintln(Exception ex, int level) {
        if (level <= getLogLevel()) {
            logOutput("", level, ex);
        }
    }

    @Override
    public void initialize(ConfigElement params, ServerConfiguration config) throws Exception {
        ConfigElement logLevelConfig = params.getChild("logLevel");
        if (logLevelConfig != null) {
            String logLevelStr = logLevelConfig.getValue();
            if (logLevelStr != null) {
                if ( logLevelStr.equalsIgnoreCase("Debug")) {
                    logLevel = Debug.Debug;
                } else if ( logLevelStr.equalsIgnoreCase("Info")) {
                    logLevel = Debug.Info;
                } else if ( logLevelStr.equalsIgnoreCase("Warn")) {
                    logLevel = Debug.Warn;
                } else if ( logLevelStr.equalsIgnoreCase("Error")) {
                    logLevel = Debug.Error;
                } else if ( logLevelStr.equalsIgnoreCase("Fatal")) {
                    logLevel = Debug.Fatal;
                } else {
                    throw new IllegalArgumentException("Invalid debug logging level: " + logLevelStr);
                }
            }
        }

        ConfigElement logConfigXmlFileConfig = params.getChild("logConfigXmlFile");
        if (logConfigXmlFileConfig != null && StringUtils.isNotEmpty(logConfigXmlFileConfig.getValue())) {
            DOMConfigurator.configure(logConfigXmlFileConfig.getValue());
        } else {
            throw new IllegalArgumentException("No logConfigXmlFile specified");
        }
    }

    @Override
    public int getLogLevel() {
        return logLevel;
    }

    protected void logOutput(String msg, int level, Throwable t) {
        Level logLevel = Level.OFF;

        switch (level) {
            case Debug.Debug:
                logLevel = Level.DEBUG;
                break;
            case Debug.Info:
                logLevel = Level.INFO;
                break;
            case Debug.Warn:
                logLevel = Level.WARN;
                break;
            case Debug.Fatal:
                logLevel = Level.FATAL;
                break;
            case Debug.Error:
                logLevel = Level.ERROR;
                break;
        }

        if (t != null) {
            logger.log(logLevel, msg, t);
        } else {
            logger.log(logLevel, msg);
        }
    }

}
