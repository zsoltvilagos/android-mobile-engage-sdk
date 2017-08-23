package com.emarsys.mobileengage.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageUtils {

    public static Bitmap loadBitmapFromUrl(String imageUrl) {
        Bitmap result = null;
        if (imageUrl != null) {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                result = BitmapFactory.decodeStream(input);
            } catch (IOException ignored) {
            }
        }
        return result;
    }

}
