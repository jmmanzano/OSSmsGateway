package es.jmmanzano.ossmsgateway;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
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
        try {
            httpServer = new HttpServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        Button btnLimpiar = (Button) findViewById(R.id.btnLimpiar);
        btnLimpiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logTextView.setText("");
            }
        });
        // static variable for context into handler
        ma = this;
    }


    private void startServer(){
        boolean perm1 = Boolean.FALSE;
        boolean perm2 = Boolean.FALSE;
        checkPermissions(Manifest.permission.READ_SMS, MY_PERMISSIONS_REQUEST_READ_SMS, PERMISSIONS_READ_SMS);
        try {

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
