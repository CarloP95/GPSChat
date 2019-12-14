package it.carlo.pellegrino.gpschat.imageUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.model.Marker;
//import com.nostra13.universalimageloader.core.ImageLoader;
//import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Singleton class used for displaying avatars on Map
 * Use the method getInstance with the GoogleMap instance as argument
 *      to create an instance and use the method setIconOnMarker.
 */
public class ImageUtils {

    private static final int size = 150;

    public static Bitmap getBitmap(final String url) {
        InputStream buffer = null;
        try {
            buffer = new URL(url).openConnection().getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bmp = BitmapFactory.decodeStream(buffer);
        bmp.setHasAlpha(true);
        return scaleKeepingProportions(bmp, 0, size);
    }


    private static Bitmap scaleKeepingProportions (Bitmap bmp, int height, int width) {

        if (height != 0 && width != 0) {
            Log.e("GPSCHAT", "You are using this method wrong. You can't hold the proportion on both height and width. The program will assume Height the preferred one.");
            width = 0;
        }

        int currentWidth = bmp.getWidth(),
                currentHeight = bmp.getHeight();
        float aspectRatio = currentWidth / (float) currentHeight;

        boolean keepWidth = width != 0;

        int newHeight = keepWidth ? Math.round(width / aspectRatio) : height,
                newWidth  = keepWidth ? width : Math.round(height * aspectRatio);

        return Bitmap.createScaledBitmap(bmp, newWidth, newHeight, false);

    }
}
