package flai.blogger.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import flai.blogger.BloggerApplication;

/**
 * Created by Jaakko on 25.11.2016.
 */
public class BitmapHelper {

    public static File loadFromStorageCacheOrCreateFile(Uri originalUri, String cacheFolder, int requestedMinDimension) {
        String originalPath = UriHelper.getPath(BloggerApplication.getAppContext(), originalUri);
        String fileName = originalPath.substring(originalPath.lastIndexOf("/") + 1);

        File destinationCacheFile = new File(cacheFolder + "/" + fileName);
        if(destinationCacheFile.exists() && destinationCacheFile.length() > 0) { // exists and is not empty
            return destinationCacheFile;
        }

        // make sure that the directory containing the file exists
        destinationCacheFile.getParentFile().mkdirs();
        destinationCacheFile.delete();

        Bitmap bitmap = BitmapHelper.decodeBitmapScaledApproximately(originalUri, requestedMinDimension);
        try(OutputStream out = new FileOutputStream(destinationCacheFile, false)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        }
        catch(Exception e) {
            DialogHelper.showErrorDialog("BitmapHelper.loadFromStorageCacheOrCreateFile failed", e);
            destinationCacheFile = null; // return null
        }

        bitmap.recycle();
        return destinationCacheFile;
    }

    public static Bitmap loadFromStorageCacheOrCreateBitmap(Uri originalUri, String cacheFolder, int requestedMinDimension) {
        String originalPath = UriHelper.getPath(BloggerApplication.getAppContext(), originalUri);
        String fileName = originalPath.substring(originalPath.lastIndexOf("/") + 1);

        File destinationCacheFile = new File(cacheFolder + "/" + fileName);
        if(destinationCacheFile.exists() && destinationCacheFile.length() > 0) {  // exists and is not empty
            return BitmapFactory.decodeFile(destinationCacheFile.getPath());
        }

        // make sure that the directory containing the file exists
        destinationCacheFile.getParentFile().mkdirs();

        Bitmap bitmap = BitmapHelper.decodeBitmapScaledApproximately(originalUri, requestedMinDimension);
        try(OutputStream out = new FileOutputStream(destinationCacheFile, false)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        }
        catch(Exception e) {
            DialogHelper.showErrorDialog("BitmapHelper.loadFromStorageCacheOrCreateBitmap failed", e);
        }

        return bitmap;
    }

    // requestedMinDimension: give "1080", then loads correctly both portrait and landscape images
    public static Bitmap decodeBitmapScaledApproximately(Uri uri, int requestedMinDimension) {
        // first load just the image info, not actual image itself to get width/height
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        try(InputStream stream = BloggerApplication.getAppContext().getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(stream, null, options);
        } catch (Exception e) {
            Log.e("blogger", "BitmapHelper.decodeBitmapScaled: initial image load failed");
            return null; // TODO: return default image
        }

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSizeFromRequesteMinDimension(options, requestedMinDimension);
        options.inJustDecodeBounds = false;

        try(InputStream stream = BloggerApplication.getAppContext().getContentResolver().openInputStream(uri)) {
            return BitmapFactory.decodeStream(stream, null, options);
        } catch (Exception e) {
            Log.e("blogger", "BitmapHelper.decodeBitmapScaled: final image load failed");
            return null; // TODO: return default image
        }
    }

    private static int calculateInSampleSizeFromRequesteMinDimension(BitmapFactory.Options options, int requestedMinDimension) {
        // Raw height and width of image
        final int imageWidth = options.outWidth;
        final int imageHeight = options.outHeight;
        int currentInSampleSize = 1;

        if (imageHeight > requestedMinDimension && imageWidth > requestedMinDimension) {

            final int halfWidth = imageWidth / 2;
            final int halfHeight = imageHeight / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / currentInSampleSize) >= requestedMinDimension && (halfWidth / currentInSampleSize) >= requestedMinDimension) {
                currentInSampleSize *= 2;
            }
        }

        return currentInSampleSize;
    }
}
