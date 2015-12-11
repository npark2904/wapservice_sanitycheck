package com.lge.wapservice_sanitycheck;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import com.lge.wapservice_sanitycheck.SanityCheckListActivity.UIMassgeHandler;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Common_Util {
    public final static int PROGRESS_DIALOG_OPEN = 10; 
    public final static int PROGRESS_DIALOG_CLOSE = 11;
    public final static int SHOW_ERROR_TOAST = 20;
    public final static int SHOW_ALERT_DIALOG = 25;
    public final static int PRINT_LOG = 30;
    public final static int NOTI_PASS = 40;
    public final static int DEFAULT_TC_MAX_COUNT = 5;
    
    public final static String OFFLINE_SI_KEY = "44H1WF";
    public final static String OFFLINE_SL_KEY = "XII230";
    public final static String OFFLINE_SIE_KEY = "UL9736";
    public final static String OFFLINE_OTAF_KEY = "063J27";
    public final static String OFFLINE_APN_KEY = "7Q2022";
    public final static String OPERATOR;
    public final static String COUNTRY;
    
    private final static String TAG = "WAP_SanityCheck_Common_Util";
    
    public final static SimpleDateFormat idFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    public final static DateFormat rondonFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    public final static DateFormat defaltLocateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public final static DateFormat LogTimeFormat = new SimpleDateFormat("HH:mm:ss");
    
    public static ArrayList<String> SL_EXCEPT_MODELS = new ArrayList<String>();
    public static ArrayList<String> USERPIN_EXCEPT_MODELS = new ArrayList<String>();
    public static HashMap<String, String> KEYLIST = new HashMap<String, String>(); 
    public static UIMassgeHandler mHandler;
    public static boolean isAuto = true;
    public static String SimOperator;
    
    static {
        SL_EXCEPT_MODELS.add("ATT");
        USERPIN_EXCEPT_MODELS.add("TMO_EU");
        USERPIN_EXCEPT_MODELS.add("TMO_COM");
        
        OPERATOR = SystemProperties.get("ro.build.target_operator");
        COUNTRY = SystemProperties.get("ro.build.target_country");
    }
    
    public static boolean isSLExceptionModel() {
        return SL_EXCEPT_MODELS.contains(OPERATOR);
    }
    
    public static boolean isUserpinExceptionModel() {
        return USERPIN_EXCEPT_MODELS.contains(OPERATOR + "_" + COUNTRY); 
    }
    
    public static void ShowAlertDialog(String title, String message) {
        Bundle data = new Bundle();
        data.putString("TITLE", title);
        data.putString("MESSAGE", message);
        mHandler.sendMessage(MakeNewMessage(SHOW_ALERT_DIALOG, data));
    }
    
    public static void PrintLogText(String log) {
        Bundle data = new Bundle();
        if (log == null)
            data.putString("LOG", "\n");
        else 
            data.putString("LOG", "["+ LogTimeFormat.format(new Date()) + "]" + log + "\n");
        
        mHandler.sendMessage(MakeNewMessage(PRINT_LOG, data));
    }
    
    public static void PrintWaitingLogText(String log) {
        Bundle data = new Bundle();
        data.putString("LOG", log);
        mHandler.sendMessage(MakeNewMessage(PRINT_LOG, data));
    }
    
    public static void notiPass(String target, Boolean result, String intentVals) {
        Bundle data = new Bundle();
        data.putString("TARGET", target);
        data.putBoolean("RESULT", result);
        data.putString("INTENTVALS", intentVals);
        mHandler.sendMessage(MakeNewMessage(NOTI_PASS, data));
    }
    
    public static Message MakeNewMessage(int type, Bundle data) {
        Message msg = new Message();
        msg.what = type;
        if (data != null) {
            msg.setData(data);
        }
        return msg;
    }
    
    public static String MakeSITestXML(String key){
        return "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE si PUBLIC \"-//WAPFORUM//DTD SI 1.0//EN\" \"http://www.wapforum.org/DTD/si.dtd\">\n" +
                "<si>\n" +
                "<indication si-id=\"" + (key == null ? idFormat.format(new Date()) : key) + "\"\n" +
                "action=\"signal-medium\"\n" +
                "href=\"http://daum.net\"\n" +
                "created=\"2004-01-01T15:10:10Z\"\n" +
                "si-expires=\"2020-12-31T00:00:00Z\">\n" +
                (key == null ? "This is generic test 001" : "SI Auto Test (#" + key + ")")+ 
                "\n</indication>\n" +
                "</si>";
    }
    
    public static String MakeSIExpireTestXML(String key) {
        TimeZone tz = TimeZone.getTimeZone("London");
        rondonFormat.setTimeZone(tz);
//        Log.i(TAG, "ori : " + rondonFormat.format(new Date()));
        long dateM3 = new Date().getTime() + 1000*60*3;
        String siDeletingTime = rondonFormat.format(new Date(dateM3));
        
        return "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE si PUBLIC \"-//WAPFORUM//DTD SI 1.0//EN\" \"http://www.wapforum.org/DTD/si.dtd\">\n" +
                "<si>\n" +
                "<indication si-id=\"" + (key == null ? idFormat.format(new Date()) : key) + "\"\n" +
                "action=\"signal-medium\"\n" +
                "href=\"http://wap.lge.com\"\n" +
                "created=\"2004-01-01T15:10:10Z\"\n" +
                "si-expires=\"" + siDeletingTime + "\">\n" +
                
                (key == null ? "This is generic test 003" + "(send : " + defaltLocateFormat.format(new Date()) + ") \n" +
                        "(expiring : " + defaltLocateFormat.format(new Date(dateM3)) + ")"
                        : "SI Expire Auto Test (#" + key + ")" ) + 
                "\n</indication>\n" +
                "</si>";

/* si update ƒ…¿ÃΩ∫
 *         return "<?xml version=\"1.0\"?>\n" +
        "<!DOCTYPE si PUBLIC \"-//WAPFORUM//DTD SI 1.0//EN\" \"http://www.wapforum.org/DTD/si.dtd\">\n" +
        "<si>\n" +
        "<indication si-id=\"" + 1111 (key == null ? idFormat.format(new Date()) : key) + "\"\n" +
        "action=\"signal-medium\"\n" +
        "href=\"http://wap.lge.com " + dateM3 + "\"\n" +
        "created=\"" + defaltLocateFormat.format(new Date()) + "2004-01-01T15:10:10Z\"\n" + "\""+
        "si-expires=\"" + siDeletingTime + "\">\n" +
        
        (key == null ? "This is generic test 003" + "(send : " + defaltLocateFormat.format(new Date()) + ") \n" +
                "(expiring : " + defaltLocateFormat.format(new Date(dateM3)) + ")"
                : "SI Expire Auto Test (#" + key + ")" ) + 
        "\n</indication>\n" +
        "</si>";*/
    }

    public static String MakeSIDeleteTestXML(boolean delete, String key){
        return "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE si PUBLIC \"-//WAPFORUM//DTD SI 1.0//EN\" \"http://www.wapforum.org/DTD/si.dtd\">\n"
                + "<si>\n"
                + "   <indication\n"
                + "      si-id=\"1234\"\n"
                + "      action=\"" + (delete ? "delete" : "signal-medium" ) + "\"\n"
                + "      href=\"http://daum.net\"\n"
                + "      created=\"" + defaltLocateFormat.format(new Date().getTime()) + "\">\n"
                + "      SI Delete Test via push" + (key == null ? "" : " (#" + key + ")\n")
                + "   </indication>\n"
                + "</si>";
    }

    public static String MakeOTAPinTestXML(String key) {
        return "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE wap-provisioningdoc PUBLIC \"-//WAPFORUM//DTD PROV 1.0//EN\" \"http://www.wapforum.org/DTD/prov.dtd\">\n" +
                "<wap-provisioningdoc>\n" +
                "<characteristic type=\"NAPDEF\">\n" +
                "<parm name=\"NAPID\" value=\"APN1\"/>\n" +
                "<parm name=\"BEARER\" value=\"GSM-GPRS\"/>\n" +
                "<parm name=\"NAME\" value=\"OTA-Pin Fail Test" + (key==null ? "" : " (#"+key+")" ) + "\"/>\n" +
                "<parm name=\"NAP-ADDRESS\" value=\"pre.h3g.it\"/>\n" +
                "<parm name=\"NAP-ADDRTYPE\" value=\"APN\"/>\n" +
                "</characteristic>\n" +
                "</wap-provisioningdoc>";
    }
    
    
    public static String MakeAPNTestXML(String key) {
        return "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE wap-provisioningdoc PUBLIC \"-//WAPFORUM//DTD PROV 1.0//EN\" \"http://www.wapforum.org/DTD/prov.dtd\">\n" +
                "<wap-provisioningdoc>\n" +
                "<characteristic type=\"NAPDEF\">\n" +
                "<parm name=\"NAPID\" value=\"APN1\"/>\n" +
                "<parm name=\"BEARER\" value=\"GSM-UMTS\"/>\n" +
                "<parm name=\"NAME\" value=\"APN Setting Test" + (key==null ? "" : " (#"+key+")" ) + "\"/>\n" +
                "<parm name=\"NAP-ADDRESS\" value=\"tb\"/>\n" +
                "<parm name=\"NAP-ADDRTYPE\" value=\"APN\"/>\n" +
                "<characteristic type=\"NAPAUTHINFO\">\n" +
                "<parm name=\"AUTHTYPE\" value=\"PAP\"/>\n" +
                "<parm name=\"AUTHNAME\" value=\"lge\"/>\n" +
                "<parm name=\"AUTHSECRET\" value=\"lge\"/>\n" +
                "</characteristic>\n" +
                "</characteristic>\n" +
                "<characteristic type=\"APPLICATION\">\n" +
                "<parm name=\"APPID\" value=\"w2\"/>\n" +
                "<parm name=\"TO-NAPID\" value=\"APN1\"/>\n" +
                "<characteristic type=\"RESOURCE\">\n" +
                "<parm name=\"NAME\" value=\"T\"/>\n" +
                "<parm name=\"URI\" value=\"http://www.daum.net\"/>\n" +
                "<parm name=\"STARTPAGE\"/>\n" +
                "</characteristic>\n" +
                "</characteristic>\n" +
                "</wap-provisioningdoc>";
    }
    
    public static String MakeNetPinAPNTestXML(boolean correct, String key) {
        return "<!DOCTYPE wap-provisioningdoc PUBLIC \"-//WAPFORUM//DTD PROV 1.0//EN\" \"http://www.wapforum.org/DTD/prov.dtd\">\n"
                + "<wap-provisioningdoc>\n"
                + "<characteristic type=\"NAPDEF\">\n"
                + "  <parm name=\"NAPID\" value=\"APN1\"/>\n"
                + "  <parm name=\"BEARER\" value=\"GSM-GPRS\"/>\n"
                + "  <parm name=\"NAME\" value=\"" + (correct ? "Valid" : "Invalid") + " network pin/APN Test"  + (key==null ? "" : " (#"+key+")" ) +  "\"/>\n"
                + "  <parm name=\"NAP-ADDRESS\" value=\"pre.h3g.it\"/>\n"
                + "  <parm name=\"NAP-ADDRTYPE\" value=\"APN\"/>\n"
                + "</characteristic>\n"
                + "</wap-provisioningdoc>";
    }
    
    
    public static String getSI_DOC_String() {
        return isAuto ? 
                "02056A0045C6110334344831574600070C036461756D2E6E6574000AC3072004010115101010C3042020123101035349204175" +
                "746F20546573742028233434483157462920000101"
                : "02056A0045C61103323031343031323132303238353900070C036461756D2E6E6574000AC3072004010115101010C30420201" +
                		"2310103546869732069732067656E65726963207465737420303031000101";
    }
    
    public static String getSID_DOC_String(boolean delete) {
        if (delete) {
            return isAuto ? "" : "02056A0045C611033132333400090C036461756D2E6E6574000AC307201409041125580"
                    + "10353492044656C6574652054657374207669612070757368202020000101";
        } else {
            return isAuto ? "" : "02056A0045C611033132333400070C036461756D2E6E6574000AC307201409041124570"
                    + "10353492044656C6574652054657374207669612070757368202020000101";
        }
    }
    
    public static String getSL_DOC_String() {
        return isAuto ? 
                "02066A008509036E617665722E636F6D20282358494932333029000601"
                : "02066A008509036E617665722E636F6D000601";
    }
    
    public static String getSIE_DOC_String() {
        SimpleDateFormat rondonIdFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        TimeZone tz = TimeZone.getTimeZone("London");
        rondonIdFormat.setTimeZone(tz);
        long dateM3 = new Date().getTime() + 1000*60*3;

        String rondonTime = rondonIdFormat.format(new Date(dateM3));

        return isAuto ? 
                "02056A0045C61103554C3937333600070C037761702E6C67652E636F6D000AC3072004010115101010C307" +
                rondonTime +
                "0103534920457870697265204175746F2054657374202823554C393733362920000101"
                : "02056A0045C61103323031343033313032303134323800070C037761702E6C67652E636F6D000AC3072004010115101010C307" +
                    rondonTime +
                    "0103546869732069732067656E6572696320746573742030303320286578706972696E672920000101";
    }

    public static String getOTAF_DOC_String() {
        return isAuto ? 
                "030B6A0045C655018711060341504E310001871006AB01870706034F544" +
                "12D50696E204661696C20546573742028233036334A3237290001870806037072652E6833672E6974000187090689010101"
                : "030B6A0045C655018711060341504E310001871006AB01870706034F54412D50696E204661696C205465737" +
                		"40001870806037072652E6833672E6974000187090689010101";
    }
    public static String getOTAF_MAC_String() {
        return isAuto ? 
                "AE8C7FE1639E13A4B2718C76DA89CFC9779565C9"
                : "436C4A8807D956BA6533D6D95AB0781E4052D0E5";
    }
    
    public static String getAPN_DOC_String() {
        return isAuto ? 
                "030B6A096C67650041504E310045C655018711068304018710060347534D2D554D545300018707060341504E2053" +
                "657474696E67205465737420282337513230323229000187080603746200018709068901C65A01870C069A01870D06830001870" +
                "E068300010101C60001550187360000060377320001872206830401C6000159018707000006035400018700013A000006036874" +
                "74703A2F2F7777772E6461756D2E6E65740001871C01010101"
                : "030B6A096C67650041504E310045C655018711068304018710060347534D2D554D545300018707060341504E2053657474696" +
                		"E672054657374000187080603746200018709068901C65A01870C069A01870D06830001870E068300010101C6000155" +
                		"0187360000060377320001872206830401C6000159018707000006035400018700013A00000603687474703A2F2F7777" +
                		"772E6461756D2E6E65740001871C01010101";
    }
    public static String getAPN_MAC_String() {
        return isAuto ? 
                "B1694A89420FB695728A48A3A257EB587F6FBB29"
                : "203D26770A25DE2A20B0E58496443972C8F9A908";
    }
}
