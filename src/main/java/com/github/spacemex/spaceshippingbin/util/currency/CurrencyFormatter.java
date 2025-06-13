package com.github.spacemex.spaceshippingbin.util.currency;

public class CurrencyFormatter {
    private static final String[] SUFFIXES = {
            "", "K", "M", "B", "T", "Q", "Qn", "Sx", "Sp", "Oc", "No", "Dc" // up to Decillion (10^33)
    };

    public static String format(double value) {
        if (value < 1000) {
            return String.valueOf((int) value);
        }

        int magnitude = 0;
        while (value >= 1000 && magnitude < SUFFIXES.length - 1) {
            value /= 1000;
            magnitude++;
        }

        long intPart = (long) value;
        long decimal = (long) ((value - intPart) * 10);

        return decimal == 0
                ? intPart + SUFFIXES[magnitude]
                : intPart + "." + decimal + SUFFIXES[magnitude];
    }
}
