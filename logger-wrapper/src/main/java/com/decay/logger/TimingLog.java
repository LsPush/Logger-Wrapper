/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.decay.logger;

import android.os.SystemClock;

import java.util.ArrayList;

/**
 * A utility class to help log timings splits throughout a method call.
 * Typical usage is:
 *
 * <pre>
 *     TimingLog timings = new TimingLog(TAG, "methodA");
 *     // ... do some work A ...
 *     timings.addSplit("work A");
 *     // ... do some work B ...
 *     timings.addSplit("work B");
 *     // ... do some work C ...
 *     timings.addSplit("work C");
 *     timings.dumpToLog();
 * </pre>
 *
 * <p>The dumpToLog call would add the following to the log:</p>
 *
 * <pre>
 *     D/TAG     ( 3459): methodA: begin
 *     D/TAG     ( 3459): methodA:      9 ms, work A
 *     D/TAG     ( 3459): methodA:      1 ms, work B
 *     D/TAG     ( 3459): methodA:      6 ms, work C
 *     D/TAG     ( 3459): methodA: end, 16 ms
 * </pre>
 */
public class TimingLog {
    private String tag;
    private int priority;
    private boolean disabled;

    private String taskLabel;
    private ArrayList<Long> splits;
    private ArrayList<String> splitLabels;

    TimingLog(int priority, String tag, String label) {
        this.priority = priority;
        this.tag = tag;
        reset(label);
    }

    public void reset(String label) {
        taskLabel = label;
        reset();
    }

    public void reset() {
        disabled = !isLoggable();
        if (disabled) return;

        if (splits == null) {
            splits = new ArrayList<>();
            splitLabels = new ArrayList<>();
        } else {
            splits.clear();
            splitLabels.clear();
        }
        addSplit(null);
    }

    public void addSplit(String splitLabel) {
        if (disabled) return;

        long now = SystemClock.elapsedRealtime();
        splits.add(now);
        splitLabels.add(splitLabel);
    }

    public void dumpToLog() {
        if (disabled) return;

        log(taskLabel + ": begin");
        final long first = splits.get(0);
        long now = first;
        for (int i = 1; i < splits.size(); i++) {
            now = splits.get(i);
            final String splitLabel = splitLabels.get(i);
            final long prev = splits.get(i - 1);

            log(taskLabel + ":      " + (now - prev) + " ms, " + splitLabel);
        }
        log(taskLabel + ": end, " + (now - first) + " ms");
    }

    public void log(String message) {
        android.util.Log.println(priority, tag, message);
    }

    public boolean isLoggable() {
        return Logger.tag(tag).isLoggable();
    }
}
