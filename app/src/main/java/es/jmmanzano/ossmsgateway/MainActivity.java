package es.jmmanzano.ossmsgateway;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 2;
    private boolean PERMISSIONS_SEND_SMS = Boolean.FALSE;
    private boolean PERMISSIONS_READ_PHONE = Boolean.FALSE;
    private boolean PERMISSIONS_READ_SMS = Boolean.FALSE;
    private static TextView logTextView;
    private HttpServer httpServer;
    public static MainActivity ma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logTextView = (TextView) findViewById(R.id.logTextView);
        logTextView.setText("");

        ToggleButton toggle = (ToggleButton) findViewById(R.id.btnOnOff);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startServer();
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
                    addLog("SERVER STARTER IN "+ipAddress+":8080");
                } else {
                    stopServer();
                    addLog("SERVER STOPPED");
                }
            }
        });
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        addLog("SMS SEND ");
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        addLog("SMS DELIVERED");
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));
        // static variable for context into handler
        ma = this;
    }


    private void startServer(){
        boolean perm1 = Boolean.FALSE;
        boolean perm2 = Boolean.FALSE;
        checkPermissions(Manifest.permission.READ_SMS, MY_PERMISSIONS_REQUEST_READ_SMS, PERMISSIONS_READ_SMS);
        try {
            httpServer = new HttpServer();
            httpServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void stopServer(){
        httpServer.stop();
    }

    private boolean checkPermissions(String permission, int permissionRequest, boolean isPermissionGranted){
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {
           if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission)) {
                return isPermissionGranted;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{permission},
                        permissionRequest);
                return isPermissionGranted;
           }
        }else {
            return Boolean.TRUE;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    PERMISSIONS_SEND_SMS = Boolean.TRUE;
                    checkPermissions(Manifest.permission.READ_PHONE_STATE, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE, PERMISSIONS_READ_PHONE);
                } else {
                    PERMISSIONS_SEND_SMS = Boolean.FALSE;
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PERMISSIONS_READ_PHONE = Boolean.TRUE;
                    checkPermissions(Manifest.permission.READ_SMS, MY_PERMISSIONS_REQUEST_READ_SMS, PERMISSIONS_READ_SMS);
                } else {
                    PERMISSIONS_READ_PHONE = Boolean.FALSE;
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_READ_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PERMISSIONS_READ_SMS = Boolean.TRUE;
                    checkPermissions(Manifest.permission.SEND_SMS, MY_PERMISSIONS_REQUEST_SEND_SMS, PERMISSIONS_SEND_SMS);

                } else {
                    checkPermissions(Manifest.permission.READ_SMS, MY_PERMISSIONS_REQUEST_READ_SMS, PERMISSIONS_READ_SMS);
                    PERMISSIONS_READ_SMS = Boolean.FALSE;
                }
                return;
            }
        }
    }


    public static void addLog(String text){
        logTextView.append(text);
        logTextView.append("\n");
        logTextView.setMovementMethod(new ScrollingMovementMethod());
    }
}
