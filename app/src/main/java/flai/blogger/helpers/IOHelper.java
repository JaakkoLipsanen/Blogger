package flai.blogger.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import flai.blogger.BloggerApplication;

/**
 * Created by Jaakko on 11.11.2016.
 */
public class IOHelper {
    // deletes folder and all sub folders and files
    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    public static Bitmap loadImage(Uri uri, int width, int height) {
        try {
            InputStream inputStream = BloggerApplication.getAppContext().getContentResolver().openInputStream(uri);
            return Bitmap.createScaledBitmap(BitmapFactory.decodeStream(inputStream), width, height, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
