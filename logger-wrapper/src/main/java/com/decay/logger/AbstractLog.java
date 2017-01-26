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

import android.support.annotation.Nullable;

public abstract class AbstractLog {

    protected void prepareLogger(String tag) {}

    protected boolean isLoggable(boolean debug, int priority, String tag) {
        return true;
    }

    protected abstract void log(int priority, String finalTag, String tag, @Nullable String message,
        @Nullable Throwable cause);
}
