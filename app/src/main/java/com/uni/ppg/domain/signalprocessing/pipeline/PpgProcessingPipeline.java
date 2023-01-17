package com.uni.ppg.domain.signalprocessing.pipeline;

import com.uni.ppg.domain.signalprocessing.steps.Derivation;
import com.uni.ppg.domain.signalprocessing.steps.MaximaCalculator;
import com.uni.ppg.domain.signalprocessing.steps.Preprocessor;
import com.uni.ppg.domain.signalprocessing.steps.ResultValidator;
import com.uni.ppg.domain.signalprocessing.steps.RollingAverage;
import com.uni.ppg.domain.signalprocessing.steps.filter.GaussianBlur;
import com.uni.ppg.domain.signalprocessing.steps.filter.LowPassFilter;

public class PpgProcessingPipeline {

    public static Pipeline pipeline() {
        return new Pipeline(new Preprocessor())
                // get max value from each batch of frame, subtract all value by the Max value
                // return int[] with length of 100 (same as batch size)
                .pipe(new RollingAverage())
                // rolling average every 10 element
                // return int[] with length of 91  (9 missing because rolling average)
                .pipe(new LowPassFilter(30))
                // low pass filter
                // return 91
                .pipe(new GaussianBlur())
                // gaussian blur
                // return 85
                .pipe(new Derivation())
                // return 85
                .pipe(new MaximaCalculator())
                .pipe(new ResultValidator());
    }
}
