package flai.blogger;

import android.net.Uri;

import java.io.IOException;
import java.io.OutputStreamWriter;

import flai.blogger.helpers.PathHelper;
import flai.blogger.helpers.UriHelper;

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

        public Image() { }
        public Image(Uri u) { uri = u; }
        public Image(Uri u, String t) { uri = u; text = t; }

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

            String sourcePath = UriHelper.getPath(BloggerApplication.getAppContext(), uri);
            String fileName = PathHelper.getLastComponentOfPath(sourcePath);

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

        public Text() { }
        public Text(String t) { text = t; }
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
        public Header() { }
        public Header(String h) { header = h; }

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

