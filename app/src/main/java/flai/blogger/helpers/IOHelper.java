package flai.blogger.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

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

    public static boolean copyFile(File from, File to) {
        try (FileInputStream inStream = new FileInputStream(from);
             FileOutputStream outStream = new FileOutputStream(to)) {

            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);

            return true;
        }
        catch(Exception e) {
            return false;
        }
    }
}
