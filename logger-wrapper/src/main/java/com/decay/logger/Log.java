/*
 * Copyright 2017 TomeOkin
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.decay.logger;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.LinkedList;

public class Log {
    private final String tag; // basic tag
    private int priority; // default message level
    private int filter; // release filter
    private boolean debug;
    private LinkedList<AbstractLog> logs = new LinkedList<>();
    private AndroidLog androidLog = null;

    /**
     * The minimum stack trace index, starts at this class after two native calls.
     */
    private static final int MIN_STACK_OFFSET = 3;

    // region create and config
    public Log(@NonNull String tag) {
        this.tag = tag;
        this.priority = android.util.Log.DEBUG;
        this.filter = android.util.Log.WARN;
        this.debug = BuildConfig.DEBUG;
    }

    public Log debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public boolean isDebug() {
        return debug;
    }

    public int getPriority() {
        return priority;
    }

    public Log priority(int priority) {
        this.priority = priority;
        return this;
    }

    public int getFilter() {
        return filter;
    }

    public Log filter(int filter) {
        this.filter = filter;
        return this;
    }

    public boolean isLoggable() {
        return debug || priority >= filter;
    }

    private boolean isLoggable(int priority) {
        return debug || priority >= filter;
    }
    // endregion

    // region custom logger
    public Log bindLogger(AbstractLog log) {
        log.prepareLogger(tag);
        logs.add(log);
        return this;
    }

    public void unbindLogger(AbstractLog log) {
        logs.remove(log);
    }
    // endregion

    // region local tag (subTag)
    private ThreadLocal<String> localTag = new ThreadLocal<>();

    // local tag
    public Log tag(@NonNull String tag) {
        localTag.set(tag);
        return this;
    }

    private String formatTag(String tag) {
        if (!TextUtils.isEmpty(tag) && !this.tag.equals(tag)) {
            return this.tag + "-" + tag;
        }
        return this.tag;
    }

    private String getFinalTag() {
        String tag = localTag.get();
        if (tag != null) localTag.remove();
        return formatTag(tag);
    }
    // endregion

    // region smart log
    public void log(Throwable throwable) {
        prepareLog(mergePriority(throwable), throwable, null);
    }

    public void log(String message, Object... args) {
        prepareLog(mergePriority(null), null, message, args);
    }

    public void log(Throwable throwable, String message, Object... args) {
        prepareLog(mergePriority(throwable), throwable, message, args);
    }

    /**
     * Merge priority for {@link Log#log(Throwable, String, Object...)}
     *
     * Serve as method for protect stack depth
     */
    private int mergePriority(Throwable throwable) {
        int p;
        if (throwable == null) {
            p = priority;
        } else if (priority <= android.util.Log.INFO) {
            p = android.util.Log.WARN;
        } else {
            p = android.util.Log.ERROR;
        }
        return p;
    }
    // endregion

    // region level log
    public void v(Throwable throwable) {
        prepareLog(android.util.Log.VERBOSE, throwable, null);
    }

    public void v(String message, Object... args) {
        prepareLog(android.util.Log.VERBOSE, null, message, args);
    }

    public void v(Throwable throwable, String message, Object... args) {
        prepareLog(android.util.Log.VERBOSE, throwable, message, args);
    }

    public void d(Throwable throwable) {
        prepareLog(android.util.Log.DEBUG, throwable, null);
    }

    public void d(String message, Object... args) {
        prepareLog(android.util.Log.DEBUG, null, message, args);
    }

    public void d(Throwable throwable, String message, Object... args) {
        prepareLog(android.util.Log.DEBUG, throwable, message, args);
    }

    public void i(Throwable throwable) {
        prepareLog(android.util.Log.INFO, throwable, null);
    }

    public void i(String message, Object... args) {
        prepareLog(android.util.Log.INFO, null, message, args);
    }

    public void i(Throwable throwable, String message, Object... args) {
        prepareLog(android.util.Log.INFO, throwable, message, args);
    }

    public void w(Throwable throwable) {
        prepareLog(android.util.Log.WARN, throwable, null);
    }

    public void w(String message, Object... args) {
        prepareLog(android.util.Log.WARN, null, message, args);
    }

    public void w(Throwable throwable, String message, Object... args) {
        prepareLog(android.util.Log.WARN, throwable, message, args);
    }

    public void e(Throwable throwable) {
        prepareLog(android.util.Log.ERROR, throwable, null);
    }

    public void e(String message, Object... args) {
        prepareLog(android.util.Log.ERROR, null, message, args);
    }

    public void e(Throwable throwable, String message, Object... args) {
        prepareLog(android.util.Log.ERROR, throwable, message, args);
    }

    public void wtf(Throwable throwable) {
        prepareLog(android.util.Log.ASSERT, throwable, null);
    }

    public void wtf(String message, Object... args) {
        prepareLog(android.util.Log.ASSERT, null, message, args);
    }

    public void wtf(Throwable throwable, String message, Object... args) {
        prepareLog(android.util.Log.ASSERT, throwable, message, args);
    }
    // endregion

    // region log core
    private void prepareLog(int priority, Throwable cause, String message, Object... args) {
        if (!isLoggable(priority)) return;

        //do it first for making sure resuming tag
        String finalTag = getFinalTag();

        String finalMsg = null;
        if (message == null) {
            if (cause == null) return; // swallow
        } else {
            finalMsg = String.format(message, args);
        }

        if (logs.size() == 0) {
            ensureAndroidLog();
            if (androidLog.isLoggable(debug, priority, tag)) {
                androidLog.log(priority, finalTag, tag, finalMsg, cause);
            }
            return;
        }

        for (AbstractLog log : logs) {
            if (log.isLoggable(debug, priority, tag)) {
                log.log(priority, finalTag, tag, finalMsg, cause);
            }
        }
    }

    private void ensureAndroidLog() {
        if (androidLog == null) androidLog = new AndroidLog();
    }
    // endregion

    // region thread and source jump features
    public Log logThread() {
        log("~~> Thread: " + Thread.currentThread().getName());
        return this;
    }

    public Log logStub() {
        logStubInfo(1);
        return this;
    }

    public Log logStub(int count) {
        logStubInfo(count);
        return this;
    }

    private String getSimpleClassName(String name) {
        int lastIndex = name.lastIndexOf(".");
        return name.substring(lastIndex + 1);
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    private void logStubInfo(int count) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();

        int stackOffset = MIN_STACK_OFFSET;
        if (count + stackOffset > trace.length) {
            count = trace.length - stackOffset - 1;
        }
        String level = "";
        for (int i = count; i > 0; i--) {
            int stackIndex = i + stackOffset;
            if (stackIndex >= trace.length) {
                continue;
            }
            StringBuilder builder = new StringBuilder();
            builder.append("--> ")
                .append(level)
                .append(getSimpleClassName(trace[stackIndex].getClassName()))
                .append(".")
                .append(trace[stackIndex].getMethodName())
                .append(" ")
                .append("(")
                .append(trace[stackIndex].getFileName())
                .append(":")
                .append(trace[stackIndex].getLineNumber())
                .append(")");
            level += "  ";
            log(builder.toString());
        }
    }
    // endregion

    // region timing logger
    public TimingLogger createTimingLogger(String label) {
        return new TimingLogger(priority, tag, label);
    }
    // endregion
}
