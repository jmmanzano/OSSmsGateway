package es.jmmanzano.ossmsgateway.handler.v1.device;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;


import java.util.Map;

import es.jmmanzano.ossmsgateway.MainActivity;
import es.jmmanzano.ossmsgateway.utils.Utils;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;


public class StatusHandler extends RouterNanoHTTPD.DefaultHandler {
    @Override
    public String getText() {
        throw new IllegalStateException("this method should not be called");
    }

    @Override
    public String getMimeType() {
        return "application/json";
    }

    @Override
    public NanoHTTPD.Response.IStatus getStatus() {
        return NanoHTTPD.Response.Status.OK;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        String salida = "";
        MainActivity.addLog("Request to: " + uriResource.toString() + " with method " + session.getMethod());
        if (session.getMethod() == NanoHTTPD.Method.GET && uriResource.getUri().toString().equals("v1/device/status")) {
            TelephonyManager tel = (TelephonyManager) MainActivity.ma.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(MainActivity.ma, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return Utils.getResponse("{\"message\":\"READ PHONE STATE permission required\"}", "", true);
            }

            String nON = tel.getNetworkOperatorName();
            BatteryManager bm = (BatteryManager)MainActivity.ma.getSystemService(Context.BATTERY_SERVICE);
            int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            boolean isChargin = bm.isCharging();
            salida = "{'NetWorkOperatorName': "+nON+", 'batteryLevel': "+batLevel+", 'isCharging':"+isChargin+"}";
        }
        return Utils.getResponse(salida, "", true);
    }
}
