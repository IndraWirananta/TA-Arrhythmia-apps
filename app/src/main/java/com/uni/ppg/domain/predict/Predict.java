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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.lang.Math;
import java.util.HashMap;
import java.util.Objects;

import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.jacksonandroidnetworking.JacksonParserFactory;


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

    public static double findMedian(double[] arr) {
        Arrays.sort(arr);
        int n = arr.length;
        if (n % 2 == 0) {
            return (double)(arr[n/2] + arr[n/2 - 1]) / 2;
        } else {
            return (double) arr[n/2];
        }
    }

    public static double findMode(double[] arr) {
        HashMap<Double  , Integer> map = new HashMap<>();
        for (double i : arr) {
            if (map.containsKey(i)) {
                map.put(i, map.get(i) + 1);
            } else {
                map.put(i, 1);
            }
        }
        int maxCount = 0;
        double mode = arr[0];
        for (double i : map.keySet()) {
            if (map.get(i) > maxCount) {
                maxCount = map.get(i);
                mode = i;
            }
        }
        return mode;
    }

    public static double findVariance(double[] arr) {
        int n = arr.length;
        int sum = 0;
        for (double i : arr) {
            sum += i;
        }
        double mean = (double) sum / n;
        double variance = 0;
        for (double i : arr) {
            variance += (i - mean) * (i - mean);
        }
        variance = variance / n;
        return variance;
    }

    public static double[] calculateProbabilities(double[] listValues) {
        Map<Double, Integer> counterValues = new HashMap<>();
        for (double value : listValues) {
            counterValues.put(value, counterValues.getOrDefault(value, 0) + 1);
        }
        double[] probabilities = new double[counterValues.size()];
        int i = 0;
        for (Map.Entry<Double, Integer> entry : counterValues.entrySet()) {
            probabilities[i++] = (double) entry.getValue() / listValues.length;
        }
        return probabilities;
    }

    public static double findEntropy(double[] arr) {
        double entropy = 0.0;
        for (double value : arr) {
            entropy -= value * Math.log(value) / Math.log(2);
        }
        return entropy;
    }

    public static double findRMSSD(double[] arr) {
        double ssd = 0;
        for (int x = 0; x < arr.length - 1; x++) {
            ssd = ssd + Math.pow((arr[x + 1] - arr[x]), 2);
        }
        return Math.sqrt((1.0 / (arr.length - 1)) * ssd);
    }

    public static double findPNN50(double[] arr) {
        int nn50 = 0;
        for (int x = 0; x < arr.length - 1; x++) {
            if (arr[x + 1] - arr[x] > 50) {
                nn50++;
            }
        }
        double pnn50 = (double) nn50 / (arr.length - 1) * 100;
        return pnn50;
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
        double[] finalFeature =  new double[GlobalConstants.addedFeature];

        for (int i = 0; i < GlobalConstants.rriLength; i++){
            feature[i] = ppInterval[i]/4; // sampling rate is 250 (250 sample per second), timestamp is in ms, so need to be divided by 4
        }

        Log.i(TAG, " feature length - " + feature.length);
        Log.i(TAG, " feature - " + Arrays.toString(feature));

        finalFeature[0] = Arrays.stream(feature).max().getAsDouble();
        Log.i(TAG, " Feature - " + Arrays.toString(finalFeature));

        finalFeature[1] = Arrays.stream(feature).min().getAsDouble();
        Log.i(TAG, " Feature - " + Arrays.toString(finalFeature));

        finalFeature[2] = Arrays.stream(feature).average().getAsDouble();
        Log.i(TAG, " Feature - " + Arrays.toString(finalFeature));

        StandardDeviation standarddev = new StandardDeviation();
        double std = standarddev.evaluate(feature);

        finalFeature[3] = std;
        Log.i(TAG, " Feature - " + Arrays.toString(finalFeature));

        finalFeature[4] = findMedian(feature);//median
        Log.i(TAG, " Feature - " + Arrays.toString(finalFeature));

        finalFeature[5] = findMode(feature);//modus
        Log.i(TAG, " Feature - " + Arrays.toString(finalFeature));

        finalFeature[6] = findVariance(feature);//varian
        Log.i(TAG, " Feature - " + Arrays.toString(finalFeature));

        finalFeature[7] = finalFeature[0]-finalFeature[1];//range
        Log.i(TAG, " Feature - " + Arrays.toString(finalFeature));

        Skewness skewness = new Skewness();
        double s = skewness.evaluate(feature);

        finalFeature[8] = s;//skew
        Log.i(TAG, " Feature - " + Arrays.toString(finalFeature));

        Kurtosis kurtosis = new Kurtosis();
        double k = kurtosis.evaluate(feature);

        finalFeature[9] = k;//kurt
        Log.i(TAG, " Feature - " + Arrays.toString(finalFeature));

        GeometricMean gmean = new GeometricMean();
        double rms = gmean.evaluate(feature);
        finalFeature[10] = rms;//rms
        Log.i(TAG, " Feature - " + Arrays.toString(finalFeature));

        double[] prob = calculateProbabilities(feature);
        double entropy = findEntropy(prob);

        finalFeature[11] = entropy;//entropy
        Log.i(TAG, " Feature - " + Arrays.toString(finalFeature));

        finalFeature[12] = findRMSSD(feature);//rmssd
        Log.i(TAG, " Feature - " + Arrays.toString(finalFeature));

        finalFeature[13] = (60*1000)/finalFeature[2];//mhr
        Log.i(TAG, " Feature - " + Arrays.toString(finalFeature));

        finalFeature[14] = findPNN50(feature);//pnn50
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

