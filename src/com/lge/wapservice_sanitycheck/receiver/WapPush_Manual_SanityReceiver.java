package com.lge.wapservice_sanitycheck.receiver;

import com.lge.wapservice_sanitycheck.Common_Util;
import com.lge.wapservice_sanitycheck.TestCase;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.util.Log;
import android.widget.Toast;

public class WapPush_Manual_SanityReceiver extends BroadcastReceiver{
    public static String TAG = "WapPush_Manual_SanityReceiver";
    private int pushCount = 1;
    
//    public static boolean checkRunFlag;
//    private ArrayList<Checker> checkRunBuffer = new ArrayList<Checker>();
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Receiver's onReceive called : " + intent.getAction() + " : " + intent.getType());
        
        // Test Log ///
        String serviceCenterAddress = intent.getStringExtra("smscAdd");
        String originatingAddress = intent.getStringExtra("originAdd");
        int permission = intent.getIntExtra("permissionToShow", -1);
        
        Log.i(TAG, "serviceCenterAddress : " + serviceCenterAddress);
        Log.i(TAG, "originatingAddress : " + originatingAddress);
        Log.i(TAG, "permission : " + permission);

        //Toast.makeText(context, "Wap Push Receiver µø¿€", Toast.LENGTH_SHORT).show();
        
        if (!Common_Util.isAuto) {
            Common_Util.PrintLogText(null);
            Common_Util.PrintLogText("[Push " + pushCount + "] ------- Received push intent -------");
            Common_Util.PrintLogText("[Push " + pushCount + "] type : " + intent.getType());
            Common_Util.PrintLogText(null);
            pushCount++;
        }
    }

    // performing instead activity
    public void registerReceiver(Context context) {

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
        
        Log.i(TAG, "Resist Manual Receiver");
    }
    
    public void unregisterReceiver(Context context) {
        context.unregisterReceiver(this);
        Log.i(TAG, "Unresist Manual Receiver");
    }
}