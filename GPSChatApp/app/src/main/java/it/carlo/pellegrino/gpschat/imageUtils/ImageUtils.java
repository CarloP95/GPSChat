package it.carlo.pellegrino.gpschat.imageUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageUtils {

    public static Bitmap getTransparentImage(String url) {
        Bitmap bmp = null;
        int size = 150;
        try {
            InputStream buffer = new URL(url).openConnection().getInputStream();
            bmp = BitmapFactory.decodeStream(buffer);
            bmp.setHasAlpha(true);
        }
        catch (MalformedURLException ex) {
            Log.v("GPSCHAT", "Error in decoding the URL");
        }
        catch (IOException ex) {
            Log.v("GPSCHAT", "Error in opening connection");
        }

        return Bitmap.createScaledBitmap(bmp, size, size, false);
    }
}
