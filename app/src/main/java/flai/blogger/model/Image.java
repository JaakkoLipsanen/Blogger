package flai.blogger.model;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;

import flai.blogger.BloggerApplication;

/**
 * Created by Jaakko on 11.11.2016.
 */
public class Image {
    private Uri _imageUri;
    public Image(Uri uri) {
        _imageUri = uri;
    }

    public Uri getImageUri() {
        return _imageUri;
    }

    public void write(OutputStreamWriter writer) {

        if (_imageUri == null) {
            return;
        }

        String fileName = _imageUri.getLastPathSegment();
        if (_imageUri.getPath().startsWith("content") || _imageUri.getPath().startsWith("/external")) { // if the uri is content:// or /external path (this happens always when image is 'picked' with gallery)
            final String[] proj = {MediaStore.Images.Media.DATA};

            Cursor c = BloggerApplication.getAppContext().getContentResolver().query(_imageUri, proj, null, null, null);
            int column_index = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            c.moveToFirst();

            String sourcePath = c.getString(column_index);
            fileName = sourcePath.substring(sourcePath.lastIndexOf("/") + 1);
        }

        try {
            writer.write(fileName);
        } catch (IOException e) {
            Log.e("blogger", "Exception in Image.write", e);
        }
    }
}
