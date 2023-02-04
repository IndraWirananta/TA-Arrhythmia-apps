package com.uni.ppg.domain.predict;

import android.os.AsyncTask;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.jacksonandroidnetworking.JacksonParserFactory;
import com.uni.ppg.constant.GlobalConstants;
import com.uni.ppg.domain.image.PpgFrameProcessor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;


public class PredictAPI extends AsyncTask<double[], String, String> {


    private static final String TAG = PredictAPI.class.getName();
    static String data;


    protected String doInBackground(double[]... feature) {
        Log.i("doInBackground", "doInBackground");
        String url = GlobalConstants.URL;

        AndroidNetworking.setParserFactory(new JacksonParserFactory());
        AndroidNetworking.post(url)
                .addBodyParameter("rri", Arrays.toString(feature))
                .setTag("test")
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
//                            Log.i(TAG, response.toString());
                            data = response.getString(GlobalConstants.parameterPrediction);
                        } catch (JSONException e) {
                            data = GlobalConstants.ERROR_MESSAGE;
                        }
//                        Log.i(TAG, "data : " + data);
                    }
                    @Override
                    public void onError(ANError error) {
                        data = GlobalConstants.ERROR_MESSAGE;
                    }
                });
//        Log.i("doInBackground", data);
        return data;
    }

    protected void onPostExecute(String result) {
//        Log.i("onPostExecute", result);
        if (data == "1.0"){
            PpgFrameProcessor.updateUIPredict("Prediction - Atrial Fibrillation");
        }else if (data == "2.0"){
            PpgFrameProcessor.updateUIPredict("Prediction - Normal Sinus Rhythm");
        }else if (data == "3.0"){
            PpgFrameProcessor.updateUIPredict("Prediction - Atrial Flutter");
        }else {
            PpgFrameProcessor.updateUIPredict("Prediction - AV junctional rhythm");
        }

    }

    public void appendLog(String text)
    {
        File logFile = new File("sdcard/log.file");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
