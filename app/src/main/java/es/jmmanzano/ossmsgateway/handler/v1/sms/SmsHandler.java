package es.jmmanzano.ossmsgateway.handler.v1.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import es.jmmanzano.ossmsgateway.MainActivity;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class SmsHandler extends RouterNanoHTTPD.DefaultHandler {
    public static final String MIME_PLAINTEXT = "text/plain";
    private static final String SENT_SMS_FLAG = "SENT_SMS";

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

    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        MainActivity.addLog("Request to: "+uriResource.toString()+" with method "+session.getMethod());
        if (session.getMethod() == NanoHTTPD.Method.GET && uriResource.getUri().toString().equals("v1/sms/send") ||
                session.getMethod() == NanoHTTPD.Method.POST) {
            Map<String, String> files = new HashMap<String, String>();

            try {
                session.parseBody(files);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NanoHTTPD.ResponseException e) {
                e.printStackTrace();
            }

            String phone = session.getParameters().get("phone").get(0);
            String message = session.getParameters().get("message").get(0);

            String SENT = "SMS_SENT";
            String DELIVERED = "SMS_DELIVERED";
            Context baseContext = MainActivity.ma.getBaseContext();
            PendingIntent sentPI = PendingIntent.getBroadcast(baseContext, 0,
                    new Intent(SENT), 0);

            PendingIntent deliveredPI = PendingIntent.getBroadcast(baseContext, 0,
                    new Intent(DELIVERED), 0);

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone, null, message, sentPI, deliveredPI);
            MainActivity.addLog("Message send to :" + phone + " " + message);
            NanoHTTPD.Response response = newFixedLengthResponse("{'status':200}");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
            response.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
            response.addHeader("Content-Type", getMimeType());
            return response;
        }else if((session.getMethod() == NanoHTTPD.Method.GET && uriResource.getUri().toString().equals("v1/sms"))
                ||(session.getMethod() == NanoHTTPD.Method.GET && uriResource.getUri().toString().equals("v1/sms/:limit") )){
            int limite = 10;
            if(!urlParams.isEmpty()){
                try{
                    limite = Integer.parseInt(urlParams.get("limit"));
                    MainActivity.addLog("Recuperando : "+limite+" sms");
                }catch (Exception e){
                    MainActivity.addLog("Error parseando limite");
                    NanoHTTPD.Response response = newFixedLengthResponse("Error");
                    return response;
                }

            }
            NanoHTTPD.Response response = newFixedLengthResponse(readSms(limite));
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
            response.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
            response.addHeader("Content-Type", getMimeType());
            return response;
        }else{
            MainActivity.addLog("Error, "+NanoHTTPD.Response.Status.NOT_FOUND);
            return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                    "The requested resource does not exist");
        }
    }

    private String readSms(int limite)  {
        Cursor cursor = MainActivity.ma.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        String response = "[";
        int limit = 0;
        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                String msgData = "";
                String smsDate = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));
                String number = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                String body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                msgData += "{'date':'"+smsDate+"','number':'"+number+"','body':'"+body+"'},";
               response += msgData;
                limit ++;
            } while (cursor.moveToNext() && limit < limite );
            response = response.substring(0, response.length() - 1);
            response += "]";
            try{
                JSONArray resp = (JSONArray) new JSONTokener(response).nextValue();
                return resp.toString(2);
            }catch (JSONException jex){
                return "[{'code':403}]";
            }
        } else {
            return  "[{'code':404}]";
        }
    }
}
