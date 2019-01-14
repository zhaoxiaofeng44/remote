package com.remote;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 23) {
            Intent intent = new Intent(MainActivity.this, com.remote.ShellService.class);
            startService(intent);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            },100);
        } else {
            Intent intent = new Intent(MainActivity.this, ShellService.class);
            startService(intent);
            finish();
        }
    }
}
