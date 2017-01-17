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
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import java.security.InvalidParameterException;

public class Logger {
    private static ArrayMap<String, Log> logs = new ArrayMap<>(1);

    public static Log tag(@NonNull String tag) {
        if (isEmpty(tag)) {
            throw new InvalidParameterException("Tag should not be null or empty");
        }

        Log log = logs.get(tag);
        if (log == null) {
            log = new Log(tag);
            logs.put(tag, log);
        }
        return log;
    }

    public static boolean isExist(String tag) {
        return !isEmpty(tag) && logs.get(tag) != null;
    }

    private static boolean isEmpty(String tag) {
        return TextUtils.isEmpty(tag) || tag.trim().length() == 0;
    }
}
