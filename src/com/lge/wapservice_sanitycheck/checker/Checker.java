package com.lge.wapservice_sanitycheck.checker;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;

import com.lge.wapservice_sanitycheck.Common_Util;
import com.lge.wapservice_sanitycheck.TestCase;
import com.lge.wapservice_sanitycheck.receiver.WapPush_Auto_SanityReceiver;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Intents;
import android.util.Log;

public class Checker implements Runnable {
    private static String TAG = "WAP_SanityCheck_Checker";
    private static final int FAIL_TEST = 1000;
    private static final int INSTALL_TEST = 1001;
    
    private ArrayList<Boolean> results;
    private Context context;
    private String type;
        
    public Checker(Context context, String type) {
        this.context = context;
        this.type = type;
        results = new ArrayList<Boolean>();
    }
    
    public boolean isPass() {
        if (results.size() == 0) {
            Common_Util.PrintLogText("unknown push message");
            return false;
        }
        return !results.contains(false);
    }
    
    private boolean SIChecker() {
        //-- SMS DB전체에서 그 고유 id를 base로 판별.
        // URI = Uri.parse("content://sms/inbox")
        if (!Common_Util.KEYLIST.containsKey("SI")) { //이미 한번 체크했으므로 패스
            return false;
        }
        
        Cursor c = context.getContentResolver().query(Sms.Inbox.CONTENT_URI, null, "subject Like '%"
                + Common_Util.KEYLIST.get("SI") + "%'", null, null);
        
        if (c.getCount() == 0) {
            return false;
        } else if (c.getCount() == 1) {
            c.moveToNext();
            
            String subject = c.getString(c.getColumnIndex("subject"));
            if (subject != null && subject.contains(Common_Util.KEYLIST.get("SI"))) { //si 메세지 확인
                
                Common_Util.PrintLogText(">> [SI][Start] Received SI push");
                Common_Util.PrintLogText("[SI] Key : pass");
                Log.i(TAG, "[SI][Start] Received SI push");
                Log.i(TAG, "[SI] Key : pass");

                if (c.getString(c.getColumnIndex("lgeSiid")).equals(Common_Util.KEYLIST.get("SI"))) { //siid 확인
                    Common_Util.PrintLogText("[SI] SI id : pass");
                    Log.i(TAG, "[SI] SI id : pass");
                } else {
                    results.add(false);
                    Common_Util.PrintLogText("[SI] SI id : fail");
                    Log.i(TAG, "[SI] SI id : fail");
                }
                
                if (c.getString(c.getColumnIndex("subject")).contains("SI Auto Test (#" + Common_Util.KEYLIST.get("SI") + ")")) { //siid 확인
                    Common_Util.PrintLogText("[SI] SI subject : pass");
                    Log.i(TAG, "[SI] SI subject : pass");
                } else {
                    results.add(false);
                    Common_Util.PrintLogText("[SI] SI subject : fail");
                    Log.i(TAG, "[SI] SI subject : fail");
                }
                
                if (c.getString(c.getColumnIndex("body")).equals("http://daum.net")) {// web address 확인 daum.net
                    Common_Util.PrintLogText("[SI] Web Link : pass");
                    Log.i(TAG, "[SI] Web Link : pass");
                } else {
                    results.add(false);
                    Common_Util.PrintLogText("[SI] Web Link : fail");
                    Log.i(TAG, "[SI] Web Link : fail");
                }
                c.close();
                Common_Util.KEYLIST.remove("SI");
                results.add(true);
            }
        } else {
            return false;
        }

        Common_Util.notiPass("SI", !results.contains(false), null);
        return true;
    }
    
    private void SLChecker() {
        if (!Common_Util.KEYLIST.containsKey("SL")) { //이미 한번 체크했으므로.
            return;
        }
        
        Cursor c = context.getContentResolver().query(Sms.Inbox.CONTENT_URI, null, "body Like '%"
                + Common_Util.KEYLIST.get("SL") + "%'", null, null);
        
        if (c.getCount() == 0) {
            return;
        } else if (c.getCount() == 1) {
            c.moveToNext();
            
            String body = c.getString(c.getColumnIndex("body"));
            if (body != null && body.contains(Common_Util.KEYLIST.get("SL"))) { //sl 메세지 확인
                Common_Util.PrintLogText(">> [SL][Start] Received SL push");
                Log.i(TAG, "[SL][Start] Received SL push");
                Common_Util.PrintLogText("[SL] Key : pass");
                Log.i(TAG, "[SL] Key : pass");

                if (c.getString(c.getColumnIndex("body")).contains("http://naver.com")) {// web address 확인 naver.com 
                    Common_Util.PrintLogText("[SL] Web Link : pass");
                    Log.i(TAG, "[SL] Web Link : pass");
                } else {
                    results.add(false);
                    Common_Util.PrintLogText("[SL] Web Link : fail");
                    Log.i(TAG, "[SL] Web Link : fail");
                }
                
                Common_Util.KEYLIST.remove("SL");
                c.close();
                results.add(true);
            }
        } else {
            return;
        }
        
        Common_Util.notiPass("SL", !results.contains(false), null);
    }
    
    private void SIEChecker() {
        boolean isChecking = false;
        long expiringTime = 0;
        
        if (!Common_Util.KEYLIST.containsKey("SIE")) { // 맵에 없다면 이미 한번 체크했으므로..
            return;
        }

        Cursor c = context.getContentResolver().query(Sms.Inbox.CONTENT_URI, null, "subject Like '%"
                + Common_Util.KEYLIST.get("SIE") + "%'", null, null);
        
        if (c.getCount() == 0) {
            return;
        } else if (c.getCount() == 1) {
            c.moveToNext();
            
            String subject = c.getString(c.getColumnIndex("subject"));
            if (subject != null && subject.contains(Common_Util.KEYLIST.get("SIE"))) {

                Common_Util.PrintLogText(">> [SIE][Start] Received SI(expiring) push");
                Common_Util.PrintLogText("[SIE] Key : pass");
                Log.i(TAG, "[SIE][Start] Received SI(expiring) push");
                Log.i(TAG, "[SIE] Key : pass");

                if (c.getString(c.getColumnIndex("lgeSiid")).equals(Common_Util.KEYLIST.get("SIE"))) { //siid 확인
                    Common_Util.PrintLogText("[SIE] SI(expiring) id : pass");
                    Log.i(TAG, "[SIE] SI(expiring) id : pass");
                } else {
                    results.add(false);
                    Common_Util.PrintLogText("[SIE] SI(expiring) id : fail");
                    Log.i(TAG, "[SIE] SI(expiring) id : fail");
                }
                
                if (c.getString(c.getColumnIndex("subject")).contains("SI Expire Auto Test (#" + Common_Util.KEYLIST.get("SIE") + ")")) { //siid 확인
                    Common_Util.PrintLogText("[SIE] SIE subject : pass");
                    Log.i(TAG, "[SIE] SIE subject : pass");
                } else {
                    results.add(false);
                    Common_Util.PrintLogText("[SIE] SIE subject : fail");
                    Log.i(TAG, "[SIE] SIE subject : fail");
                }
                
                if (c.getString(c.getColumnIndex("body")).equals("http://wap.lge.com")) {// web address 확인 http://wap.lge.com
                    Common_Util.PrintLogText("[SIE] Web Link : pass");
                    Log.i(TAG, "[SIE] Web Link : pass");
                } else {
                    results.add(false);
                    Common_Util.PrintLogText("[SIE] Web Link : fail");
                    Log.i(TAG, "[SIE] Web Link : fail");
                }
                
                expiringTime = c.getLong((c.getColumnIndex("lgeExpires")));
                
                c.close();
                isChecking = true;
            }
        } else {
            return;
        }
        
        Intent intent = new Intent(Intents.WAP_PUSH_RECEIVED_ACTION);
        intent.setType("lgeWapService/start.push.expiration.service");
        
        if (isChecking) {
            Common_Util.PrintLogText("[SIE] Wait 3 Min for expiring ...");
            Common_Util.PrintLogText("[SIE] (expiring time : " +
            Common_Util.defaltLocateFormat.format(new Date(expiringTime)) + ")");
            long curTime;
            int timer = 0;
            
            while (WapPush_Auto_SanityReceiver.checkRunFlag) {
                if (timer != 0 && timer%60 == 0) {
                    Common_Util.PrintWaitingLogText(timer + "sec\n");
                }
                
                try {
                    if (!WapPush_Auto_SanityReceiver.checkRunFlag) break; 
                    Thread.sleep(1000);
                    if (!WapPush_Auto_SanityReceiver.checkRunFlag) break; 
                } catch (InterruptedException e) {}
                
                curTime = new Date().getTime();
                if (curTime < expiringTime) {
                    Common_Util.PrintWaitingLogText(".");
                }
                
                context.sendBroadcast(intent, "android.permission.RECEIVE_WAP_PUSH");
                
                if (results.contains(false) || !Common_Util.KEYLIST.containsKey("SIE")) {
                    results.add(false);
                    
                    Common_Util.PrintLogText("[SIE] Waiting : fail");
                    break;
                }
                
                curTime = new Date().getTime();
                if (curTime < expiringTime) { 
                    Cursor newCursor = context.getContentResolver().query(Sms.Inbox.CONTENT_URI, null, "subject Like '%"
                            + Common_Util.KEYLIST.get("SIE") + "%'", null, null);
                    
                    // 새로 얻은 커서에서 삭제 되었는지 판별 : 삭제되었으면 fail
                    if (newCursor.getCount() == 0) {
                        results.add(false);
                        Common_Util.PrintWaitingLogText("\n");
                        Common_Util.PrintLogText("[SIE] Deleting too early : fail");
                    }
                    newCursor.close();
                    
                } else if (curTime >= expiringTime) {
                    context.sendBroadcast(intent, "android.permission.RECEIVE_WAP_PUSH");
                    Common_Util.PrintWaitingLogText("\n");
                    
                    try {
                        if (!WapPush_Auto_SanityReceiver.checkRunFlag) break; 
                        Thread.sleep(5000); // expire broadcast 동작 시간을 고려해 3초 대기
                        if (!WapPush_Auto_SanityReceiver.checkRunFlag) break; 
                    } catch (InterruptedException e) {}
                    
                    Cursor newCursor = context.getContentResolver().query(Sms.Inbox.CONTENT_URI, null,
                            "subject Like '%" + Common_Util.KEYLIST.get("SIE") + "%'", null, null);
                    
                    // 새로 얻은 커서에서 삭제 되었는지 판별 : 삭제되었으면 pass
                    if (newCursor.getCount() == 0) {
                        Common_Util.PrintLogText("[SIE] expiring pass");
                    } else if (newCursor.getCount() >= 1) {
                        results.add(false);
                        Common_Util.PrintLogText("[SIE] Exist SI(expiring) message : fail");
                    }
                    newCursor.close();
                    break;
                }
                timer++;
            }
            Common_Util.KEYLIST.remove("SIE");
            results.add(true);
        }

        Common_Util.notiPass("SIE", !results.contains(false), null);
    }
    
    private boolean OTAFChecker() {
        if (!Common_Util.KEYLIST.containsKey("OTAF")) { // 맵에 없다면 이미 한번 체크했으므로..
            return false;
        }

        String intentVals = null;
        Cursor c = context.getContentResolver().query(Sms.Inbox.CONTENT_URI, null, "subject Like '%"
                + Common_Util.KEYLIST.get("OTAF") + "%'", null, null);
        
        if (c.getCount() == 0) {
            return false;
        } else if (c.getCount() == 1) {
            c.moveToNext();
            
            String subject = c.getString(c.getColumnIndex("subject"));
            if (subject != null && subject.contains(Common_Util.KEYLIST.get("OTAF"))) {
                Common_Util.PrintLogText(">> [OTAF][Start] Received OTA push");
                Common_Util.PrintLogText("[OTAF] Key : pass");
                Log.i(TAG, "[OTAF][Start] Received OTA push");
                Log.i(TAG, "[OTAF] Key : pass");

                if (true) {
//                    results.add(false);
                    Common_Util.PrintLogText("[OTAF] partially pass");
                }
                
                intentVals = c.getString(c.getColumnIndex("lgeSec")) + ":/:" + c.getString(c.getColumnIndex("lgeMac")) + ":/:" + 
                        c.getString(c.getColumnIndex("lgeDoc")) + ":/:" + FAIL_TEST + ":/:" + 
                        c.getLong(c.getColumnIndex("_id")); 
                
                c.close();
                if(Common_Util.isUserpinExceptionModel()) {
                    results.add(false);
                    Common_Util.PrintLogText("[OTAF] This operator(or country) is not supported : fail");
                }
                
                Common_Util.KEYLIST.remove("OTAF");
                results.add(true);
            }
        } else {
            return false;
        }
        
        Common_Util.notiPass("OTAF", !results.contains(false), intentVals);
        return true;
    }
    
    private void APNChecker() {
        if (!Common_Util.KEYLIST.containsKey("APN")) { // 맵에 없다면 이미 한번 체크했으므로..
            return;
        }
        
        String intentVals = null;
        Cursor c = context.getContentResolver().query(Sms.Inbox.CONTENT_URI, null, "subject Like '%"
                + Common_Util.KEYLIST.get("APN") + "%'", null, null);
        
        if (c.getCount() == 0) {
            return;
        } else if (c.getCount() == 1) {
            c.moveToNext();
            
            String subject = c.getString(c.getColumnIndex("subject"));
            if (subject != null && subject.contains(Common_Util.KEYLIST.get("APN"))) {
                
                Common_Util.PrintLogText(">> [APN][Start] Received OTA push");
                Common_Util.PrintLogText("[APN] Key : pass");
                Log.i(TAG, "[APN][Start] Received OTA push");
                Log.i(TAG, "[APN] Key : pass");

                if (true) {
//                    results.add(false);
                    Common_Util.PrintLogText("[APN] partially pass");
                }
                
                intentVals = c.getString(c.getColumnIndex("lgeSec")) + ":/:" + c.getString(c.getColumnIndex("lgeMac")) + ":/:" + 
                        c.getString(c.getColumnIndex("lgeDoc")) + ":/:" + FAIL_TEST + ":/:" + 
                        c.getLong(c.getColumnIndex("_id")); 

                c.close();
                if(Common_Util.isUserpinExceptionModel()) {
                    results.add(false);
                    Common_Util.PrintLogText("[APN] This operator(or country) is not supported : fail");
                }
                
                Common_Util.KEYLIST.remove("APN");
                results.add(true);
            }
        } else {
            return;
        }
        
        Common_Util.notiPass("APN", !results.contains(false), intentVals);
    }

    @Override
    public void run() {
        Common_Util.PrintLogText("Assort push message..");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Common_Util.PrintLogText("push type : " + type);
//        //db 전체 확인 코드
//        Cursor c = context.getContentResolver().query(Sms.Inbox.CONTENT_URI, null, null, null, null);
//        Cursor c = context.getContentResolver().query(Sms.Inbox.CONTENT_URI, null, "subject Like '%"
//                + Common_Util.KEYLIST.get("SI") + "%'", null, null);
//        String colum = "\t";
//        String columName = "\t";
//        while  (c.moveToNext()) {
//            colum = "\t";
//            columName = "\t";
//            for(int i=0; i<61; i++) {
//                columName += c.getColumnName(i) + "\t";
//                colum += c.getString(i) + "\t";
//            }
//            Log.i("geunchae",columName);
//            Log.i("geunchae",colum);
//        }
        
        
        
        if (type.equals(TestCase.TYPE_SI)){
            if (SIChecker()) return;
            SIEChecker();
        } else if (type.equals(TestCase.TYPE_SL)){
            SLChecker();
        } else if (type.equals(TestCase.TYPE_OTA)){
            if (OTAFChecker()) return;
            APNChecker();
        }
    }
}
