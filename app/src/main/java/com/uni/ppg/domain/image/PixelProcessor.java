package com.uni.ppg.domain.image;

/**
 * This class is responsible for converting the YUV format byte array
 * of a camera frame, to the sum of red components for the whole image.
 */
public class PixelProcessor {

    private static final int TWO_18 = 262143;
    private static final int MASK_ALPHA = 0xff000000;
    private static final int MASK_RED = 0xff0000;
    private static final int MASK_GREEN = 0xff00;
    private static final int MASK_BLUE = 0xff;

//    "UVP" is a variable used in the provided Java code to represent the index of the current U or V chroma value in the "yuv" array. The "uvp" variable is used to iterate through the "yuv" array and extract the U and V values for each pixel in the image.
//
//In the provided code, "uvp" is defined as "frameSize + (h >> 1) * width". This means that "uvp" starts at an index equal to the total number of pixels in the image (which is "frameSize"), and it is incremented by 1 for every other pixel in the image (due to the "h >> 1" expression).

    public static int yuvToRedSum(byte[] yuv, int width, int height) {
        int frameSize = width * height;
        int sum = 0;

        for (int h = 0, yp = 0; h < height; h++) {
            int uvp = frameSize + (h >> 1) * width;
            int u = 0;
            int v = 0;

            for (int w = 0; w < width; w++, yp++) {
                //correct Y from [16..235] range
                int y = (MASK_BLUE & ((int) yuv[yp])) - 16;
                if (y < 0) {
                    y = 0;
                }

                if ((w & 1) == 0) {
                    v = (MASK_BLUE & yuv[uvp++]) - 128;
                    u = (MASK_BLUE & yuv[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) {
                    r = 0;
                } else if (r > TWO_18) {
                    r = TWO_18;
                }

                if (g < 0) {
                    g = 0;
                } else if (g > TWO_18) {
                    g = TWO_18;
                }

                if (b < 0) {
                    b = 0;
                } else if (b > TWO_18) {
                    b = TWO_18;
                }

                int pixel = MASK_ALPHA  |((r << 6) & MASK_RED) | ((g >> 2) & MASK_GREEN) | ((b >> 10) & MASK_BLUE);
                int red = (pixel >> 16) & MASK_BLUE;
                sum += red;
            }
        }
        return sum;
    }

}
