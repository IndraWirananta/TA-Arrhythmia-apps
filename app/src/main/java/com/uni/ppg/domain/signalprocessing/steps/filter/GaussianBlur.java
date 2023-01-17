package com.uni.ppg.domain.signalprocessing.steps.filter;

import android.util.Log;

import com.uni.ppg.domain.signalprocessing.steps.Step;

import java.util.Arrays;

public class GaussianBlur implements Step {

    private static final String TAG = GaussianBlur.class.getName();

    private final float[] coefficients = new float[]{0.004f, 0.054f, 0.242f, 0.401f, 0.242f, 0.054f, 0.004f};//impulse response, for gaussian blur the impulse response is a gaussian function
    private final int padding;

    public GaussianBlur() {
        this.padding = (coefficients.length - 1) / 2;
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
       longInfo("signalProcessing : Applying Gaussian blur" + signal.length + " result : "+ Arrays.toString(filter(signal)));
        return filter(signal);
    }

    private int[] filter(int[] signal) {
        int[] filtered = new int[signal.length - 2 * padding];
        for (int i = padding; i < filtered.length + padding; i++) {
            float value = 0;
            for (int j = 0; j < 7; j++) {
                value += (float) signal[i - padding + j] * coefficients[j];
            }
            filtered[i - padding] = (int) value;
        }
        return filtered;
    }
}
