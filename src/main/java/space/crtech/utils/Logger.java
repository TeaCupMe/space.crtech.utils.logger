package space.crtech.utils;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
//
//import space.crtech.utils.Formatter;
//import space.crtech.utils.FormatterOptions;

import static java.lang.Math.min;

public class Logger {

    Map<LogType, String> logTypeIntToString = Map.of(
            LogType.LOG_TYPE_DEFAULT, "DEFAULT",
            LogType.LOG_TYPE_ERROR, "ERR",
            LogType.LOG_TYPE_OK, "OK",
            LogType.LOG_TYPE_WARNING, "WARN",
            LogType.LOG_TYPE_INFO, "INFO",
            LogType.LOG_TYPE_WEAK, "WEAK"

    );
    FormatterOptions formatError = new FormatterOptions("B", "R");
    FormatterOptions formatWarning = new FormatterOptions("", "Y");
    FormatterOptions formatInfo = new FormatterOptions("", "D");
    FormatterOptions formatWeak = new FormatterOptions("", "W");
    FormatterOptions formatSuccess = new FormatterOptions("B", "G");

    PrintStream debugOutput = System.out;


    /**
     * logLevel: </br>
     * 0 - Off </br>
     * 1 - Errors and Success </br>
     * 2 - Warnings </br>
     * 3 - Info </br>
     * 4 - Weak
     */
    int logLevel = 4;
    boolean logStackTrace = true;
    boolean logTimeStamp = true;
    boolean logLogType = true;
    boolean logTag = true;

    int stackTraceDepth = 2;

    ArrayList<String> tagsWhiteList = new ArrayList<>();
    ArrayList<String> tagsBlackList = new ArrayList<>();
    boolean allowSelectedTags  = false;
    boolean dropSelectedTags = false;
    LogType type = LogType.LOG_TYPE_DEFAULT;
    String tag = "";

    public void setStackDepth(int depth) {
        this.stackTraceDepth = depth;
    }

    public void enableStacktraceLogging(boolean enable) {
        logStackTrace = enable;
    }

    public void enableTimestampLogging(boolean enable) {
        logTimeStamp = enable;
    }

    public void enableLogTypeLogging(boolean enable) {
        logLogType = enable;
    }

    public void enableTagLogging(boolean enable) {
        logTag = enable;
    }

    public void addTagToBlackList(String tag) {
        this.tagsBlackList.add(tag);
    }

    public void addTagToWhiteList(String tag) {
        this.tagsWhiteList.add(tag);
    }

    public boolean removeTagFromBlackList(String tag) {
        return this.tagsBlackList.remove(tag);
    }

    public boolean removeTagFromWhiteList(String tag) {
        return this.tagsWhiteList.remove(tag);
    }

    public void enableTagsBlackList(boolean enable) {
        dropSelectedTags = enable;
    }

    public void enableTagsWhiteList(boolean enable) {
        allowSelectedTags = enable;
    }

    public void setDebugOutput(PrintStream debugOutput) {
        this.debugOutput = debugOutput;
    }

    String getTime() {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS").format(new java.util.Date());
    }

    public void logWeak(String message) {
        if (logLevel>3) {
            type = LogType.LOG_TYPE_WEAK;
            log(message, formatWeak);
        }
    }

    public void logError(String message) {
        if (logLevel>0) {
            type = LogType.LOG_TYPE_ERROR;
            log(message, formatError);
        }
    }

    public void logWarning(String message) {
        if (logLevel>1) {
            type = LogType.LOG_TYPE_WARNING;
            log(message, formatWarning);
        }
    }

    public void logInfo(String message) {
        if (logLevel>2) {
            type = LogType.LOG_TYPE_INFO;
            log(message, formatInfo);
        }
    }

    public void logSuccess(String message) {
        if (logLevel>0) {
            type = LogType.LOG_TYPE_OK;
            log(message, formatSuccess);
        }
    }

    @Deprecated
    private void log(String message) {
        log(message, formatInfo);
    }

    private void log(String message, FormatterOptions options) {

        // If selected tag is not marked as visible
        String selectedTag = consumeTag();
        if (allowSelectedTags && !tagsWhiteList.contains(selectedTag)) {
            return;
        }

        if (dropSelectedTags && tagsBlackList.contains(selectedTag)) {
            return;
        }

        // Builder for all prefix info: stack trace, timestamp, logType, tag, etc.
        StringBuilder prefixBuilder = new StringBuilder();

        // Structure: [stacktrace] + [timestamp] + [tag] + [logType] + <message>

        // Process stacktrace
        if (logStackTrace) {
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            prefixBuilder.append("Log from:");
            for (int i = min(3 + stackTraceDepth, stackTraceElements.length-1); i >= min(3, stackTraceElements.length-1); i--) {
                StackTraceElement stackTraceElement = stackTraceElements[i];
                prefixBuilder.append(":").append(stackTraceElement.getClassName()).append(".").append(stackTraceElement.getMethodName()).append("()");
            }
            prefixBuilder.append(" ");
        }

        // Process timestamp
        if (logTimeStamp) {
            prefixBuilder.append("[").append(getTime()).append("]");
        }
        debugOutput.print(Formatter.format(prefixBuilder.toString(), formatWeak));
        prefixBuilder.setLength(0);

        // Processing log type (Error, warning, weak, etc.)
        if (logLogType) {
            prefixBuilder.append("[").append(logTypeIntToString.get(type)).append("]");
        }

        // Processing log tag
        if (logTag && !selectedTag.isEmpty()) {
            prefixBuilder.append("[").append(selectedTag).append("]");
        }

        debugOutput.println(Formatter.format(prefixBuilder + " " + message, options));
    }

    public Logger tag(String tag) {
        this.tag = tag;
        return this;
    }

    private String consumeTag() {
        String tag = this.tag;
        this.tag = "";
        return tag;
    }

    public void logBare(String message, FormatterOptions options) { debugOutput.print(Formatter.format(message, options)); }

    private static final Logger instance = new Logger();
    public static Logger getLogger() {
        return instance;
    }

    public void setLogLevel(int logLevel) { this.logLevel = logLevel; }
}

