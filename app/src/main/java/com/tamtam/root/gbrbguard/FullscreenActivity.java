package com.tamtam.root.gbrbguard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.SyncStateContract;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */

    TextView gbr_token;

    Button copy_gbr_key,calls_btn, settings_btn,clear_data;

    FrameLayout calls_view,settings_view;

    WebView messageList;


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            if(bundle.get("notification_message") != null) {
                storeMessage(bundle.get("notification_message").toString());
            }
            //NotificationManager manager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            //for(String key: bundle.keySet()){
             //   Log.e(key, bundle.get(key).toString());
            //}
            //manager.cancel();
        }

        copy_gbr_key = (Button)findViewById(R.id.copy_gbr_key);
        calls_btn = (Button)findViewById(R.id.calls_btn);
        settings_btn = (Button)findViewById(R.id.settings_btn);
        clear_data = (Button)findViewById(R.id.clear_data);
        calls_view = (FrameLayout)findViewById(R.id.calls_view);
        settings_view = (FrameLayout)findViewById(R.id.settings_view);
        gbr_token = (TextView)findViewById(R.id.gbr_token);

        messageList = (WebView)findViewById(R.id.message_list);

        try {
            initDefaultView();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        gbr_token.setText(getGBRToken());

        copy_gbr_key.setOnClickListener(new Button.OnClickListener(){
            public  void onClick(View v){
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", gbr_token.getText());
                clipboard.setPrimaryClip(clip);
            }
        });

        calls_btn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                settings_view.setVisibility(View.GONE);
                calls_view.setVisibility(View.VISIBLE);
                try {
                    showMessageList();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        settings_btn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                calls_view.setVisibility(View.GONE);
                settings_view.setVisibility(View.VISIBLE);
                gbr_token.setText(getGBRToken());
            }
        });

        clear_data.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                Dbhelper dbhelper = new Dbhelper(getApplicationContext());
                dbhelper.removeMessages();
            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter

                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    String message = intent.getStringExtra("message");
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    try {
                        showMessageList();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    protected void initDefaultView() throws JSONException {
        settings_view.setVisibility(View.GONE);
        calls_view.setVisibility(View.VISIBLE);
        showMessageList();
    }

    protected String getGBRToken(){
        Dbhelper db = new Dbhelper(getApplicationContext());
        return db.getToken();
    }


    protected void showMessageList() throws  JSONException{
        Dbhelper db = new Dbhelper(getApplicationContext());
        JSONArray messages = db.getMessages(null);
        String html = "<html><body><div>";
        if(messages != null) {
            for (int i = 0; i < messages.length(); i++) {
                String item = "<div>";
                JSONObject row = messages.getJSONObject(i);
                String name = "<div><font face=verdana>" + row.getString("fname") + "</font></div>";
                String address = "<div><font face=verdana>" + row.getString("address") + "</font></div>";
                String created = "<div><font color=green face=monospace>" + row.getString("created") + "</font></div>";
                item = name + address + created + "</div><hr/>";
                html += item;
            }
            if(messages.length() < 1) {
                html += "<h3>Вызовов пока нету</h3></div></body></html>";
            }else {
                html += "</div><div>&nbsp;</div></body></html>";
            }
        }
        messageList.getSettings().setDefaultTextEncodingName("utf-8");
        messageList.loadDataWithBaseURL(null,html, "text/html", "utf-8",null);
    }

    public void storeMessage(String msg){
        String[] row = msg.split(";");
        Dbhelper db = new Dbhelper(getApplicationContext());
        db.saveMessage(row[0],row[1], row[2]);
    }

    public static  void updateMessageText(String content, String from){
        Log.e("SMS", "MEEEEEEEEEESSSSSSSSSSSSSSSSSSSAAAAAAAAAAAAAAGGGGGGEEEEEEEEEEE");
        String[] row = content.split(";");
        if(row != null){
            if(row.length > 1){
                String name = row[0];
                if(row[1] != null) {
                    String geo[] = row[1].split(",");
                    if(geo != null){
                        String html = "<div>";
                        Date d = new Date();
                        name = "<div><font face=verdana>" + row[0] + "</font></div>";
                        String address = "<div><font face=verdana>" + geo[0] + " " + geo[1] + "</font></div>";
                        String created = "<div><font color=green face=monospace>" + d.toString() + "</font></div>";
                        html = name + address + created + "</div><hr/>";
                        Log.d("SMS", html);
                    }
                }
            }else{
                if(row.length > 0){
                    String name = row[0];
                }
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            showMessageList();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));
        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        //NotificationUtils.clearNotifications(getApplicationContext());

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        try {
            showMessageList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        try {
            showMessageList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
