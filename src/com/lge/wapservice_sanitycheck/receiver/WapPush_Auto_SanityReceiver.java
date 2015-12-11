package com.lge.wapservice_sanitycheck.receiver;

import java.util.ArrayList;

import com.lge.wapservice_sanitycheck.Common_Util;
import com.lge.wapservice_sanitycheck.TestCase;
import com.lge.wapservice_sanitycheck.checker.Checker;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.util.Log;
import android.widget.Toast;

public class WapPush_Auto_SanityReceiver extends BroadcastReceiver{
    public static String TAG = "WapPush_Auto_SanityReceiver";
    
    public static boolean checkRunFlag;
    private ArrayList<Checker> checkRunBuffer = new ArrayList<Checker>();
    private ArrayList<Boolean> resultBuffer = new ArrayList<Boolean>();
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Receiver's onReceive called : " + intent.getAction() + " : " + intent.getType());
        
        /*// Test Log ///
        String serviceCenterAddress = intent.getStringExtra("smscAdd");
        String originatingAddress = intent.getStringExtra("originAdd");
        int permission = intent.getIntExtra("permissionToShow", -1);
        
        Log.i(TAG, "serviceCenterAddress : " + serviceCenterAddress);
        Log.i(TAG, "originatingAddress : " + originatingAddress);
        Log.i(TAG, "permission : " + permission);*/

        //Toast.makeText(context, "Wap Push Receiver 동작", Toast.LENGTH_SHORT).show();
        
        if (Common_Util.isAuto) {
            checkRunBuffer.add(new Checker(context, intent.getType()));
        }
    }

    // performing instead activity
    public void registerReceiver(Context context) {
        checkRunFlag = true;

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.WAP_PUSH_RECEIVED");
        filter.addAction("com.lge.wapservice.SANITYCHECK");

        try {
            filter.addDataType(TestCase.TYPE_SI);
            filter.addDataType(TestCase.TYPE_SL);
            filter.addDataType(TestCase.TYPE_OTA);

//            filter.addDataType("lgeWapService/start.push.expiration.service");
//            filter.addDataType("lgeWapService/stop.push.expiration.service");
//            filter.addDataType("lgeWapService/clear.push.notification");
//            filter.addDataType("lgeWapService/clear.prov.notification");
            Log.i(TAG, "Receiver set Filter");
        } catch (MalformedMimeTypeException e) {
            // TODO Auto-generated catch block
            Log.i(TAG, "filter addDataType error : " + e);
            e.printStackTrace();
        }
        context.registerReceiver(this, filter);
        
        if (Common_Util.isAuto) {
            new Thread() {
                private int max = TCMaxCount();
                        
                private int TCMaxCount() {
                    int defaultMax = Common_Util.DEFAULT_TC_MAX_COUNT;
                    defaultMax = Common_Util.isSLExceptionModel() ? defaultMax-1 : defaultMax;
                    defaultMax = Common_Util.isUserpinExceptionModel() ? defaultMax-2 : defaultMax;
                    return defaultMax;
                }
                
                public void run() {
                    while(checkRunFlag) {
                        if (checkRunBuffer.size() > 0) {
                            Common_Util.PrintLogText(null);
                            Common_Util.PrintLogText(">> Start analysing push message");
                            Thread work = new Thread(checkRunBuffer.get(0));
                            work.start();
                            
                            try {
                                work.join();
                            } catch (InterruptedException e) {}
                            Common_Util.PrintLogText(">> Finish.");
                            resultBuffer.add(checkRunBuffer.get(0).isPass());
                            checkRunBuffer.remove(checkRunBuffer.get(0));
                        }

                        try {
                            if (!checkRunFlag) return;
                            sleep(500);
                            if (!checkRunFlag) return;
                        } catch (InterruptedException e) {}

                        if (resultBuffer.size() >= max) {
                            Common_Util.PrintLogText(null);
                            Common_Util.PrintLogText("----- Finish All Test -----");
                            
                            String message = "";
                            int count=0;
                            for (boolean b : resultBuffer) if (b==false) count++;
                            message = (resultBuffer.size() - count) + " T/C Pass, " + count + " T/C Fail";
                            message += "\n - OTA install 및 신규 T/C는 수동 테스트 필요합니다.\n"
                                    + " - 결과란이 비어있으면 fail 상황 입니다.";
                            Common_Util.ShowAlertDialog("Result", message);
                            Log.i(TAG, message);
                            
                            checkRunFlag = false;
                            break;
                        }
                        //Log.i(TAG, "Receiver CheckBuffer run~");
                    }
                }
            }.start();
        }
        
        Log.i(TAG, "Resist Receiver");
    }
    
    public void unregisterReceiver(Context context) {
        checkRunFlag = false;
        context.unregisterReceiver(this);
        Log.i(TAG, "Unresist Receiver");
    }
}