package flai.blogger;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;
import java.io.OutputStreamWriter;

// either Text, Image, Header or ImageGroup
public abstract class EntryType {

    public static int TYPE_TEXT = 0;
    public static int TYPE_HEADER= 1;
    public static int TYPE_IMAGE = 2;
    public static int TYPE_IMAGE_GROUP = 3;

    public abstract int getEntryType(); // i don't think this is even used to anything wise
    public abstract void write(OutputStreamWriter writer) throws IOException; // write is called when blog entry is saved to file

    public static class Image extends EntryType {
        public String text = "";
        public Uri uri; // the uri/path of the image

        @Override
        public int getEntryType() {
            return TYPE_IMAGE;
        }

        @Override
        public void write(OutputStreamWriter writer) throws IOException {

            if (uri == null) {
                // gg. not sure what to do here. i guess i just ignore
                writer.write("image:");
                return;
            }

            String sourcePath = "";
            String fileName = uri.getLastPathSegment();
            if (uri.getPath().startsWith("content") || uri.getPath().startsWith("/external")) { // if the uri is content:// or /external path (this happens always when image is 'picked' with gallery)
                final String[] proj = {MediaStore.Images.Media.DATA};

                Cursor c = MyApplication.getAppContext().getContentResolver().query(uri, proj, null, null, null);
                int column_index = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                c.moveToFirst();

                sourcePath = c.getString(column_index);
                fileName = sourcePath.substring(sourcePath.lastIndexOf("/") + 1);
            }

            writer.write("image: " + fileName + "|" + text);
        }
    }

    public static class ImageGroup extends EntryType {
        @Override
        public int getEntryType() {
            return TYPE_IMAGE_GROUP;
        }

        @Override
        public void write(OutputStreamWriter writer) {
            // not supported
        }
    }

    public static class Text extends EntryType {
        public String text = "";
        @Override
        public int getEntryType() {
            return TYPE_TEXT;
        }

        @Override
        public void write(OutputStreamWriter writer) throws IOException {
            writer.write("text: " + this.text);
        }
    }

    public static class Header extends EntryType {
        public String header = "";
        @Override
        public int getEntryType() {
            return TYPE_HEADER;
        }

        @Override
        public void write(OutputStreamWriter writer) throws IOException {
            writer.write("header: " + this.header);

        }
    }
}

