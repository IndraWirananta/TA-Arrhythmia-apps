package com.uni.ppg.domain.signalprocessing.steps;

import android.util.Log;

import java.util.Arrays;

public class RollingAverage implements Step {

    private static final String TAG = RollingAverage.class.getName();

    private final int windowSize;

    public RollingAverage(int windowSize) {
        this.windowSize = windowSize;
    }

    public RollingAverage() {
        this.windowSize = 10;
    }

    public static void longInfo(String str) {
        if (str.length() > 4000) {
            Log.d(TAG, str.substring(0, 4000));
            longInfo(str.substring(4000));
        } else
            Log.d(TAG, str);
    }

    @Override
    public int[] invoke(int[] signal) {
       longInfo( "signalProcessing : Running rolling average subtraction - length : "+subtractAverage(signal).length+"-- result : "+Arrays.toString(subtractAverage(signal)));
        return subtractAverage(signal);
    }

    private int[] subtractAverage(int[] signal) {
        int[] remainingPoints = Arrays.stream(signal, windowSize - 1, signal.length).toArray();
        for (int i = 0; i <= signal.length - windowSize; i++) {
            double average = Arrays.stream(signal, i, i + windowSize).average().orElse(signal[i]);
            remainingPoints[i] = remainingPoints[i] - (int) average;
        }
        return remainingPoints;
    }
}
