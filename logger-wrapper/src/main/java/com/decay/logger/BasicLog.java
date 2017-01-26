/*
 * Copyright 2017 LsPush
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

public class BasicLog extends Log {
    private final String tag; // basic tag
    private int priority; // default message level
    private int filter; // release filter
    private boolean debug;
    private LinkedList<AbstractLog> logs = new LinkedList<>();
    private AndroidLog androidLog = null;

    // region create and config
    public BasicLog(@NonNull String tag) {
        this.tag = tag;
        this.priority = android.util.Log.DEBUG;
        this.filter = android.util.Log.WARN;
        this.debug = BuildConfig.DEBUG;
    }

    public String getBasicTag() {
        return tag;
    }

    public BasicLog debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public boolean isDebug() {
        return debug;
    }

    public int getPriority() {
        return priority;
    }

    public BasicLog priority(int priority) {
        this.priority = priority;
        return this;
    }

    public int getFilter() {
        return filter;
    }

    public BasicLog filter(int filter) {
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
    public BasicLog bindLogger(AbstractLog log) {
        log.prepareLogger(tag);
        logs.add(log);
        return this;
    }

    public void unbindLogger(AbstractLog log) {
        logs.remove(log);
    }
    // endregion

    @Override
    public TimingLogger createTimingLogger(String label) {
        return new TimingLogger(priority, tag, label);
    }

    public SubLog subTag(String tag) {
        return new SubLog(this, tag);
    }

    // region log core
    protected void prepareLog(int priority, Throwable cause, String message, Object... args) {
        //do it first for making sure resuming tag
        prepareLog(priority, getFinalTag(), cause, message, args);
    }

    void prepareLog(int priority, String finalTag, Throwable cause, String message, Object... args) {
        if (priority == SMART_PRIORITY) priority = mergePriority(cause);
        if (!isLoggable(priority)) return;

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

    /**
     * Merge priority for {@link BasicLog#log(Throwable, String, Object...)}
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

    protected String getFinalTag() {
        return formatTag(getLocalTag());
    }

    protected String formatTag(String localTag) {
        String finalTag = tag;
        if (!TextUtils.isEmpty(localTag)) finalTag += "-" + localTag;
        return finalTag;
    }

    private void ensureAndroidLog() {
        if (androidLog == null) androidLog = new AndroidLog();
    }
    // endregion
}
