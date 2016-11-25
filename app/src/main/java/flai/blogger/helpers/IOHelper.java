package flai.blogger.helpers;

import java.io.File;

/**
 * Created by Jaakko on 11.11.2016.
 */
public class IOHelper {
    // deletes folder and all sub folders and files
    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }
}
