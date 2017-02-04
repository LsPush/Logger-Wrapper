package com.decay.logger.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.decay.logger.BasicLog;
import com.decay.logger.Logger;
import com.decay.logger.TimingLog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logger.tag("args").debug(BuildConfig.DEBUG).log("args&");

        BasicLog androidLog = Logger.tag("LogWithDefaultAndroidLog");

        androidLog.log("normal log");
        androidLog.debug(BuildConfig.DEBUG).filter(android.util.Log.WARN).priority(android.util.Log.INFO);

        androidLog.log("hello %s", "AndroidLog");
        androidLog.w("good");
        androidLog.e(new Throwable("Hello Exception"));

        BasicLog loggerLog = Logger.tag("LogWithCustomLogger");
        loggerLog.bindLogger(new DebugLog());
        loggerLog.log(new Throwable("Logger"), "hello %s", "cause");
        loggerLog.log("info log");
        loggerLog.v("hello v");
        loggerLog.d("tag", null, "debug %s", "test");

        doLog();

        Logger.tag("abc").tag("subTag").logStub().logThread();

        new MyJob().start();
    }

    public void doLog() {
        Logger.tag("nest").bindLogger(new DebugLog()).log("nest message");
    }

    private class MyJob extends Thread {
        public static final String TAG_MYJOB = "TAG_MYJOB";

        public void run() {
            BasicLog basicLog = Logger.tag(TAG_MYJOB);
            TimingLog timings = Logger.tag(TAG_MYJOB).createTimingLogger("MyJob");

            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timings.addSplit("Phase 1 ready");

            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timings.addSplit("Phase 2 ready");

            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timings.addSplit("Phase 3 ready");

            timings.dumpToLog();
        }
    }
}
