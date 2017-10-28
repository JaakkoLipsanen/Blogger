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
        private String _imageText;
        public ImageEntry(Image image) {
            this(image, "");
        }
        public ImageEntry(Image image, String imageText) {
            _image = image;
            _imageText = imageText;
        }

        @Override
        public void write(OutputStreamWriter writer) throws IOException {
            writer.write("image: ");
            if(_image != null) {
                _image.write(writer);
                writer.write("|" + _imageText);
            }
        }

        public Image getImage() { return _image; }

        public String getImageText() {
            return _imageText;
        }
        public void setImageText(String value) { _imageText = value; }
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

        public String getHeaderText() { return _headerText; }
        public void setHeaderText(String value) { _headerText = value; }
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

        public String getText() { return _text; }
        public void setText(String value) { _text = value; }
    }

    public static class ImageGroupEntry extends BlogEntry {
        private Image[] _images;
        public ImageGroupEntry(Image[] images) {
            _images = images;
        }

        @Override
        public void write(OutputStreamWriter writer) throws IOException {
            writer.write("image-group: ");
            for(Image image : _images) {
                image.write(writer);
                writer.write(" ");
            }
        }

        public Image[] getImages() { return _images; }
    }
}
