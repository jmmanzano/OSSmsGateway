package es.jmmanzano.ossmsgateway.handler.v1.thread;

import android.database.Cursor;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

import es.jmmanzano.ossmsgateway.MainActivity;
import es.jmmanzano.ossmsgateway.utils.Utils;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;


public class ThreadHandler extends RouterNanoHTTPD.DefaultHandler {

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
        NanoHTTPD.Response response;
        if(session.getMethod() == NanoHTTPD.Method.GET && uriResource.getUri().toString().equals("v1/thread/:thread_id")){
            int limite = 100;
            String thread_id = "";
            if (!urlParams.isEmpty()) {
                try {
                    thread_id = urlParams.get("thread_id");
                    //limite = Integer.parseInt(urlParams.get("thread_id"));
                } catch (Exception e) {
                    return Utils.getResponse("[{\"message\":\"Error reading params\"}]", "", false);
                }
            }
            return Utils.getResponse(readThread(thread_id, limite), getMimeType(), false);
        }else  if ((session.getMethod() == NanoHTTPD.Method.GET && uriResource.getUri().toString().equals("v1/thread"))) {
            int limit = 10;
            int offset = 0;
            Map<String, String> params = new HashMap<String, String>();
            try {
                session.parseBody(params);
            } catch (Exception e) {
                return Utils.getResponse("{\"status\":500}", "", true);
            }
            try{
                offset = Integer.parseInt(session.getParameters().get("offset").get(0));
                limit = Integer.parseInt(session.getParameters().get("limit").get(0));
            }catch (Exception e){
            }

            /*int limite = 10;
            if (!urlParams.isEmpty()) {
                try {
                    limite = Integer.parseInt(urlParams.get("thread_id"));
                } catch (Exception e) {
                    return Utils.getResponse("[{\"message\":\"Error reading params\"}]", "", false);
                }
            }*/
            return Utils.getResponse(readThreads(limit, offset), getMimeType(), false);
        }
            return Utils.getResponse("[{\"message\":\"Error, not allowed route\"}]", "", false);
    }

    private String readThreads(int limite, int offset)  {
        Cursor cursor = MainActivity.ma.getContentResolver().query(Uri.parse("content://mms-sms/conversations/"), null, null, null, "date desc");
        StringBuilder response = new StringBuilder("[");
        int limit = 0;
        int countOffSet = 0;
        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                if(countOffSet >= offset){
                    String[] columnNames = cursor.getColumnNames();
                    String msgData = "{";
                    for (int i = 0; i < columnNames.length; i++) {
                        msgData += "'"+columnNames[i]+"':'"+cursor.getString(i)+"',";
                    }
                    msgData = msgData.substring(0, msgData.length() - 1);
                    msgData += "},";
                    response.append(msgData);
                    limit ++;
                }
                countOffSet++;
            } while (cursor.moveToNext() && limit < limite );
            response.deleteCharAt(response.toString().length()-1);
            response.append("]");
            return response.toString();
        } else {
            return  "[{\"message\":\"No data\"}]";
        }
    }
    private String readThread(String thread_id, int limite)  {
        Cursor cursor = MainActivity.ma.getContentResolver().query(Uri.parse("content://sms/inbox"), null, "thread_id = "+thread_id, null, "date desc");
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
