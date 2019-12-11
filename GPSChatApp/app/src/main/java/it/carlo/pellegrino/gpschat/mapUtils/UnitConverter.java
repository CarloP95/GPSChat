package it.carlo.pellegrino.gpschat.mapUtils;

import android.util.Log;

public class UnitConverter {

    private static String km = "km";
    private static String m  = "m";

    private static double defaultReturnValue = 1000;

    public static double convertInMeters(String number, String unit) {
        try {
            double multiplicator = 1, convertedRadius = Double.parseDouble(number);

            multiplicator = unit.toLowerCase().equals(km) ? 1000 : 1;

            return convertedRadius * multiplicator;
        }
        catch(NumberFormatException ex) {
            Log.i("GPSCHAT", "Something thrown NumberFormatException. An empty string was passed: Number is : " + number + ". Unit is: " + unit + ". Returning default value : " + defaultReturnValue);
            return defaultReturnValue;
        }
    }
}
