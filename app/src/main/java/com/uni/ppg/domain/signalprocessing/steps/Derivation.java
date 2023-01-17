package com.uni.ppg.domain.signalprocessing.steps;

import android.util.Log;

import java.util.Arrays;

/**
 * This signal processing step approximates the derivative of the
 * curve using a centered difference.
 */
public class Derivation implements Step {

    private static final String TAG = Derivation.class.getName();

    public static void longInfo(String str) {
        if (str.length() > 4000) {
            Log.d(TAG, str.substring(0, 4000));
            longInfo(str.substring(4000));
        } else
            Log.d(TAG, str);
    }

    @Override
    public int[] invoke(int[] signal) {
        int[] test = (centeredDifference(signal));
        longInfo("signalProcessing : Running differentiation" + signal.length + "result : " + Arrays.toString(test));
        return test;
    }

    private int[] centeredDifference(int[] signal) {
        int[] derivative = new int[signal.length - 2];
        for (int t = 0; t < signal.length - 2; t++) {
            derivative[t] = (signal[t + 2] - signal[t]) / 2;
        }
        return derivative;
    }
}
