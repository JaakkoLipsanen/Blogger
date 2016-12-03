package flai.blogger.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import flai.blogger.BloggerApplication;
import flai.blogger.model.ImageQuality;

/**
 * Created by Jaakko on 25.11.2016.
 */
public class BitmapHelper {

    public static File loadFromStorageCacheOrCreateFile(Uri originalUri, String cacheFolder, int requestedMinDimension, ImageQuality imageQuality, boolean rotateBeforeCaching) {
        CachedBitmap cachedBitmap = loadImageFromCache(originalUri, cacheFolder, requestedMinDimension, imageQuality, rotateBeforeCaching);
        if(cachedBitmap.Bitmap != null) {
            cachedBitmap.Bitmap.recycle();
        }

        return cachedBitmap.File;
    }

    public static Bitmap loadFromStorageCacheOrCreateBitmap(Uri originalUri, String cacheFolder, int requestedMinDimension, ImageQuality imageQuality, boolean rotateBeforeCaching) {
        CachedBitmap cachedBitmap = loadImageFromCache(originalUri, cacheFolder, requestedMinDimension, imageQuality, rotateBeforeCaching);
        if(cachedBitmap.Bitmap == null && cachedBitmap.File != null) {
            return BitmapFactory.decodeFile(cachedBitmap.File.getPath());
        }

        return cachedBitmap.Bitmap;
    }

    // requestedMinDimension: give "1080", then loads correctly both portrait and landscape images
    public static Bitmap decodeBitmapScaledApproximately(Uri uri, int requestedMinDimension, ImageQuality imageQuality) {
        // first load just the image info, not actual image itself to get width/height
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = (imageQuality == ImageQuality.Original) ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        if(imageQuality == ImageQuality.LowDef) {
            options.inDither = true;
        }

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

    private static class CachedBitmap {
        public final Bitmap Bitmap;
        public final File File;

        public CachedBitmap(Bitmap bitmap, File file) {
            this.Bitmap = bitmap;
            this.File = file;
        }
    }

    private static CachedBitmap loadImageFromCache(Uri originalUri, String cacheFolder, int requestedMinDimension, ImageQuality imageQuality, boolean rotateBeforeCaching) {
        String originalPath = UriHelper.getPath(BloggerApplication.getAppContext(), originalUri);
        String fileName = PathHelper.getLastComponentOfPath(originalPath);

        File destinationCacheFile = new File(cacheFolder + "/" + fileName);
        if(destinationCacheFile.exists() && destinationCacheFile.length() > 0) { // exists and is not empty
            return new CachedBitmap(null, destinationCacheFile);
        }

        // make sure that the directory containing the file exists
        destinationCacheFile.getParentFile().mkdirs();
        destinationCacheFile.delete();

        ExifInterface sourceExif = BitmapHelper.getExifData(originalPath);
        Bitmap bitmap = BitmapHelper.decodeBitmapScaledApproximately(originalUri, requestedMinDimension, imageQuality);
        if(rotateBeforeCaching && sourceExif != null) {
            bitmap = BitmapHelper.applyOrientation(bitmap, sourceExif);
        }

        try(OutputStream out = new FileOutputStream(destinationCacheFile, false)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            BitmapHelper.copyExif(sourceExif, destinationCacheFile.getPath());
        }
        catch(Exception e) {
            DialogHelper.showErrorToast("BitmapHelper.loadImageFromCache failed", e);
            destinationCacheFile = null;
        }

        return new CachedBitmap(bitmap, destinationCacheFile);
    }

    public static void copyExif(ExifInterface from, String destinationImagePath) throws IOException
    {
        final String[] attributes = new String[]
        {
                ExifInterface.TAG_APERTURE,
                ExifInterface.TAG_DATETIME,
                ExifInterface.TAG_DATETIME_DIGITIZED,
                ExifInterface.TAG_EXPOSURE_TIME,
                ExifInterface.TAG_FLASH,
                ExifInterface.TAG_FOCAL_LENGTH,
                ExifInterface.TAG_GPS_ALTITUDE,
                ExifInterface.TAG_GPS_ALTITUDE_REF,
                ExifInterface.TAG_GPS_DATESTAMP,
                ExifInterface.TAG_GPS_LATITUDE,
                ExifInterface.TAG_GPS_LATITUDE_REF,
                ExifInterface.TAG_GPS_LONGITUDE,
                ExifInterface.TAG_GPS_LONGITUDE_REF,
                ExifInterface.TAG_GPS_PROCESSING_METHOD,
                ExifInterface.TAG_GPS_TIMESTAMP,
                ExifInterface.TAG_IMAGE_LENGTH,
                ExifInterface.TAG_IMAGE_WIDTH,
                ExifInterface.TAG_ISO,
                ExifInterface.TAG_MAKE,
                ExifInterface.TAG_MODEL,
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.TAG_SUBSEC_TIME,
                ExifInterface.TAG_SUBSEC_TIME_DIG,
                ExifInterface.TAG_SUBSEC_TIME_ORIG,
                ExifInterface.TAG_WHITE_BALANCE,
        };

        ExifInterface newExif = new ExifInterface(destinationImagePath);
        for (int i = 0; i < attributes.length; i++)
        {
            String value = from.getAttribute(attributes[i]);
            if (value != null)
                newExif.setAttribute(attributes[i], value);
        }
        newExif.saveAttributes();
    }

    private static Bitmap applyOrientation(Bitmap bitmap, ExifInterface exif) {
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

        int rotateAmount = 0;
        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotateAmount = 90;
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotateAmount = 180;
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotateAmount = 270;
                break;
        }

        if(rotateAmount == 0) {
            return bitmap;
        }

        Matrix rotationMatrix = new Matrix();
        rotationMatrix.postRotate(rotateAmount);

        Bitmap rotated = Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotationMatrix, true);
        bitmap.recycle();

        exif.setAttribute(ExifInterface.TAG_ORIENTATION, "1"); // remove the orientation attribute from exif
        return rotated;
    }

    private static ExifInterface getExifData(String imagePath) {
        try {
            return new ExifInterface(imagePath);
        } catch (IOException e) {
            return null;
        }
    }
}
