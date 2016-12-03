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
    public void setImageUri(Uri uri) {
        if(_imageUri != null && _imageUri.equals(uri)) {
            return;
        }

        _imageUri = uri;
        _displayBitmap = null; // not sure if I dare to recycle this..
                               // if it's used somewhere, then app crashes probably. and recycle isn't actually required, it will be gc'd.
    }

    public Bitmap getThumbnail() {
        if(_imageUri == null) {
            return null; // TODO: default image
        }
        else if(_displayBitmap != null) {
            return _displayBitmap;
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

    public String getFilename() {
        if(_imageUri == null) {
            return null;
        }

        String uriPath = UriHelper.getPath(BloggerApplication.getAppContext(), _imageUri);
        String fileName = PathHelper.getLastComponentOfPath(uriPath);

        return fileName;
    }
}
