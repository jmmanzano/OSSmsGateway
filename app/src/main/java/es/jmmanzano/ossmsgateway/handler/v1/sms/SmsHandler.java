package es.jmmanzano.ossmsgateway.handler.v1.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.jmmanzano.ossmsgateway.MainActivity;
import es.jmmanzano.ossmsgateway.utils.Utils;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;


public class SmsHandler extends RouterNanoHTTPD.DefaultHandler {
    private static String SENT = "SMS_SENT";
    private static String DELIVERED = "SMS_DELIVERED";
    private static ArrayList<Integer> listaSmsStatus = new ArrayList<>();
    private BroadcastReceiver brSender = null;
    private BroadcastReceiver brDelivered = null;

    public SmsHandler(){
        this.brSender = new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        MainActivity.addLog("SMS SEND ");
                        Toast.makeText(MainActivity.ma.getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(MainActivity.ma.getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(MainActivity.ma.getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(MainActivity.ma.getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(MainActivity.ma.getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
                listaSmsStatus.add(getResultCode());
            }
        };

        this.brDelivered = new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        MainActivity.ma.addLog("SMS DELIVERED");
                        Toast.makeText(MainActivity.ma.getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(MainActivity.ma.getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };


        //---when the SMS has been sent---
        MainActivity.ma.registerReceiver(brSender, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        MainActivity.ma.registerReceiver(brDelivered, new IntentFilter(DELIVERED));
    }

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
            listaSmsStatus = new ArrayList<>();
            smsManager.sendMultipartTextMessage(phone, null, smsParts, sentPendingIntents, deliveredPendingIntents);
            MainActivity.addLog("Message send to :" + phone + " " + message);
            // retry is used to define a timeout.
            // if we need 100 iterations return status 500.
            int retry = 0;
            do {
                retry++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }while (smsParts.size() > listaSmsStatus.size() || retry == 100);
            //
            //-- unregister receivers
            MainActivity.ma.unregisterReceiver(brSender);
            MainActivity.ma.unregisterReceiver(brDelivered);
            //-------------------------------------
            if(retry == 100){
                return Utils.getResponse("{\"status\":500}", "", true);
            }

            for (Integer value: listaSmsStatus) {
                if(value != Activity.RESULT_OK ){
                    return Utils.getResponse("{\"status\":500}", "", true);
                }
            }
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
