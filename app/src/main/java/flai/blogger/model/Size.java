package flai.blogger.model;

import flai.blogger.helpers.MiscHelper;

/**
 * Created by Jaakko on 06.12.2016.
 */
public class Size {
    public final int Width;
    public final int Height;

    public Size(int width, int height) {
        this.Width = width;
        this.Height = height;
    }

    public static Size tryParse(String str) {
        String[] parts = str.split("x");
        if(parts.length != 2) {
            return null;
        }

        Integer width = MiscHelper.parseIntegerOrNull(parts[0]);
        Integer height = MiscHelper.parseIntegerOrNull(parts[1]);

        if(width == null || height == null) {
            return null;
        }

        return new Size(width, height);
    }
}
