package com.uni.ppg.domain.signalprocessing.steps;

import android.util.Log;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * This signal processing step converts reflected light information to
 * absorption.
 */
public class Preprocessor implements Step {

    private static final String TAG = Preprocessor.class.getName();

    public static void longInfo(String str) {
        if (str.length() > 4000) {
            Log.d(TAG, str.substring(0, 4000));
            longInfo(str.substring(4000));
        } else
            Log.d(TAG, str);
    }

    @Override
    public int[] invoke(int[] signal) {
        int max = max(signal); // get max value from signal from the batch frame
        Log.i(TAG, "signalProcessing : Running preprocessor - Max : " + max );
        longInfo( "signalProcessing : Running preprocessor - Result : " + Arrays.toString(IntStream.of(signal).map(i -> max - i).toArray()));
        return IntStream.of(signal).map(i -> max - i).toArray();// subtract each element (i) of signal by Max
    }

    private int max(int[] signal) {
        return Arrays.stream(signal).max().orElseGet(() -> {
            Log.i(TAG, "Signal does not have a maximum, preprocessing will leave it intact");
            return 0;
        });
    }
}
