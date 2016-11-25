package flai.blogger.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import flai.blogger.BloggerApplication;
import flai.blogger.helpers.BitmapHelper;
import flai.blogger.helpers.UriHelper;
import flai.blogger.helpers.PathHelper;

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

    public Bitmap getDisplayBitmap() {
        if(_displayBitmap != null) {
            return _displayBitmap;
        }
        else if(_imageUri == null) {
            return null; // TODO: default image
        }

        final int DisplayImageWidth = 256;
        _displayBitmap = BitmapHelper.decodeBitmapScaled(_imageUri, DisplayImageWidth);

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
}
