package com.lge.wapservice_sanitycheck;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony.Sms.Intents;
import android.util.Log;

public class TestCase extends Thread {
    public final static String TAG = "WAP_SanityCheck_TestThread";
    public String TC_ID = null;
    
    public final static String TYPE_SI = "application/vnd.wap.sic";
    public final static String TYPE_SL = "application/vnd.wap.slc";
    public final static String TYPE_OTA = "application/vnd.wap.connectivity-wbxml";
    
    private static boolean DIALOG_STATE = false;
    
    private Intent intent = new Intent(Intents.WAP_PUSH_RECEIVED_ACTION);
    
    private String TC_Name;
    private Context context;
    private ServerConnector conn;
    private ServerConnector conn2;
    private boolean isOffline;
    private String result;
    private String phoneNum;

    public void makeKey() {
        TC_ID = "";
        for(int i=0; i<6; i++) {
            char c = '0';
            if ((int)(Math.random()*2) == 0) 
                c = (char)((Math.random()*10) + 48);
            else 
                c = (char)((Math.random() * 24) + 65);
            TC_ID += c;
        }
    }
    
    public TestCase(Context context, String phoneNum, boolean isOffline, boolean isAuto){
        this.context = context;
        this.phoneNum = phoneNum;
        this.isOffline = isOffline;
        
        if (isAuto) {
            makeKey();
        }
        Common_Util.PrintLogText("Init T/C data");
        Log.i(TAG, (isOffline ? "Offline"  : "Online") + " test Start");
    }

    @Override
    public void run() {
        Common_Util.PrintLogText("[" + TC_Name + "] Ready to send - " + TC_Name + " request");
        while (DIALOG_STATE) {
            try {
                Log.i(TAG, "Waiting.. Dialog is opend..");
                sleep(500);
            } catch (InterruptedException e) {}
        }
        
        //Log.i(TAG, TC_Name + " KEY : " + TC_ID);
        try {
            //dialog open
            if(!Common_Util.isAuto) {
                Common_Util.mHandler.sendMessage(Common_Util.MakeNewMessage(Common_Util.PROGRESS_DIALOG_OPEN, null));
                DIALOG_STATE = true;
            }
            
            if(isOffline) {
                context.sendBroadcast(intent);
            } else {
                result = conn.send();
                Common_Util.PrintLogText("[" + TC_Name + "] HTTP POST - 125.141.31.137:8800");
                if (!result.contains("Message Submitted")) {
                    throw new Exception("[" + TC_Name + "] 데이터 전송 실패");
                }
                Common_Util.PrintLogText("[" + TC_Name + "] Check request String");
                
                if (conn2 != null) {
                    result = conn2.send();
                    Common_Util.PrintLogText("[" + TC_Name + "] Connection2 HTTP POST - 125.141.31.137:8800");
                    if (!result.contains("Message Submitted")) {
                        throw new Exception("[" + TC_Name + "] Connection2 데이터 전송 실패");
                    }
                    Common_Util.PrintLogText("[" + TC_Name + "] Connection2 Check request String");
                }
            }

        } catch (final Exception e) {
            e.printStackTrace();
            if (Common_Util.isAuto)
                Common_Util.PrintLogText("[Err][" +  TC_Name + "] Network is unavailable");
            else {
                //toast
                Bundle data = new Bundle();
                data.putString("TOAST", "[" + TC_Name + "] 네트워크 연결 Error :\n(" + e + ")");
                Common_Util.mHandler.sendMessage(Common_Util.MakeNewMessage(Common_Util.SHOW_ERROR_TOAST, data));
            }
        }
        
        try {
            sleep(400);
        } catch (InterruptedException e) {}

        Common_Util.PrintLogText("[" + TC_Name + "] " + (isOffline ? "Self Broadcast" : "HTTP POST") + " : Success");
        //dialog close
        if(!Common_Util.isAuto) {
            Common_Util.mHandler.sendMessage(Common_Util.MakeNewMessage(Common_Util.PROGRESS_DIALOG_CLOSE, null));
            DIALOG_STATE = false;
        }
    }

    private static byte[] hexToByteArray(String hex) {
        if (hex == null || hex.length() == 0) {
            return null;
        }

        byte[] ba = new byte[hex.length() / 2];
        for (int i = 0; i < ba.length; i++) {
            ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return ba;
    }

    public Thread Single_SITest() {
        TC_Name = "SI";
        if (!isOffline) {
            conn = new ServerConnector("http://125.141.31.137:8800/Send%20XML%20Settings.htm");
            conn.add("PhoneNumber", phoneNum).add("OTAXMLSETTINGS", Common_Util.MakeSITestXML(TC_ID));
        } else {
            byte[] hexToByte = hexToByteArray(Common_Util.getSI_DOC_String());
            
            TC_ID = Common_Util.OFFLINE_SI_KEY;
            intent.setType(TYPE_SI); // si type
            intent.putExtra("data", hexToByte);
            intent.putExtra("smscAdd", "+821029991115");
            intent.putExtra("originAdd", "+821029991115");
            intent.putExtra("permissionToShow", -1);
        }
        this.start();
        Common_Util.KEYLIST.put("SI", TC_ID);
        return this;
    }
    
    public Thread Single_SLTest() {
        TC_Name = "SL";
        if (!isOffline) {
            conn = new ServerConnector("http://125.141.31.137:8800/Send%20WAP%20Push%20SL%20Message.htm");
            String WAPSL = "Yes";
            String WAPURL = "http://naver.com" + (TC_ID == null ? "" : " (#" + TC_ID + ")");
            String WAPSLACTION = "execute-high";
            conn.add("PhoneNumber", phoneNum).add("WAPSL", WAPSL).add("WAPURL", WAPURL).add("WAPSLACTION", WAPSLACTION);
        } else {
            byte[] hexToByte = hexToByteArray(Common_Util.getSL_DOC_String());

            TC_ID = Common_Util.OFFLINE_SL_KEY;
            intent.setType(TYPE_SL); // sl type
            intent.putExtra("data", hexToByte);
            intent.putExtra("smscAdd", "+821029991115");
            intent.putExtra("originAdd", "+821029991115");
            intent.putExtra("permissionToShow", -1);
        }
        this.start();
        Common_Util.KEYLIST.put("SL", TC_ID);
        return this;
    }
    
    public Thread Single_SIExpireTest() {
        TC_Name = "SIE";
        if (!isOffline) {
            conn = new ServerConnector("http://125.141.31.137:8800/Send%20XML%20Settings.htm");
            conn.add("PhoneNumber", phoneNum).add("OTAXMLSETTINGS", Common_Util.MakeSIExpireTestXML(TC_ID));
        } else {
            byte[] hexToByte = hexToByteArray(Common_Util.getSIE_DOC_String());
            //expire 되는 시간이 이미 지난 시간이므로 log기반으로 수신 여부 파악해야함..? 디코딩 방법을 모르면 사실상 불가능.

            TC_ID = Common_Util.OFFLINE_SIE_KEY;
            intent.setType(TYPE_SI); // si type
            intent.putExtra("data", hexToByte);
            intent.putExtra("smscAdd", "+821029991115");
            intent.putExtra("originAdd", "+821029991115");
            intent.putExtra("permissionToShow", -1);
        }
        this.start();
        Common_Util.KEYLIST.put("SIE", TC_ID);
        return this;
    }
    
    public Thread Single_SIDeleteTest(boolean delete) {
        TC_Name = "SID_Post";
        if (!isOffline) {
            conn = new ServerConnector("http://125.141.31.137:8800/Send%20XML%20Settings.htm");
            conn.add("PhoneNumber", phoneNum).add("OTAXMLSETTINGS", Common_Util.MakeSIDeleteTestXML(delete, TC_ID));
        } else {
            byte[] hexToByte = hexToByteArray(Common_Util.getSID_DOC_String(delete));

            TC_ID = Common_Util.OFFLINE_SIE_KEY;
            intent.setType(TYPE_SI); // si type
            intent.putExtra("data", hexToByte);
            intent.putExtra("smscAdd", "+821029991115");
            intent.putExtra("originAdd", "+821029991115");
            intent.putExtra("permissionToShow", -1);
        }
        this.start();
        Common_Util.KEYLIST.put("SID", TC_ID);
        return this;
    }

    
    public Thread Single_OTAPinTest(String otaPin) {
        TC_Name = "OTAF";
        if (!isOffline) {
            if (otaPin.length() < 4) {
                Bundle data = new Bundle();
                data.putString("TOAST", "OTA Pin을 4자리 이상 입력하세요.");
                Common_Util.mHandler.sendMessage(Common_Util.MakeNewMessage(Common_Util.SHOW_ERROR_TOAST, data));
                return null;
            }

            String otaPinType = "USERPIN";
            conn = new ServerConnector("http://125.141.31.137:8800/Send%20XML%20Settings.htm");
            conn.add("PhoneNumber", phoneNum).add("OTAXMLSETTINGS", Common_Util.MakeOTAPinTestXML(TC_ID)).add("OTAPIN", otaPin).add("OTAPINTYPE", otaPinType);
        } else {
            byte[] hexToByte = hexToByteArray(Common_Util.getOTAF_DOC_String());
            
            TC_ID = Common_Util.OFFLINE_OTAF_KEY;
            
            intent.setType(TYPE_OTA);
            intent.putExtra("data", hexToByte);
            intent.putExtra("permissionToShow", -1);
            
            HashMap<String, String> contentTypeParameters = new HashMap<String, String>();
            contentTypeParameters.put("SEC", "1");
            contentTypeParameters.put("MAC", Common_Util.getOTAF_MAC_String());
            
            intent.putExtra("contentTypeParameters", contentTypeParameters);
            intent.putExtra("subscription", "0"); //ota_simid
        }
        this.start();
        Common_Util.KEYLIST.put("OTAF", TC_ID);
        return this;
    }
 
    public Thread Single_APNTest(String apnPin) {
        TC_Name = "APN";
        if (!isOffline) {
            if (apnPin.length() < 4) {
                Bundle data = new Bundle();
                data.putString("TOAST", "OTA Pin을 4자리 이상 입력하세요.");
                Common_Util.mHandler.sendMessage(Common_Util.MakeNewMessage(Common_Util.SHOW_ERROR_TOAST, data));
                return null;
            }

            String apnPinType = "USERPIN";
            conn = new ServerConnector("http://125.141.31.137:8800/Send%20XML%20Settings.htm");
            conn.add("PhoneNumber", phoneNum).add("OTAXMLSETTINGS", Common_Util.MakeAPNTestXML(TC_ID)).add("OTAPIN", apnPin).add("OTAPINTYPE", apnPinType);
        } else {
            byte[] hexToByte = hexToByteArray(Common_Util.getAPN_DOC_String());
            
            TC_ID = Common_Util.OFFLINE_APN_KEY;
            
            intent.setType(TYPE_OTA);
            intent.putExtra("data", hexToByte);
            intent.putExtra("permissionToShow", -1);
            
            HashMap<String, String> contentTypeParameters = new HashMap<String, String>();
            contentTypeParameters.put("SEC", "1");
            contentTypeParameters.put("MAC", Common_Util.getAPN_MAC_String());
            
            intent.putExtra("contentTypeParameters", contentTypeParameters);
            intent.putExtra("subscription", "0"); //ota_simid
        }
        this.start();
        Common_Util.KEYLIST.put("APN", TC_ID);
        return this;
    }
    
    public Thread Single_NetpinAPNTest() {
        TC_Name = "NETWPIN";

        if (!isOffline) {
            String networkPin = Common_Util.SimOperator + phoneNum.substring(3);
            Log.i("geunchae",networkPin);
            Bundle data = new Bundle();

            if (!Common_Util.isAuto) {
                data.putString("TOAST", "Sending 2ea T/C networkPin test push (correct/incorrect)");
                Common_Util.mHandler.sendMessage(Common_Util.MakeNewMessage(Common_Util.SHOW_ERROR_TOAST, data));
            }

            String apnPinType = "NETWPIN";

            conn = new ServerConnector("http://125.141.31.137:8800/Send%20XML%20Settings.htm");
            conn.add("PhoneNumber", phoneNum).add("OTAXMLSETTINGS", Common_Util.MakeNetPinAPNTestXML(false, TC_ID)).add("OTAPIN", new StringBuffer(networkPin).reverse().toString()).add("OTAPINTYPE", apnPinType);

            conn2 = new ServerConnector("http://125.141.31.137:8800/Send%20XML%20Settings.htm");
            conn2.add("PhoneNumber", phoneNum).add("OTAXMLSETTINGS", Common_Util.MakeNetPinAPNTestXML(true, TC_ID)).add("OTAPIN", networkPin).add("OTAPINTYPE", apnPinType);
        } else {
            byte[] hexToByte = hexToByteArray(""/*Common_Util.getAPN_DOC_String()*/); //미구현
            
            TC_ID = Common_Util.OFFLINE_APN_KEY;
            
            intent.setType(TYPE_OTA);
            intent.putExtra("data", hexToByte);
            intent.putExtra("permissionToShow", -1);
            
            HashMap<String, String> contentTypeParameters = new HashMap<String, String>();
            contentTypeParameters.put("SEC", "1");
            contentTypeParameters.put("MAC", Common_Util.getAPN_MAC_String());
            
            intent.putExtra("contentTypeParameters", contentTypeParameters);
            intent.putExtra("subscription", "0"); //ota_simid
        }
        this.start();
        Common_Util.KEYLIST.put("NETWPIN", TC_ID);
        return this;
    }
}

