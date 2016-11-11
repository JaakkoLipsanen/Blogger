package flai.blogger.model;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by Jaakko on 11.11.2016.
 */
public abstract class BlogEntry {
    public abstract void write(OutputStreamWriter writer) throws IOException;

    public static class ImageEntry extends BlogEntry {
        private Image _image;
        public ImageEntry(Image image) {
            _image = image;
        }

        @Override
        public void write(OutputStreamWriter writer) throws IOException {
            writer.write("image: ");
            if(_image != null) {
                _image.write(writer);
            }
        }

        public Image getImage() {
            return _image;
        }
    }

    public static class HeaderEntry extends BlogEntry {
        private String _headerText;
        public HeaderEntry(String headerText) {
            _headerText = headerText;
        }


        @Override
        public void write(OutputStreamWriter writer) throws IOException {
            writer.write("header: " + _headerText);
        }

        public String getHeaderText() {
            return _headerText;
        }
    }

    public static class TextEntry extends BlogEntry {
        private String _text;
        public TextEntry(String text) {
            _text = text;
        }

        @Override
        public void write(OutputStreamWriter writer) throws IOException {
            writer.write("text: " + _text);
        }

        public String getText() {
            return _text;
        }
    }
}
