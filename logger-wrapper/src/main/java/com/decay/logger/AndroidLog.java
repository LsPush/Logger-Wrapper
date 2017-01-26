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
import android.support.annotation.Nullable;

public class AndroidLog extends AbstractLog {
    private static final int MAX_LOG_LENGTH = 4000;

    @Override
    public void log(int priority, String finalTag, String tag, @Nullable String message, @Nullable Throwable cause) {
        if (message != null) {
            log(priority, finalTag, message);
        }
        if (cause != null) {
            android.util.Log.println(priority, finalTag, android.util.Log.getStackTraceString(cause));
        }
    }

    public void log(int priority, String tag, @NonNull String message) {
        if (message.length() < MAX_LOG_LENGTH) {
            android.util.Log.println(priority, tag, message);
            return;
        }

        // Split by line, then ensure each line can fit into Log's maximum length.
        for (int i = 0, length = message.length(); i < length; i++) {
            int newline = message.indexOf('\n', i);
            newline = newline != -1 ? newline : length;
            do {
                int end = Math.min(newline, i + MAX_LOG_LENGTH);
                String part = message.substring(i, end);
                android.util.Log.println(priority, tag, part);
                i = end;
            } while (i < newline);
        }
    }
}
