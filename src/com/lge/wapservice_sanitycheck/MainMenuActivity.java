
package com.lge.wapservice_sanitycheck;

import android.os.Bundle;
import android.os.SystemProperties;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainMenuActivity extends Activity implements OnItemClickListener {

    private ListView mainList;
    private String menuItem[];
    private final static String TAG = "WAP_SanityCheck_MainMenuActivity";
    //private boolean isOffline;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        
        getBaseContext();
        //test code
        //isOffline = false;
        Common_Util.SimOperator = ((TelephonyManager)getBaseContext().getSystemService(Context.TELEPHONY_SERVICE)).getSimOperator();
        
        String spn = ((TelephonyManager)getBaseContext().getSystemService(Context.TELEPHONY_SERVICE)).getSimOperatorName();
        String iccid = SystemProperties.get("persist.sys.iccid");
        String imsi = ((TelephonyManager)getBaseContext().getSystemService(Context.TELEPHONY_SERVICE)).getSubscriberId();
        //String gid = retVal.gid = new com.lge.uicc.LGUiccCard(simID).getGid1();
        
        Log.i(TAG, "ro.com.google.clientidbase.ms = " + SystemProperties.get("ro.com.google.clientidbase.ms"));
        Toast.makeText(getApplicationContext(),
                "ro.com.google.clientidbase.ms = \n  " + SystemProperties.get("ro.com.google.clientidbase.ms"),
                Toast.LENGTH_LONG).show();
        Log.i(TAG, "SIM INFO : mcc-mnc=" + Common_Util.SimOperator + ", spn=" + spn + ", gid=" +
        "test.. , imsi=" + imsi + " iccid : " + iccid);
        Toast.makeText(getApplicationContext(),
                "SIM INFO :\n  mcc-mnc=" + Common_Util.SimOperator + "\n  spn=" + spn + "\n  gid=" +
                        "test.. \n  imsi=" + imsi + "\n  iccid=" + iccid,
                Toast.LENGTH_LONG).show();
        
        
        menuItem = new String[]{"Online SanityCheck", "Offline SanityCheck"};
        mainList = (ListView)findViewById(R.id.mainMenuListView);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, menuItem);
        mainList.setAdapter(adapter);
        mainList.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (view instanceof TextView) {
            Intent intent = new Intent(MainMenuActivity.this, SanityCheckListActivity.class);
            
            String menuString = ((TextView)view).getText().toString();
            if(menuString.equals(menuItem[0])) {
                intent.putExtra("ISOFFLINE", false);
            } else if (menuString.contains(menuItem[1])){
                intent.putExtra("ISOFFLINE", true);
            }
            
            if(intent != null) {
                startActivity(intent);
            }
        } 
    }
    
}
