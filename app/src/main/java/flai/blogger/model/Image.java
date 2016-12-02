package flai.blogger.model;

import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;

import flai.blogger.BloggerApplication;
import flai.blogger.helpers.BitmapHelper;
import flai.blogger.helpers.PathHelper;
import flai.blogger.helpers.UriHelper;

/**
 * Created by Jaakko on 11.11.2016.
 */
public class Image {
    private Uri _imageUri;
    private Bitmap _displayBitmap;

    public Image(Uri uri) {
        _imageUri = uri;
    }

    public Uri getImageUri() {
        return _imageUri;
    }
    public void setImageUri(Uri uri) { _imageUri = uri; }

    public Bitmap getThumbnail() {
        if(_displayBitmap != null) {
            return _displayBitmap;
        }
        else if(_imageUri == null) {
            return null; // TODO: default image
        }

        final int DisplayImageSize = 172;
        _displayBitmap = BitmapHelper.loadFromStorageCacheOrCreateBitmap(_imageUri, PathHelper.ThumbnailCacheFolder, DisplayImageSize, ImageQuality.LowDef, true);

        return _displayBitmap;
    }

    public void write(OutputStreamWriter writer) {
        if (_imageUri == null) {
            return;
        }

        String uriPath = UriHelper.getPath(BloggerApplication.getAppContext(), _imageUri);
        String fileName = PathHelper.getLastComponentOfPath(uriPath);

        try {
            writer.write(fileName);
        } catch (IOException e) {
            Log.e("blogger", "Exception in Image.write", e);
        }
    }

    public float getRotation() {
        int orientation = getOrientation();
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

    private ExifInterface getExif() throws IOException {
        return new ExifInterface(UriHelper.getPath(BloggerApplication.getAppContext(), _imageUri));
    }

    private int getOrientation() {
        try {
            return this.getExif().getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        } catch (IOException e) {
            return 1;
        }
    }

    public String getFilename() {
        String uriPath = UriHelper.getPath(BloggerApplication.getAppContext(), _imageUri);
        String fileName = PathHelper.getLastComponentOfPath(uriPath);

        return fileName;
    }
}
