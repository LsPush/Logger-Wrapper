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

import android.text.TextUtils;

public class SubLog extends Log {
    private BasicLog basicLog;
    private String tag;
    private String subTag;

    public SubLog(BasicLog basicLog, String subTag) {
        this.basicLog = basicLog;
        this.tag = basicLog.getBasicTag();
        this.subTag = subTag;
    }

    @Override
    public TimingLog createTimingLogger(String label) {
        ensureLog();
        return basicLog.createTimingLogger(label);
    }

    @Override
    protected void prepareLog(int priority, Throwable cause, String message, Object... args) {
        basicLog.prepareLog(priority, getFinalTag(), cause, message, args);
    }

    protected String getFinalTag() {
        return formatTag(getLocalTag());
    }

    protected String formatTag(String localTag) {
        String finalTag = tag + "-" + subTag;
        if (!TextUtils.isEmpty(localTag)) finalTag += "-" + localTag;
        return finalTag;
    }

    private void ensureLog() {
        if (basicLog == null) basicLog = Logger.tag(tag);
    }
}
