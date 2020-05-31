package es.jmmanzano.ossmsgateway.handler.v1.device;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Map;

import es.jmmanzano.ossmsgateway.MainActivity;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

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
                System.err.println("error de permisos");
                NanoHTTPD.Response response = newFixedLengthResponse("[{code:'404'}]");
                response.addHeader("Access-Control-Allow-Origin", "*");
                response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
                response.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
                response.addHeader("Content-Type", getMimeType());
                return  response;

            }

            String nON = tel.getNetworkOperatorName();

            BatteryManager bm = (BatteryManager)MainActivity.ma.getSystemService(Context.BATTERY_SERVICE);
            int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            boolean isChargin = bm.isCharging();
            salida = "{'NetWorkOperatorName': "+nON+", 'batteryLevel': "+batLevel+", 'isCharging':"+isChargin+"}";
        }
        try{
            JSONObject resp = (JSONObject) new JSONTokener(salida).nextValue();
            salida =  resp.toString(2);
        }catch (JSONException jex){
            salida = "[{'code':404}]";
        }


        NanoHTTPD.Response response = newFixedLengthResponse(salida);
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
        response.addHeader("Content-Type", getMimeType());

        return response;
    }
}
