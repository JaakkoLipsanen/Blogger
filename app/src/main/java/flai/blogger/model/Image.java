package flai.blogger.model;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import java.io.File;
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
    private Bitmap _thumbnailBitmap;
    private Size _resolution; // not actual res necessarily, but aspect ratio has to be right

    public Image(Uri uri, Size resolution) {
        _imageUri = uri;
        _resolution = resolution;
    }

    public Uri getImageUri() {
        return _imageUri;
    }
    public void setImageUri(Uri uri) {
        if(_imageUri != null && _imageUri.equals(uri)) {
            return;
        }

        _imageUri = uri;
        _thumbnailBitmap = null; // not sure if I dare to recycle this..
                                 // if it's used somewhere, then app crashes probably. and recycle isn't actually required, it will be gc'd.

        _resolution = null;
    }

    public Bitmap getThumbnail() {
        if(_imageUri == null) {
            return null; // TODO: default image
        }
        else if(_thumbnailBitmap != null) {
            return _thumbnailBitmap;
        }

        final int DisplayImageSize = 172;
        _thumbnailBitmap = BitmapHelper.loadFromStorageCacheOrCreateBitmap(_imageUri, PathHelper.ThumbnailCacheFolder, DisplayImageSize, ImageQuality.LowDef, true);

    //  if(_resolution == null) {
    //      _resolution = new Size(_thumbnailBitmap.getWidth(), _thumbnailBitmap.getHeight());
    //  }

        return _thumbnailBitmap;
    }

    public void write(OutputStreamWriter writer) {
        if (_imageUri == null) {
            return;
        }

        if(_resolution == null) {
            _resolution = BitmapHelper.getResolution(_imageUri); // this might, possibly return null. not likely though
        }

        try {
            writer.write(this.getFilename());
            if(_resolution != null) { // if it IS null, then uhh... what?
                writer.write("?" + _resolution.Width + "x" + _resolution.Height);
            }
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

    public static Image parse(String parentFolder, String str) {
        String fileName = str;
        Size resolution = null;

        if(str.contains("?")) {
            String[] p = str.split("\\?");
            fileName = p[0];
            if(p.length > 1) {
                resolution = Size.tryParse(p[1]);
            }
        }

        return new Image(Uri.fromFile(new File(parentFolder + "/" + fileName)), resolution);
    }
}
