package flai.blogger;

import android.net.Uri;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.ZipOutputStream;

import flai.blogger.helpers.BitmapHelper;
import flai.blogger.helpers.DialogHelper;
import flai.blogger.helpers.IOHelper;
import flai.blogger.helpers.PathHelper;
import flai.blogger.helpers.UriHelper;
import flai.blogger.helpers.ZipHelper;
import flai.blogger.model.BlogEntry;
import flai.blogger.model.BlogPost;
import flai.blogger.model.ImageQuality;

/**
 * Created by Jaakko on 11.11.2016.
 */
public class SaveBlogPost {
    private static final int FILE_MAX_SIZE_IN_BYTES = 20 * 1024 * 1024 - 2048; // 20MB - 2kb for buffer // todo: make this into a modifable setting
    public static void saveBlogPost(BlogPost blogPost) {

        final File tempFolder = new File(PathHelper.TempFolderName);
        IOHelper.deleteRecursive(tempFolder); // make sure it's empty
        tempFolder.mkdirs();

        final File photoFolder = new File(tempFolder, "/orig");
        photoFolder.mkdirs();

        final File postFile = new File(tempFolder, "post.txt");
        try {
            postFile.createNewFile();
        } catch (IOException e) {
            DialogHelper.showErrorToast("SaveBlogPost: creating post.txt failed", e);
            return;
        }

        boolean writePostFileSuccess = SaveBlogPost.savePostInfoFile(postFile, blogPost);
        if(!writePostFileSuccess) {
            return;
        }

        /* THEN, SAVE ALL IMAGES TO /ORIG */
        SaveBlogPost.saveImages(blogPost.getAllImageUris(), photoFolder);

        /* FINALLY, SAVE TO .ZIP */
        // take the title, replace all spaces with hyphens and remove all special characters
        final String fileName = blogPost.getTitle().replace(" ", "-").replaceAll("/[^A-Za-z0-9 ]/", "");

        final File sourceFolder = tempFolder;
        final String toLocation = PathHelper.FlaiFolderName + "/" + fileName + ".zip";

        try(FileOutputStream dest = new FileOutputStream(toLocation);

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest))) {
            ZipHelper.zipFolderRecursively(out, sourceFolder, sourceFolder.getPath().length());
        } catch (Exception e) {
            DialogHelper.showErrorToast("SaveBlogPost: creating zip file failed", e);
            return;
        }


        File outputZip = new File(toLocation);
        if(!outputZip.exists()) {
            DialogHelper.showErrorToast("SaveBlogPost: zip save succesful, but couldn't find it for splitting?");
            return;
        }

        long size = outputZip.length();
        if(size > SaveBlogPost.FILE_MAX_SIZE_IN_BYTES) {
            splitFile(outputZip, SaveBlogPost.FILE_MAX_SIZE_IN_BYTES);
        }
    }

    private static boolean savePostInfoFile(File postFile, BlogPost blogPost) {
         /* THEN, WRITE THE POST.TXT */
        try(FileOutputStream fileStream = new FileOutputStream(postFile);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileStream)) {

            outputWriter.write("title: " + blogPost.getTitle() + System.getProperty("line.separator"));
            outputWriter.write("trip: " + blogPost.getTrip() + System.getProperty("line.separator"));
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
            DialogHelper.showErrorToast("SaveBlogPost: writing post.txt file failed", e);
            return false;
        }

        return true;
    }

    private static void saveImages(Iterable<Uri> uris, File destinationFolder) {
        /* THEN, SAVE ALL IMAGES TO /ORIG */
        for (Uri uri : uris) {
            String sourcePath = UriHelper.getPath(BloggerApplication.getAppContext(), uri);
            String fileName = sourcePath.substring(sourcePath.lastIndexOf("/") + 1);

            final int ImageMinDimensionSize = 1080;
            File sourceFile = BitmapHelper.loadFromStorageCacheOrCreateFile(Uri.fromFile(new File(sourcePath)), PathHelper.HighQualityImageCacheFolder, ImageMinDimensionSize, ImageQuality.Original, false);
            File destinationFile = new File(destinationFolder.getPath() + "/" + fileName);
            destinationFile.getParentFile().mkdirs(); /* TODO: Okei, tää oli siin antin muokkaamssa commitissa. En tiiä miksi, mut jos jotai häikkää nii kato tätä */

            boolean copySuccess = IOHelper.copyFile(sourceFile, destinationFile);
            if(!copySuccess) {
                DialogHelper.showErrorToast("SaveBlogPost: copying image failed");
            }
        }
    }

    private static void splitFile(File outputFile, int chunkSize) {
        if(!outputFile.exists() || outputFile.length() <= chunkSize) {
            return;
        }

        String filename = PathHelper.getFilenameWithoutExtension(outputFile.getName());
        String outputFolderPath = outputFile.getParent() + "/" + filename +  "_split/";

        File outputFolder = new File(outputFolderPath);
        outputFolder.delete();
        outputFolder.mkdirs();

        IOHelper.splitFile(outputFile, outputFolder.getAbsolutePath(), chunkSize);
    }
}
