package flai.blogger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.ZipOutputStream;

import flai.blogger.helpers.IOHelper;
import flai.blogger.helpers.PathHelper;
import flai.blogger.helpers.UriHelper;
import flai.blogger.helpers.ZipHelper;
import flai.blogger.model.BlogEntry;
import flai.blogger.model.BlogPost;

/**
 * Created by Jaakko on 11.11.2016.
 */
public class SaveBlogPost {
    public static void saveBlogPost(BlogPost blogPost) {

        /* FIRST, MAKE SURE ALL FOLDERS ARE CREATED */
        final File flaiFolder = new File(PathHelper.FlaiFolderName);
        flaiFolder.mkdir(); // make sure this exists

        final File imageFolder = new File(PathHelper.ImageFolderName);
        imageFolder.mkdir();

        final File tempFolder = new File(PathHelper.TempFolderName);

        IOHelper.deleteRecursive(tempFolder);
        tempFolder.mkdir();

        File photoFolder = new File(PathHelper.TempFolderName + "/orig");
        photoFolder.mkdir();

        final File postFile = new File(PathHelper.TempFolderName, "post.txt");
        try {
            postFile.createNewFile();
        } catch (IOException e) {
            Log.e("blogger", "SaveBlogPost: creating post.txt file failed" +  Log.getStackTraceString(e));
            return;
        }

        /* THEN, WRITE THE POST.TXT */
        try(FileOutputStream fileStream = new FileOutputStream(postFile);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileStream)) {

            outputWriter.write("title: " + blogPost.getTitle() + System.getProperty("line.separator"));
            outputWriter.write("trip: USA 2016" + System.getProperty("line.separator"));
            outputWriter.write("date-range: " + blogPost.getDayRange().toString() + System.getProperty("line.separator"));

            outputWriter.write("main-image: ");
            blogPost.getMainImage().write(outputWriter);

            outputWriter.write(System.getProperty("line.separator"));
            outputWriter.write(System.getProperty("line.separator"));

            for(BlogEntry entry : blogPost.entries()) {
                entry.write(outputWriter);
                outputWriter.write(System.getProperty("line.separator"));
            }

        } catch (IOException e) {
            Log.e("blogger", "SaveBlogPost: writing post.txt file failed: " + Log.getStackTraceString(e));
            return;
        }

        /* THEN, SAVE ALL IMAGES TO /ORIG */
        for (Uri uri : blogPost.getAllImageUris()) {

            String sourcePath = UriHelper.getPath(BloggerApplication.getAppContext(), uri);
            String fileName = sourcePath.substring(sourcePath.lastIndexOf("/") + 1);

            File destination = new File(photoFolder.getPath() + "/" + fileName);
            destination.getParentFile().mkdirs(); /* TODO: Okei, tää oli siin antin muokkaamssa commitissa. En tiiä miksi, mut jos jotai häikkää nii kato tätä */

            try(OutputStream out = new FileOutputStream(destination, false)) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                Bitmap bitmap = BitmapFactory.decodeFile(sourcePath, options);  /* IF I WANT TO RESIZE THE IMAGES A BIT SMALLER, USE THIS:  BITMAP_RESIZER(BitmapFactory.decodeFile(sourcePath, options), 1920); */
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                bitmap.recycle();

            } catch (Exception e) {
                Log.e("blogger", "SaveBlogPost: saving image (" + fileName + ") failed: " + Log.getStackTraceString(e));
                return;
            }
        }

        /* FINALLY, SAVE TO .ZIP */
        // take the title, replace all spaces with hyphens and remove all special characters
        final String fileName = blogPost.getTitle().replace(" ", "-").replaceAll("/[^A-Za-z0-9 ]/", "");

        final File sourceFolder = tempFolder;
        final String toLocation = PathHelper.FlaiFolderName + "/" + fileName + ".zip";

        try(FileOutputStream dest = new FileOutputStream(toLocation);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest))) {

            ZipHelper.zipFolderRecursively(out, sourceFolder, sourceFolder.getPath().length());
        } catch (Exception e) {
            Log.e("blogger", "SaveBlogPost: creating zip file failed: " + Log.getStackTraceString(e));
            return;
        }
    }
}
