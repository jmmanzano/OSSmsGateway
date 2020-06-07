package es.jmmanzano.ossmsgateway.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import fi.iki.elonen.NanoHTTPD;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class Utils {

    public static NanoHTTPD.Response getResponse(String content, String mime, boolean isObject){
        String defaultMime = "application/json";
        String mimeToUse = mime.equals("") ? defaultMime : mime;
        if(isObject){
            try{
                JSONObject resp = (JSONObject) new JSONTokener(content).nextValue();
                NanoHTTPD.Response response = newFixedLengthResponse(resp.toString(2));
                response.addHeader("Access-Control-Allow-Origin", "*");
                response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
                response.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
                response.addHeader("Content-Type", mimeToUse);
                return response;
            }catch (JSONException jex){
                System.out.println(jex.toString());
                NanoHTTPD.Response response  = newFixedLengthResponse("[{\"message\":\"Error in JSON process\"}]");
                response.addHeader("Access-Control-Allow-Origin", "*");
                response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
                response.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
                response.addHeader("Content-Type", mimeToUse);
                return response;
            }
        }else{
            try{
                JSONArray resp = (JSONArray) new JSONTokener(content).nextValue();
                NanoHTTPD.Response response = newFixedLengthResponse(resp.toString(2));
                response.addHeader("Access-Control-Allow-Origin", "*");
                response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
                response.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
                response.addHeader("Content-Type", mimeToUse);
                return response;
            }catch (JSONException jex){
                System.out.println(jex.toString());
                NanoHTTPD.Response response  = newFixedLengthResponse("[{\"message\":\"Error in JSON process\"}]");
                response.addHeader("Access-Control-Allow-Origin", "*");
                response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
                response.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
                response.addHeader("Content-Type", mimeToUse);
                return response;
            }
        }
    }
}
