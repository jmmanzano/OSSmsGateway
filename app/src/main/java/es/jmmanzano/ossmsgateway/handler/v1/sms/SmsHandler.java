package es.jmmanzano.ossmsgateway.handler.v1.sms;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.jmmanzano.ossmsgateway.MainActivity;
import es.jmmanzano.ossmsgateway.utils.Utils;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;


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
            Map<String, String> params = new HashMap<String, String>();

            try {
                session.parseBody(params);
            } catch (Exception e) {
                return Utils.getResponse("{\"status\":500}", "", true);
            }

            String phone = session.getParameters().get("phone").get(0);
            String message = session.getParameters().get("message").get(0);

            String SENT = "SMS_SENT";
            String DELIVERED = "SMS_DELIVERED";
            Context baseContext = MainActivity.ma.getBaseContext();
            ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
            ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<PendingIntent>();
            PendingIntent sentPI = PendingIntent.getBroadcast(baseContext, 0,
                    new Intent(SENT), 0);

            PendingIntent deliveredPI = PendingIntent.getBroadcast(baseContext, 0,
                    new Intent(DELIVERED), 0);


            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> smsParts = smsManager.divideMessage(message);
            for (int i = 0; i < smsParts.size(); i++) {
                sentPendingIntents.add(i, sentPI);
                deliveredPendingIntents.add(i, deliveredPI);
            }
            smsManager.sendMultipartTextMessage(phone, null, smsParts, sentPendingIntents, deliveredPendingIntents);
            for (PendingIntent pi: sentPendingIntents ) {

            };

            MainActivity.addLog("Message send to :" + phone + " " + message);
            return Utils.getResponse("{\"status\":200}", "", true);
        }else if((session.getMethod() == NanoHTTPD.Method.GET && uriResource.getUri().toString().equals("v1/sms"))
                ||(session.getMethod() == NanoHTTPD.Method.GET && uriResource.getUri().toString().equals("v1/sms/:limit") )){
            int limite = 10;
            if(!urlParams.isEmpty()){
                try{
                    limite = Integer.parseInt(urlParams.get("limit"));
                    MainActivity.addLog("Recuperando : "+limite+" sms");
                }catch (Exception e){
                    return Utils.getResponse("[{\"message\":\"Error reading params\"}]", "", false);
                }

            }
            return Utils.getResponse(readSms(limite), getMimeType(), false);
        }else{
            return Utils.getResponse("[{\"message\":\"Error, not allowed route\"}]", "", false);
        }
    }

    private String readSms(int limite)  {
        Cursor cursor = MainActivity.ma.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, "date desc");
        StringBuilder response = new StringBuilder("[");
        int limit = 0;
        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                String[] columnNames = cursor.getColumnNames();
                String msgData = "{";
                for (int i = 0; i < columnNames.length; i++) {
                    msgData += "'"+columnNames[i]+"':'"+cursor.getString(i)+"',";
                }
                msgData = msgData.substring(0, msgData.length() - 1);
                msgData += "},";
                response.append(msgData);
                limit ++;
            } while (cursor.moveToNext() && limit < limite );
            response.deleteCharAt(response.toString().length()-1);
            response.append("]");
            return response.toString();
        } else {
            return  "[{\"message\":\"No data\"}]";
        }
    }
}
