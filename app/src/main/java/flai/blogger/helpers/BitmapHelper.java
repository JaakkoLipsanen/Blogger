package flai.blogger.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.InputStream;

import flai.blogger.BloggerApplication;

/**
 * Created by Jaakko on 25.11.2016.
 */
public class BitmapHelper {
    public static Bitmap decodeBitmapScaled(Uri uri, int requestedWidth) {
        // first load just the image info, not actual image itself to get width/height
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        try(InputStream stream = BloggerApplication.getAppContext().getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(stream, null, options);
        } catch (Exception e) {
            Log.e("blogger", "BitmapHelper.decodeBitmapScaled: initial image load failed");
            return null; // TODO: return default image
        }

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSizeFromRequestedWidth(options, requestedWidth, requestedWidth);
        options.inJustDecodeBounds = false;

        try(InputStream stream = BloggerApplication.getAppContext().getContentResolver().openInputStream(uri)) {
            return BitmapFactory.decodeStream(stream, null, options);
        } catch (Exception e) {
            Log.e("blogger", "BitmapHelper.decodeBitmapScaled: final image load failed");
            return null; // TODO: return default image
        }
    }

    private static int calculateInSampleSizeFromRequestedWidth(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int imageWidth = options.outWidth;
        final int imageHeight = options.outHeight;
        int currentInSampleSize = 1;

        if (imageHeight > reqHeight || imageWidth > reqWidth) {

            final int halfWidth = imageWidth / 2;
            final int halfHeight = imageHeight / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / currentInSampleSize) >= reqHeight && (halfWidth / currentInSampleSize) >= reqWidth) {
                currentInSampleSize *= 2;
            }
        }

        return currentInSampleSize;
    }
}
