package com.example.spygame;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkController {

    private static final String TAG = "NetworkController";
    private static final String URL = "https://script.google.com/macros/s/AKfycby2NPHIkAVnZb-buosJxzKdgG4IAIL-Clgzpjkvz18pIcptUnc/exec";
    private static NetworkController networkController;
    private OkHttpClient client;

    private NetworkController(){
        client = new OkHttpClient();
    }

    public static NetworkController getInstance(){
        if(networkController == null){
            networkController = new NetworkController();
        }
        return networkController;
    }

    private static class CallbackAdapter implements Callback{

        private CCallback cCallback;

        public CallbackAdapter(CCallback cCallback){
            this.cCallback = cCallback;
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            cCallback.onFailure(e.getMessage());
            cCallback.onCompleted();
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            String str = response.body().string();
            Log.d(TAG, str);

            try {
                JSONObject jsonObject = new JSONObject(str);
                if(!jsonObject.getString("errorcode").equals("-1")){
                    cCallback.onFailure(jsonObject.getString("data"));
                }else{
                    cCallback.onResponse(jsonObject.getJSONObject("data"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally{
                cCallback.onCompleted();
            }
        }

    }

    public interface CCallback{
        public void onFailure(String errorMsg);
        public void onResponse(JSONObject data);
        public void onCompleted();
    }

    public void getPuzzles(CCallback callback){
        RequestBody body = new FormBody.Builder()
                .add("command", "getPuzzles")
                .build();
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();
        client.newCall(request).enqueue(new CallbackAdapter(callback));
    }




}
