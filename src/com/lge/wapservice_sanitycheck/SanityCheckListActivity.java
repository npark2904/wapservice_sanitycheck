
package com.lge.wapservice_sanitycheck;

import java.util.ArrayList;
import java.util.HashMap;

import com.lge.wapservice_sanitycheck.receiver.WapPush_Auto_SanityReceiver;
import com.lge.wapservice_sanitycheck.receiver.WapPush_Manual_SanityReceiver;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class SanityCheckListActivity extends Activity {

    public static String TAG = "WSCT_List";
    private final String DEFAULT_OTAPIN = "1234";

    private UIMassgeHandler mHandler = new UIMassgeHandler();
    private boolean isOffline;
    private boolean isAuto;

    private String phoneNum;

    private LinearLayout CheckListView;
    
    private ToggleButton togBtn;
    
    private Button siBtn;
    private Button slBtn;
    private Button sieBtn;
    private Button sidPostBtn;
    private Button sidDelBtn;
    private Button otaBtn;
    private Button apnBtn;
    private Button netPinBtn;
    private Button dualSiBtn;
    private Button dualSlBtn;
    private Button dualSiDelBtn;
    private Button dualOtaBtn;
    private Button dualApnBtn;
    private Button autoStartBtn;
    

    private EditText otaField;
    private EditText apnField;
    private EditText dualOtaField;
    private EditText dualApnField;
    
    private TextView phoneNumView;
    private TextView modelOperatorView;
    private TextView modelCountryView;
    private TextView logTextView;
    
    private ScrollView logScroll;
    
    private HashMap<String, TextView> resultText = new HashMap<String, TextView>();
    
    private WapPush_Auto_SanityReceiver autoReceiver;
    private WapPush_Manual_SanityReceiver manualReceiver;
    
    
    @Override
    protected void onResume() {
        super.onResume();
//        initReceiver();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
//        if (receiver != null) {
//            receiver.unregisterReceiver(getApplicationContext());
//            receiver = null;
//        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (autoReceiver != null) {
            autoReceiver.unregisterReceiver(getApplicationContext());
            autoReceiver = null;
        }
        if (manualReceiver != null) {
            manualReceiver.unregisterReceiver(getApplicationContext());
            manualReceiver = null;
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //스크린 on 유지
        requestWindowFeature(Window.FEATURE_NO_TITLE); //타이틀바 없애기
        
        isOffline = false;
        Common_Util.mHandler = mHandler;
        Bundle intentData = getIntent().getExtras();
        if (intentData != null) {
            isOffline = intentData.getBoolean("ISOFFLINE");
        }
        
        setContentView(R.layout.activity_sanity_check_list);
        
        modelOperatorView = (TextView)findViewById(R.id.operatorView);
        modelCountryView = (TextView)findViewById(R.id.countryView);
        phoneNumView = (TextView)findViewById(R.id.phoneNumView);
        togBtn = (ToggleButton)findViewById(R.id.togBtn);
        
        togBtn.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    togBtn.setBackgroundColor(Color.parseColor("#777777"));
                    togBtn.setTextColor(Color.parseColor("#FFFFFF"));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    togBtn.setBackgroundColor(Color.parseColor("#CFCFCF"));
                    togBtn.setTextColor(Color.parseColor("#11AFDF"));
                }
                return false;
            }
        });
        
        togBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isAuto = togBtn.isChecked();
                Common_Util.isAuto = isAuto;
                if (togBtn.isChecked()) {
                    initAutoLayout();
                    togBtn.setText("Auto");
                }
                else {
                    initManualLayout();            
                    togBtn.setText("Manual");
                }
            }
        });
        
        initTestMode();
        initModelInfo();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    private boolean SetPhoneNum() {
        if (isOffline) {
            phoneNumView.setText("Phone Number : Offline Test\n" +
                    "(TBD SIM이 없는 경우, APN 설정 및 Network Pin test 불가능)");
            phoneNumView.setTextColor(Color.YELLOW);
            return true;
        } else {
            TelephonyManager telManager = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            phoneNum = telManager.getLine1Number();
            if(phoneNum == null) {
                phoneNumView.setTextColor(Color.RED);
                phoneNumView.setText("Phone Number : 알수 없음 - SIM을 확인하세요");
                return false;
            } else {
                phoneNumView.setText("Phone Number : " + phoneNum);
            }
            ////-- test code : checking mccmnc & Operator;
            String s = "NetworkOperator:"+telManager.getNetworkOperator()+" / NetworkOperatorName:"+telManager.getNetworkOperatorName()+"\n"+
                    "SimOperator:"+telManager.getSimOperator() + " / SimOperatorName:" + telManager.getSimOperatorName();
            Log.i(TAG, s);
    
            String mProduct = null;
            mProduct = SystemProperties.get("ro.build.product", "DEFAULT");
            Log.i(TAG, "mProduct:"+mProduct);
            //////////////////////////////////////////////
            return true;
        }
    }
    
    private boolean isSingleSIM() {
        return true;
    }
    
    private void disableSingleSIM() {
        if (isAuto) {
            autoStartBtn.setEnabled(false);
            autoStartBtn.setTextColor(Color.rgb(110, 110, 110));
        } else {
            otaField.setEnabled(false);
            apnField.setEnabled(false);
            siBtn.setEnabled(false);
            slBtn.setEnabled(false);
            sieBtn.setEnabled(false);
            sidPostBtn.setEnabled(false);
            sidDelBtn.setEnabled(false);
            otaBtn.setEnabled(false);
            apnBtn.setEnabled(false);
            netPinBtn.setEnabled(false);
        }
    }
    
    private void enableSingleSIM() {
        if (isAuto) {
            autoStartBtn.setEnabled(true);
            autoStartBtn.setTextColor(Color.BLACK);
        } else {
            otaField.setEnabled(true);
            apnField.setEnabled(true);
            siBtn.setEnabled(true);
            slBtn.setEnabled(true);
            sieBtn.setEnabled(true);
            
            if (sidDelBtn.getText().toString().equals("Wait")) {
                sidPostBtn.setEnabled(true);
                sidDelBtn.setEnabled(false);
            } else {
                sidPostBtn.setEnabled(false);
                sidDelBtn.setEnabled(true);
            }
            
            otaBtn.setEnabled(true);
            apnBtn.setEnabled(true);
            
            if (isOffline)
                netPinBtn.setEnabled(false);
            else
                netPinBtn.setEnabled(true);
        }
    }
    
    private void disableDualSIM() {
        if (isAuto) {}
        else {
            dualOtaField.setEnabled(false);
            dualApnField.setEnabled(false);
            dualSiBtn.setEnabled(false);
            dualSlBtn.setEnabled(false);
            dualSiDelBtn.setEnabled(false);
            dualOtaBtn.setEnabled(false);
            dualApnBtn.setEnabled(false);
        }
    }
    
    private void initManualLayout() {
        initManualReceiver();
        
        LinearLayout sc = (LinearLayout)findViewById(R.id.ScrollPanel);
        CheckListView = (LinearLayout)getLayoutInflater().inflate(R.layout.manual_list_view, null);
        sc.removeAllViews();
        sc.addView(CheckListView);
        
        logScroll = (ScrollView)CheckListView.findViewById(R.id.LogScrollView);
        logTextView = (TextView)CheckListView.findViewById(R.id.LogTextField);
        
        siBtn = (Button)CheckListView.findViewById(R.id.SIPostBtn);
        slBtn = (Button)CheckListView.findViewById(R.id.SLPostBtn);
        sieBtn = (Button)CheckListView.findViewById(R.id.SIEPostBtn);
        sidPostBtn = (Button)CheckListView.findViewById(R.id.SIDPostBtn);
        sidDelBtn = (Button)CheckListView.findViewById(R.id.SIDDelBtn);
        otaBtn = (Button)CheckListView.findViewById(R.id.OTAPostBtn);
        apnBtn = (Button)CheckListView.findViewById(R.id.APNPostBtn);
        netPinBtn = (Button)CheckListView.findViewById(R.id.netPinBtn);

        dualSiBtn = (Button)CheckListView.findViewById(R.id.dualSIPostBtn);
        dualSlBtn = (Button)CheckListView.findViewById(R.id.dualSLPostBtn);
        dualSiDelBtn = (Button)CheckListView.findViewById(R.id.dualSIDeletingPostBtn);
        dualOtaBtn = (Button)CheckListView.findViewById(R.id.dualOTAPostBtn);
        dualApnBtn = (Button)CheckListView.findViewById(R.id.dualAPNPostBtn);

        otaField = (EditText)CheckListView.findViewById(R.id.OTA_field);
        apnField = (EditText)CheckListView.findViewById(R.id.OTA_APN_field);
        dualOtaField = (EditText)CheckListView.findViewById(R.id.dualOTA_field);
        dualApnField = (EditText)CheckListView.findViewById(R.id.dualOTA_APN_field);
        

        if (isSingleSIM()) disableDualSIM();
        else disableSingleSIM();
        
        if (!SetPhoneNum()) {
            disableSingleSIM(); return;
        }
        
        // 상황에 따른 버튼 활성화
        if(Common_Util.isSLExceptionModel()) {
            slBtn.setEnabled(false);
        }
        if (sidDelBtn.getText().toString().equals("Wait")) {
            sidPostBtn.setEnabled(true);
            sidDelBtn.setEnabled(false);
        } else {
            sidPostBtn.setEnabled(false);
            sidDelBtn.setEnabled(true);
        }
        if (isOffline)
            netPinBtn.setEnabled(false);
        else
            netPinBtn.setEnabled(true);
        ///
        

        siBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new TestCase(getApplicationContext(), phoneNum, isOffline, isAuto).Single_SITest();
                disableSingleSIM();
            }
        });
        
        slBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new TestCase(getApplicationContext(), phoneNum, isOffline, isAuto).Single_SLTest();
                disableSingleSIM();
            }
        });
        
        sieBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new TestCase(getApplicationContext(), phoneNum, isOffline, isAuto).Single_SIExpireTest();
                disableSingleSIM();
            }
        });
        
        sidPostBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new TestCase(getApplicationContext(), phoneNum, isOffline, isAuto).Single_SIDeleteTest(false);
                sidDelBtn.setText("Delete");
                disableSingleSIM();
            }
        });
        
        sidDelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new TestCase(getApplicationContext(), phoneNum, isOffline, isAuto).Single_SIDeleteTest(true);
                sidDelBtn.setText("Wait");
                disableSingleSIM();
            }
        });
        
        otaBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new TestCase(getApplicationContext(), phoneNum, isOffline, isAuto).Single_OTAPinTest(otaField.getText().toString());
                disableSingleSIM();
            }
        });
        
        
        apnBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new TestCase(getApplicationContext(), phoneNum, isOffline, isAuto).Single_APNTest(apnField.getText().toString());
                disableSingleSIM();
            }
        });
        
        netPinBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new TestCase(getApplicationContext(), phoneNum, isOffline, isAuto).Single_NetpinAPNTest();
                disableSingleSIM();
            }
        });
    }
    
    private void initTestMode() {
        isAuto = togBtn.isChecked();
        Common_Util.isAuto = isAuto;
        
        if (togBtn.isChecked()) {
            initAutoLayout();
            togBtn.setText("Auto");
        }
        else {
            initManualLayout();            
            togBtn.setText("Manual");
        }
    }
    
    private void initAutoReceiver() {
        if (manualReceiver != null) {
            manualReceiver.unregisterReceiver(getApplicationContext());
            manualReceiver = null;
        }
        
        if (autoReceiver != null)
            autoReceiver.unregisterReceiver(getApplicationContext());
        autoReceiver = new WapPush_Auto_SanityReceiver();
        autoReceiver.registerReceiver(getApplicationContext());
    }
    
    private void initManualReceiver() {
        if (autoReceiver != null) {
            autoReceiver.unregisterReceiver(getApplicationContext());
            autoReceiver = null;
        }
        
        if (manualReceiver != null)
            manualReceiver.unregisterReceiver(getApplicationContext());
        manualReceiver = new WapPush_Manual_SanityReceiver();
        manualReceiver.registerReceiver(getApplicationContext());
    }
    
    private void initAutoLayout() {
        initAutoReceiver();
        
        LinearLayout sc = (LinearLayout)findViewById(R.id.ScrollPanel);
        CheckListView = (LinearLayout)getLayoutInflater().inflate(R.layout.auto_list_view, null);
        sc.removeAllViews();
        sc.addView(CheckListView);
        
        resultText.put("SI", (TextView)findViewById(R.id.siResult));
        resultText.put("SL", (TextView)findViewById(R.id.slResult));
        resultText.put("SIE", (TextView)findViewById(R.id.sieResult));
        resultText.put("OTAF", (TextView)findViewById(R.id.otafResult));
        resultText.put("APN", (TextView)findViewById(R.id.apnResult));

        logScroll = (ScrollView)CheckListView.findViewById(R.id.LogScrollView);
        logTextView = (TextView)CheckListView.findViewById(R.id.LogTextField);
        autoStartBtn = (Button)CheckListView.findViewById(R.id.autoStartBtn);
        
        if (isSingleSIM()) disableDualSIM();
        else disableSingleSIM();
        
        if (!SetPhoneNum()) {
            disableSingleSIM(); return;
        }
        
        resultText.get("OTAF").setOnClickListener(null);
        resultText.get("APN").setOnClickListener(null);
                
        autoStartBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                autoStartBtn.setEnabled(false);
                autoStartBtn.setTextColor(Color.rgb(110, 110, 110));
                Common_Util.KEYLIST.clear();
                logTextView.setText("");
                
                runOnUiThread(new Thread() {
                    public void run() {
                        final ArrayList<Thread> tarr = new ArrayList<Thread>();
                        
                        tarr.add(new TestCase(getApplicationContext(), phoneNum, isOffline, isAuto).Single_SITest());
                        Common_Util.PrintLogText("Send SI message");
                        resultText.get("SI").setText("wait..");

                        tarr.add(new TestCase(getApplicationContext(), phoneNum, isOffline, isAuto).Single_OTAPinTest(DEFAULT_OTAPIN));
                        Common_Util.PrintLogText("Send OTA Fail test message");
                        resultText.get("OTAF").setText("wait..");
                        
                        tarr.add(new TestCase(getApplicationContext(), phoneNum, isOffline, isAuto).Single_APNTest(DEFAULT_OTAPIN));
                        Common_Util.PrintLogText("Send OTA - Set APN test message");
                        resultText.get("APN").setText("wait..");
                        
                        tarr.add(new TestCase(getApplicationContext(), phoneNum, isOffline, isAuto).Single_SIExpireTest());
                        Common_Util.PrintLogText("Send SI Expire message");
                        resultText.get("SIE").setText("wait..");
                        
                        if(!Common_Util.isSLExceptionModel()) {
                            tarr.add(new TestCase(getApplicationContext(), phoneNum, isOffline, isAuto).Single_SLTest());
                            Common_Util.PrintLogText("Send SL message");
                            resultText.get("SL").setText("wait..");
                        } else resultText.get("SL").setText("N/T");
                        
                        new Thread() {
                            public void run() {
                                for (Thread th : tarr) {
                                    try {
                                        if (th != null) {
                                            th.join();
                                        }
                                    } catch (InterruptedException e) {}
                                }
                                Common_Util.PrintLogText(null);
                                Common_Util.PrintLogText("--- waiting push message ---");
                            }
                        }.start();

                    }
                });
            }
            
        });

        if (isOffline) {
            new AlertDialog.Builder(this)
                .setTitle("Caution")
                .setMessage("기존에 받은 Offline Wap Push Message를 삭제하지 않으면 정상 동작하지 않습니다.")
                .setPositiveButton(android.R.string.ok, null).setCancelable(false)
                .create()
                .show();
        }
    }

    private void initModelInfo() {
        modelCountryView.setText(modelCountryView.getText() + Common_Util.COUNTRY);
        modelOperatorView.setText(modelOperatorView.getText() + Common_Util.OPERATOR);
    }
    
    //--------------------- inner Class ---------------------//
    
    public class UIMassgeHandler extends Handler {
        ProgressDialog waitDialog;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            switch (msg.what) {
            case Common_Util.PROGRESS_DIALOG_OPEN :
                if (waitDialog != null && waitDialog.isShowing()) waitDialog.dismiss();
                waitDialog = ProgressDialog.show(SanityCheckListActivity.this,"전송 중","잠시 기다려주세요.");
                break;
            case Common_Util.PROGRESS_DIALOG_CLOSE :
                if (waitDialog != null && waitDialog.isShowing()) {
                    waitDialog.dismiss();
                }
                enableSingleSIM();
                break;
            case Common_Util.SHOW_ERROR_TOAST :
                Toast.makeText(getApplicationContext(), msg.getData().getString("TOAST"), Toast.LENGTH_SHORT).show();
                break;
            case Common_Util.PRINT_LOG :
                if (msg.getData() != null) {
                    String log = msg.getData().getString("LOG");
                    logTextView.append(log);
                    if (!log.equals(".")) {
                        logScroll.post(new Runnable(){
                            @Override
                            public void run() {
                                logScroll.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
                    }
                }
                break;
            case Common_Util.NOTI_PASS :
                if (msg.getData() != null) {
                    final String target = msg.getData().getString("TARGET");
                    boolean result = msg.getData().getBoolean("RESULT");
                    
                    resultText.get(target).setText((result ? "Pass" : "Fail"));
                    if (result) {
                        if (target.equals("OTAF") || target.equals("APN")) {
                            resultText.get(target).setText("Click!");
                            resultText.get(target).setTextColor(Color.rgb(0,50,200));

                            final String intallTarget = msg.getData().getString("INTENTVALS");

                            if (intallTarget != null) {
                                resultText.get(target).setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String[] bf = intallTarget.split(":/:");
                                        Intent intent = new Intent("com.lge.wapservice.prov");
                                        intent.putExtra("sec", bf[0]);
                                        intent.putExtra("mac", bf[1]);
                                        intent.putExtra("data", bf[2]);
                                        intent.putExtra("testCode", bf[3]);
                                        intent.putExtra("msguri", "content://sms/"+ bf[4]); // sms uri부분 하드코딩
                                        intent.setAction("android.intent.action.cp.install");
                                        sendBroadcast(intent, "android.permission.RECEIVE_WAP_PUSH");
                                        resultText.get(target).setText("Checked");
                                        resultText.get(target).setTextColor(Color.rgb(242,203,97));
                                        resultText.get(target).setOnClickListener(null);
                                    }
                                });
                            }
                            
                        } else {
                            resultText.get(target).setTextColor(Color.rgb(50, 150, 50));
                        }
                    } else {
                        resultText.get(target).setTextColor(Color.RED);
                    }
                }
                break;
                
            case Common_Util.SHOW_ALERT_DIALOG :
                if (msg.getData() != null) {
                    String title = msg.getData().getString("TITLE");
                    String message = msg.getData().getString("MESSAGE");

                    new AlertDialog.Builder(SanityCheckListActivity.this).setTitle(title).setMessage(message)
                    .setPositiveButton(android.R.string.ok, null).setCancelable(false).create().show();
                }
                break;
                
            default:
                break;
            }
        }
         
    };
}