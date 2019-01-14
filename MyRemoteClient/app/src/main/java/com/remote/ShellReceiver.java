package com.remote;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class ShellReceiver extends BroadcastReceiver {

    private Handler myHandler = new Handler();

    @Override
    public void onReceive(final Context context, Intent intent) {

        myHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context.getApplicationContext(),"onReceive >>  ",Toast.LENGTH_SHORT).show();
            }
        });

        Context appContext = context.getApplicationContext();
        if (!isRun(appContext, "com.example.remote")) {
            intent.setClass(appContext, MainActivity.class);
            intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            appContext.startActivity(intent);
        }
    }

    /**
     * 判断应用是否在运行
     *
     * @param context
     * @return
     */
    public boolean isRun(Context context, String packagename) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        boolean isAppRunning = false;
        //100表示取的最大的任务数，info.topActivity表示当前正在运行的Activity，info.baseActivity表系统后台有此进程在运行
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(packagename) || info.baseActivity.getPackageName().equals(packagename)) {
                isAppRunning = true;
                Log.d("ActivityService isRun()", info.topActivity.getPackageName() + " info.baseActivity.getPackageName()=" + info.baseActivity.getPackageName());
                break;
            }
        }
        Log.d("ActivityService isRun()", "com.ad 程序  ...isAppRunning......" + isAppRunning);
        return isAppRunning;
    }
}
