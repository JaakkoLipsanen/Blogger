package flai.blogger.helpers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Jaakko on 11.11.2016.
 */
public class ZipHelper {
    // recursive zipping method. zips the folder and sub folders and files
    public static void zipFolderRecursively(ZipOutputStream out, File folder, int basePathLength) throws IOException {
        final int BUFFER = 2048;

        File[] fileList = folder.listFiles();
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipFolderRecursively(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath.substring(basePathLength);

                ZipEntry entry = new ZipEntry(relativePath);
                out.putNextEntry(entry);

                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);

                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }
}
