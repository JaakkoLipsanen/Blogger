package flai.blogger.helpers;

/**
 * Created by Jaakko on 25.11.2016.
 */
public class MiscHelper {
    public static Integer parseIntegerOrNull(String str) {
        try {
            return Integer.parseInt(str);
        }
        catch (Exception e) {
            return null;
        }
    }
}
