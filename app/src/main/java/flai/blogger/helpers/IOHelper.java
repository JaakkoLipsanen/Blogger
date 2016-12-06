package flai.blogger.helpers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

    // http://stackoverflow.com/a/10864346
    public static boolean splitFile(File file, String outputFolder, int fileMaxSize) {
        if(!file.exists() || file.length() <= fileMaxSize) { // todo: if file size is smaller than parameter, it should still probably be moved to outputFolder...?
            return false;
        }

        final int totalFileCount = (int)Math.ceil(file.length() / (double)fileMaxSize); // todo: hmm is this correct? should be, but could there be some overheads or something?

        int partNumber= 1;
        byte[] buffer = new byte[fileMaxSize];

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {//try-with-resources to ensure closing stream
            String name = file.getName();

            int bytesRead;
            while ((bytesRead = bis.read(buffer)) > 0) {
                //write each chunk of data into separate file with different number in name
                File newFile = new File(outputFolder, name + "." + partNumber + "o" + totalFileCount);
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    out.write(buffer, 0, bytesRead);
                }

                partNumber++;
            }
        } catch (IOException e) {
            DialogHelper.showErrorToast("IOHelper.splitFile failed: ", e);
            return false;
        }

        return true;
    }
}
