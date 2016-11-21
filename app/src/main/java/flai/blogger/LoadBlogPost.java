package flai.blogger;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import flai.blogger.helpers.PathHelper;
import flai.blogger.model.BlogEntry;
import flai.blogger.model.BlogPost;
import flai.blogger.model.DayRange;
import flai.blogger.model.Image;

/**
 * Created by Jaakko on 11.11.2016.
 */
public class LoadBlogPost {
    public static BlogPost loadBlogPost(Uri uri) {
        try {
            InputStream inputStream = new BufferedInputStream(BloggerApplication.getAppContext().getContentResolver().openInputStream(uri));
            ZipInputStream zipInput = new ZipInputStream(inputStream);
            String postsFileContent = "";

            ZipEntry entry = null;
            ArrayList<Byte> byteCollector = new ArrayList<>();
            while((entry = zipInput.getNextEntry()) != null) { /* LOAD ALL FILES IN THE .ZIP */
                if(entry.getName().equals("/post.txt")) {
                    final int BUFFER = 8192;
                    byte bytes[] = new byte[BUFFER];

                    int count = 0;
                    while ((count = zipInput.read(bytes)) != -1) {
                        for (int i = 0; i < count; i++) {
                            byteCollector.add(bytes[i]);
                        }
                    }

                    byte[] bytes2 = new byte[byteCollector.size()];
                    for (int i = 0; i < byteCollector.size(); i++) {
                        bytes2[i] = byteCollector.get(i).byteValue();
                    }

                    postsFileContent = new String(Arrays.copyOfRange(bytes2, 0, bytes2.length), "UTF-8");

                }
                else if(entry.getName().startsWith("/orig/")) { // aka image!!

                    new File(PathHelper.ImageFolderName).mkdirs();
                    final File imageFile = new File(PathHelper.ImageFolderName + "/" + PathHelper.getLastComponentOfPath(entry.getName())); /* SAVE IMAGES TO /flai/images_all */
                    imageFile.delete(); // make sure deleted
                    imageFile.createNewFile();

                    FileOutputStream outputStream = new FileOutputStream(imageFile);


                    final int BUFFER = 2048;
                    byte byteData[] = new byte[BUFFER];
                    int count = 0;
                    while ((count = zipInput.read(byteData)) != -1)
                    {
                        outputStream.write(byteData, 0, count);
                    }
                    outputStream.close();
                    zipInput.closeEntry();
                }
            }
            zipInput.close();

            BlogPost post = LoadBlogPost.parsePostsFile(postsFileContent);
            return post;

        } catch (Exception e) {
            Log.e("blogger", Log.getStackTraceString(e));
        }

        return null;
    }

    /* PARSES THE post.txt from string (that contains the content of posts.txt */
    private static BlogPost parsePostsFile(String postsFileContent) throws IOException {

        String[] lines = postsFileContent.split(System.getProperty("line.separator"));

        String title = lines[0].split(":")[1].trim();
        String trip = lines[1].split(":")[1].trim();
        String dateRange = lines[2].split(":")[1].trim();
        String mainImage = lines[3].split(":")[1].trim();

        final File imageFolder = new File(PathHelper.ImageFolderName);
        imageFolder.mkdir(); // make sure exists

        ArrayList<BlogEntry> entries = new ArrayList<>();
        for(int i = 4; i < lines.length; i++) { // load all other info
            String line = lines[i].trim();
            if(line.isEmpty()) {
                continue;
            }

            String tag = line.split(":")[0].trim();
            String content = line.split(":", 2)[1].trim();

            if(tag.equals("text")) {
                entries.add(new BlogEntry.TextEntry(content));
            }
            else if(tag.equals("header")) {
                entries.add(new BlogEntry.HeaderEntry(content));
            }
            else if(tag.equals("image")) {
                String[] parts = content.split("\\|");
                String fileName = parts[0];
                String imageText = (parts.length > 1) ? parts[1] :  "";

                entries.add(new BlogEntry.ImageEntry(new Image(
                        Uri.fromFile(new File(PathHelper.ImageFolderName + "/" + fileName))),
                        imageText));
            }
        }

        Uri mainImageUri =  Uri.fromFile(new File(PathHelper.ImageFolderName + "/" + mainImage));
        return new BlogPost(title, DayRange.parse(dateRange), entries, new Image(mainImageUri));
    }
}