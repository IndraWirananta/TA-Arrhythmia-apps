package com.uni.ppg.domain.predict;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.lang.Math;
import java.util.Objects;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.jacksonandroidnetworking.JacksonParserFactory;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONException;
import org.json.JSONObject;


import com.uni.ppg.constant.GlobalConstants;
import com.uni.ppg.domain.image.PpgFrameProcessor;
import com.uni.ppg.ui.AfibDetection;

public class Predict {


    private static final String TAG = Predict.class.getName();
    private static String data;

    private final int[] signal;
    private final int[] unprocessedSignal;
    private final long[] timeStamps;

    public Predict(int[] signal, int[] unprocessedSignal, long[] timeStamps) {
        this.signal = signal;
        this.timeStamps = timeStamps;
        this.unprocessedSignal = unprocessedSignal;
    }
    public static void longInfo(String str) {
        if (str.length() > 4000) {
            Log.d(TAG, str.substring(0, 4000));
            longInfo(str.substring(4000));
        } else
            Log.d(TAG, str);
    }
    public void predict()  {
        Log.i(TAG, "catboostPredict");

        int sigLen = this.signal.length;

        double[] ppInterval = new double[sigLen -1];
        int[] hrTimestamp = new int[sigLen];

        for (int y = 0; y < sigLen-1; y++) {
            ppInterval[y] =  this.timeStamps[signal[y+1]] - this.timeStamps[signal[y]];
        }

        for (int z = 0; z < sigLen; z++) {
            hrTimestamp[z] =  (int)this.timeStamps[signal[z]];
        }

        longInfo(" timestamp - " + Arrays.toString(this.timeStamps));
        Log.i(TAG, " PPI - " + Arrays.toString(ppInterval));

        Log.i(TAG, " PPI - " + Arrays.toString(ppInterval));
        Log.i(TAG, " PPI len - " + ppInterval.length);
        Log.i(TAG, " HR - " + Arrays.toString(hrTimestamp));

        double[] feature =  new double[GlobalConstants.rriLength];
        double[] finalFeature =  new double[GlobalConstants.rriLength + GlobalConstants.addedFeature];


        for (int i = 0; i < GlobalConstants.rriLength; i++){
            feature[i] = ppInterval[i]/4; // sampling rate is 250 (250 sample per second), timestamp is in ms, so need to be divided by 4
            finalFeature[i] = ppInterval[i]/4;
        }
        Log.i(TAG, " feature length - " + feature.length);
        Log.i(TAG, " feature base - " + feature.length);


        finalFeature[GlobalConstants.rriLength] = Arrays.stream(feature).max().getAsDouble();
        Log.i(TAG, " Feature - " + Arrays.toString(finalFeature));

        finalFeature[GlobalConstants.rriLength + 1] = Arrays.stream(feature).min().getAsDouble();
        Log.i(TAG, " Feature - " + Arrays.toString(finalFeature));

        finalFeature[GlobalConstants.rriLength + 2] = Arrays.stream(feature).average().getAsDouble();
        Log.i(TAG, " Feature - " + Arrays.toString(finalFeature));

        finalFeature[GlobalConstants.rriLength + 3] = calculateStandardDeviation(feature);
        Log.i(TAG, " Feature - " + Arrays.toString(finalFeature));

        hitPredictApi(finalFeature, this.unprocessedSignal);
    }

    private static void hitPredictApi(double[] feature, int[] unprocessedSignal){

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
                            data = response.getString(GlobalConstants.parameterPrediction);

                            Log.i(TAG+"AndroidNetworking", "prediction : " + data);
                            Log.i(TAG+"AndroidNetworking", "feature : " + Arrays.toString(feature));

//                            appendLog("Prediction : " +data+ " Features : "+ Arrays.toString(feature));
                            SimpleDateFormat format = new SimpleDateFormat("EEEE-MMMM-d-yyyy-h:mm-a");
                            String currentTime = format.format(Calendar.getInstance().getTime());
                            saveFile(AfibDetection.getAppContext(),"Prediction - " + data +" - "+currentTime+"-","Prediction : " +data+ " Features : "+ Arrays.toString(feature) + " Raw Signal : " + Arrays.toString(unprocessedSignal),"txt");

                            if (data.equals("0.0")){
                                PpgFrameProcessor.updateUIPredict("Atrial Fibrillation");
                            }else if (data.equals("1.0")){
                                PpgFrameProcessor.updateUIPredict("Normal Sinus Rhythm");
                            }else if (data.equals("2.0")){
                                PpgFrameProcessor.updateUIPredict("Atrial Flutter");
                            }else if (data.equals("3.0")){
                                PpgFrameProcessor.updateUIPredict("AV junctional rhythm");}
                            else {
                                    PpgFrameProcessor.updateUIPredict("Prediction : -");
                            }
                        } catch (JSONException e) {
                            Log.i(TAG+"AndroidNetworking", "error : " + e);
                            PpgFrameProcessor.updateUIPredict(GlobalConstants.ERROR_MESSAGE);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i(TAG+"AndroidNetworking", "error : " + e);
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        Log.i(TAG+"AndroidNetworking", "onError : " + data);
                    }
                });
    }


    public static void saveFile(Context context, String fileName, String text, String extension) throws IOException{
        OutputStream outputStream;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            ContentValues values = new ContentValues();

            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName + extension);   // file name
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri extVolumeUri = MediaStore.Files.getContentUri("external");
            Uri fileUri = context.getContentResolver().insert(extVolumeUri, values);

            outputStream = context.getContentResolver().openOutputStream(fileUri);
            Log.i("saveFile", "CREATED SAVE FILE at " + fileUri.toString());
        }
        else {
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "ppgLog";
            File file = new File(path, fileName + extension);
            Log.d(TAG, "saveFile: file path - " + file.getAbsolutePath());
            outputStream = new FileOutputStream(file);
        }

        byte[] bytes = text.getBytes();
        outputStream.write(bytes);
        outputStream.close();
    }

//    public static void appendLog(String text)
//    {
//        Context context = AfibDetection.getAppContext();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//
//            ContentValues values = new ContentValues();
//            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "ppgLog" + ".txt");
//            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
//            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);
//
//            Uri extVolumeUri = MediaStore.Files.getContentUri("external");
//            Uri fileUri = context.getContentResolver().insert(extVolumeUri, values);
//
//            try {
//                OutputStream fos =  context.getContentResolver().openOutputStream(fileUri);
//                byte[] bytes = text.getBytes();
//                fos.write(bytes);
//                fos.close();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                Log.i(TAG+"appendLog", " e");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        } else {
//            String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
//            File image = new File(imagesDir, name + ".jpg");
//            try {
//                OutputStream fos = new FileOutputStream(image);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                Log.i(TAG+"appendLog", " e");
//            }
//        }
//
//
//        File f = new File(Environment.getExternalStorageDirectory(), folder_main);
//        if (!f.exists()) {
//            Log.i(TAG+"appendLog", " WRITING FOLDER");
//            f.mkdirs();
//        }
//
//        Log.i(TAG+"appendLog", " WRITING FILE");
//        File logFile = new File(Environment.getExternalStorageDirectory().getPath()
//                +  File.separator + "ppgLog" + File.separator + "/log.file");
//        if (!logFile.exists())
//        {
//            try
//            {
//                logFile.createNewFile();
//            }
//            catch (IOException e)
//            {
//                Log.i(TAG+"appendLog", "onError : " + e);
//                e.printStackTrace();
//            }
//        }
//        try
//        {
//            //BufferedWriter for performance, true to set append to file flag
//            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
//            buf.append(text);
//            buf.newLine();
//            buf.close();
//        }
//        catch (IOException e)
//        {
//            Log.i(TAG+"appendLog", "onError : " + e);
//            e.printStackTrace();
//        }
//    }

    private static double calculateStandardDeviation(double[] array) {

        // get the sum of array
        double sum = 0.0;
        for (double i : array) {
            sum += i;
        }

        // get the mean of array
        int length = array.length;
        double mean = sum / length;

        // calculate the standard deviation
        double standardDeviation = 0.0;
        for (double num : array) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation / length);
    }

//    public void openFolder(String location)
//    {
//        // location = "/sdcard/my_folder";
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        Uri mydir = Uri.parse("file://"+location);
//        intent.setDataAndType(mydir,"application/*");    // or use */*
//        startActivity(intent);
//    }
}

