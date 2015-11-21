package com.kien.luna.communication;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SendJsonTask extends AsyncTask<Message, Boolean, Boolean> {
    private static final String TAG = "SendJsonTask";

    @Override
    protected Boolean doInBackground(Message... msgs) {
        Message msg = msgs[0];
        Boolean result = post_message(msg);
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {

    }

    private Boolean post_message(Message msg)
    {
        String[] parts = msg.getMessage().split(";");
        Log.e(TAG, msg.getMessage());

        String http = msg.getRemoteDevice();
        StringBuilder sb = new StringBuilder();

        HttpURLConnection urlConnection=null;
        try {
            URL url = new URL(http);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setUseCaches(false);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            urlConnection.setRequestProperty("Content-Type","application/json");
            urlConnection.connect();

            //Create JSONObject here
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("user_id", parts[0]);
            jsonParam.put("event", parts[1]);
            jsonParam.put("area", parts[2]);
            jsonParam.put("steps", parts[3]);
            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(jsonParam.toString());
            out.close();

            int HttpResult =urlConnection.getResponseCode();
            if(HttpResult ==HttpURLConnection.HTTP_OK){
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream(),"utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                System.out.println(""+sb.toString());

            }else{
                System.out.println(urlConnection.getResponseMessage());
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        catch (IOException e) {
            return false;
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }finally{
            if(urlConnection!=null)
                urlConnection.disconnect();
        }
        return true;
    }
}
