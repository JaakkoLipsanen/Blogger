package flai.blogger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

// okei, tää appi crashaa joskus kun valitsee kuvaa. syy: kun kuvaa valitaan niin galleria appi avautuu. koska tätä bloggeri appia ei oo enää näkyvissä, nii tää appi "stopataan". ja android voi poistaa stopatun appin muistista halutessaan.
// pitäs jotenkin säätää et onnistuis

public class MainPage extends AppCompatActivity {
    // just some int's that are used in onActivityResult
    private static final int PICK_MAIN_IMAGE = 1;
    private static final int PICK_LOADED_FILE = 2;

    private final String flaiFolderName = "/storage/sdcard1" + "/flai";
    private final String imageFolderName = flaiFolderName + "/images_all";
    private final String tempFolderName = flaiFolderName + "/temp_folder";

    private EntryListAdapter listAdapter;
    private ListView _listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        final ListView list = (ListView) findViewById(R.id.itemList);
        _listView = list;
        listAdapter = new EntryListAdapter();

        list.setAdapter(listAdapter);

        final FloatingActionButton addNewEntryButton = (FloatingActionButton) findViewById(R.id.add_fab);
        addNewEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] items = new String[]{"Text", "Header", "Image", "Image Group"};
                new AlertDialog.Builder(view.getContext())
                        .setTitle("New entry")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO: create new item here!

                                // 0 == text, 1 == header, 2 == image, 3 == image group
                                EntryType entry = (which == EntryType.TYPE_TEXT) ? new EntryType.Text() : ((which == EntryType.TYPE_HEADER) ? new EntryType.Header() : ((which == EntryType.TYPE_IMAGE) ? new EntryType.Image() : new EntryType.ImageGroup()));
                                listAdapter.add(entry);

                                setListViewHeightBasedOnItems(list);
                            }
                        })
                        .show();
            }
        });

        // on main image clicked
        findViewById(R.id.mainImageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // from http://stackoverflow.com/questions/5309190/android-pick-images-from-gallery
                //
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
                startActivityForResult(chooserIntent, PICK_MAIN_IMAGE);
            }
        });


        final Button saveButton = (Button)findViewById(R.id.save_button);
        final MainPage mainPage = this;
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* FIRST, MAKE SURE ALL FOLDERS ARE CREATED */
                final File flaiFolder = new File(flaiFolderName);
                flaiFolder.mkdir(); // make sure this exists

                final File imageFolder = new File(imageFolderName);
                imageFolder.mkdir();

                final File tempFolder = new File(tempFolderName);

                deleteRecursive(tempFolder);
                tempFolder.mkdir();

                File photoFolder = new File(tempFolderName + "/orig");
                photoFolder.mkdir();

                final File postFile = new File(tempFolderName, "posts.txt");
                try {
                    postFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                /* THEN, WRITE THE POSTS.TXT */
                try {
                    FileOutputStream fileStream = new FileOutputStream(postFile);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileStream);

                    outputStreamWriter.write("title: " + mainPage.getEntryTitle() + System.getProperty("line.separator"));
                    outputStreamWriter.write("trip: USA 2016" + System.getProperty("line.separator"));
                    outputStreamWriter.write("date-range: " + mainPage.getDateRange() + System.getProperty("line.separator"));


                    outputStreamWriter.write("main-");
                    listAdapter.MainImage.write(outputStreamWriter);

                    outputStreamWriter.write(System.getProperty("line.separator"));
                    outputStreamWriter.write(System.getProperty("line.separator"));

                    for (int i = 0; i < listAdapter.getCount(); i++) {
                        EntryType type = listAdapter.getItem(i);
                        type.write(outputStreamWriter);
                        outputStreamWriter.write(System.getProperty("line.separator"));
                    }

                    outputStreamWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                /* THEN, SAVE ALL IMAGES TO /ORIG */
                for (Uri uri : listAdapter.allImageUris()) {

                    String sourcePath = uri.getPath();
                    String fileName = uri.getLastPathSegment();
                    if (sourcePath.startsWith("/external") || sourcePath.startsWith("content")) { //  // if the uri is content:// or /external path (this happens always when image is 'picked' with gallery)
                        final String[] proj = {MediaStore.Images.Media.DATA};

                        Cursor c = getApplicationContext().getContentResolver().query(uri, proj, null, null, null);
                        int column_index = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        c.moveToFirst();
                        sourcePath = c.getString(column_index);

                        fileName = sourcePath.substring(sourcePath.lastIndexOf("/") + 1);
                    }

                    File destination = new File(photoFolder.getPath() + "/" + fileName);
                    OutputStream out = null;
                    try {
                        out = new FileOutputStream(destination);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }


                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                    Bitmap bitmap = BitmapFactory.decodeFile(sourcePath, options);  /* IF I WANT TO RESIZE THE IMAGES A BIT SMALLER, USE THIS:  BITMAP_RESIZER(BitmapFactory.decodeFile(sourcePath, options), 1920); */
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                }

                /* FINALLY, SAVE TO .ZIP */
                try {
                    final int BUFFER = 2048;

                    // take the title, replace all spaces with hyphens and remove all special characters
                    final String fileName = mainPage.getEntryTitle().replace(" ", "-").replaceAll("/[^A-Za-z0-9 ]/", "");

                    final File sourceFile = tempFolder;
                    final String toLocation = flaiFolderName + "/" + fileName + ".zip";

                    BufferedInputStream origin = null;
                    FileOutputStream dest = new FileOutputStream(toLocation);
                    ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                            dest));
                    if (sourceFile.isDirectory()) {
                        zipSubFolder(out, sourceFile, sourceFile.getPath().length());
                    } else {
                        byte data[] = new byte[BUFFER];
                        FileInputStream fi = new FileInputStream(tempFolder);
                        origin = new BufferedInputStream(fi, BUFFER);
                        ZipEntry entry = new ZipEntry(getLastPathComponent(tempFolderName));
                        out.putNextEntry(entry);
                        int count;
                        while ((count = origin.read(data, 0, BUFFER)) != -1) {
                            out.write(data, 0, count);
                        }
                    }
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        });


        final Button loadButton = (Button)findViewById(R.id.load_button);
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // from http://stackoverflow.com/questions/5309190/android-pick-images-from-gallery
                //
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("application/zip");
                // getIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("application/zip");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Zip");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
                startActivityForResult(chooserIntent, PICK_LOADED_FILE);
            }
        });
    }

    // ACTIVITY == CHOOSE IMAGE OR ZIP FILE DIALOG
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // PICK_MAIN_IMAGE AND PIC_LOADED_FILE ARE JUST INT'S THAT I'VE DEFINED

        if (requestCode == PICK_MAIN_IMAGE) { /* MAIN IMAGE PICKING */
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)

                ImageButton mainButton = (ImageButton) findViewById(R.id.mainImageButton);
                try {
                    InputStream inputStream = getContentResolver().openInputStream(data.getData());

                    listAdapter.MainImage.uri = data.getData();
                    mainButton.setImageBitmap (Bitmap.createScaledBitmap(BitmapFactory.decodeStream(inputStream), 160, 160, false));

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        else if(requestCode == PICK_LOADED_FILE) { /* THIS IS WHEN BLOG ENTRY WAS LOADED (WITH 'LOAD' BUTTON) */
            if(resultCode == RESULT_OK) {

                try {
                    listAdapter.reset();

                    InputStream inputStream = new BufferedInputStream(getContentResolver().openInputStream(data.getData()));
                    ZipInputStream zipInput = new ZipInputStream(inputStream);

                    String postsFileContent = "";

                    ZipEntry entry = null;
                    while((entry = zipInput.getNextEntry()) != null) { /* LOAD ALL FILES IN THE .ZIP */

                        if(entry.getName().equals("/posts.txt")) {
                            final int BUFFER = 8192;

                            byte bytes[] = new byte[BUFFER];
                            int count = zipInput.read(bytes, 0, BUFFER);

                            postsFileContent = new String(Arrays.copyOfRange(bytes, 0, count), "UTF-8");
                             //  this.loadPostsFile(zipInput, entry); // POSTS.TXT IS LOADED AFTER EVERYTHING SINCE IMAGES MUST BE LOADED BEFORE POSTS
                        }
                        else if(entry.getName().startsWith("/orig/")) { // aka image!!
                            final int BUFFER = 2048;

                            new File(imageFolderName).mkdirs();
                            final File imageFile = new File(imageFolderName + "/" + getLastPathComponent(entry.getName())); /* SAVE IMAGES TO /flai/images_all */
                            imageFile.delete(); // make sure deleted

                            imageFile.createNewFile();

                            FileOutputStream outputStream = new FileOutputStream(imageFile);
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

                    this.parsePostsFile(postsFileContent);

                    ImageButton mainButton = (ImageButton) findViewById(R.id.mainImageButton);
                    mainButton.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeStream(new FileInputStream(listAdapter.MainImage.uri.getPath())), 160, 160, false));

                    setListViewHeightBasedOnItems((ListView)findViewById(R.id.itemList));


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else if(requestCode >= 100 && requestCode < 1000) { // "PICK_IMAGE_ENTRY". THIS IS CALLED WHEN IMAGE ENTRY WAS CHANGED */
            if (resultCode == RESULT_OK) {

                try {
                    InputStream inputStream = getContentResolver().openInputStream(data.getData());

                    int itemPosition = requestCode - 100;
                    EntryType.Image entry = (EntryType.Image)listAdapter.getItem(itemPosition);
                    ImageView imageEntryView = (ImageView)listAdapter.getViewByPosition(itemPosition, _listView).findViewById(R.id.image_entry_image);

                    imageEntryView.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeStream(inputStream), 128, 128, false));
                    entry.uri = data.getData();

                    //mTmpGalleryPicturePath = selectedImage.getPath();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getEntryTitle() {
        return ((EditText)findViewById(R.id.title_edit_text)).getText().toString();
    }

    private String getDateRange() {
        return ((EditText)findViewById(R.id.date_range_min)).getText().toString() + "-" + ((EditText)findViewById(R.id.date_range_max)).getText().toString();
    }

    private void setEntryTitle(String title) {
        ((EditText)findViewById(R.id.title_edit_text)).setText(title);
    }

    private void setDateRange(String dateRange) { // format == "X-Y"
        String min = dateRange.split("-")[0];
        String max = dateRange.split("-")[1];

        ((EditText)findViewById(R.id.date_range_min)).setText(min);
        ((EditText)findViewById(R.id.date_range_max)).setText(max);
    }

    /* PARSES THE posts.txt from string (that contains the content of posts.txt */
    private void parsePostsFile(String postsFileContent) throws IOException {

        String[] lines = postsFileContent.split(System.getProperty("line.separator"));

        String title = lines[0].split(":")[1].trim();
        String trip = lines[1].split(":")[1].trim();
        String dateRange = lines[2].split(":")[1].trim();
        String mainImage = lines[3].split(":")[1].trim();

        this.setEntryTitle(title);
        this.setDateRange(dateRange);
        // 'trip' you can ignore, it's hard coded to USA 2016

        final File imageFolder = new File(imageFolderName);
        imageFolder.mkdir(); // make sure exists

        for(int i = 4; i < lines.length; i++) { // load all other info
            String line = lines[i].trim();
            if(line.isEmpty()) {
                continue;
            }

            String tag = line.split(":")[0].trim();
            String content = line.split(":")[1].trim();

            if(tag.equals("text")) {
                EntryType.Text t = new EntryType.Text();
                t.text = content;

                listAdapter.add(t);
            }
            else if(tag.equals("header")) {
                EntryType.Header header = new EntryType.Header();
                header.header = content;

                listAdapter.add(header);
            }
            else if(tag.equals("image")) {
                EntryType.Image image = new EntryType.Image();
                image.uri = Uri.fromFile(new File(imageFolderName + "/" + content));

                listAdapter.add(image);
            }
        }

        listAdapter.MainImage.uri = Uri.fromFile(new File(imageFolderName + "/" + mainImage));
    }

    /* DUNNO WHAT THIS IS, AUTO GENERATED */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_page, menu);
        return true;
    }

    /* DUNNO WHAT THIS IS, AUTO GENERATED */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // sets the entry ListView's height based on how many items there are in the list
    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 360 + numberOfItems * 240; // default height

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }

    }

    // recursive zipping method. zips the folder and sub folders and files
    private static void zipSubFolder(ZipOutputStream out, File folder,
                              int basePathLength) throws IOException {
        final int BUFFER = 2048;

        File[] fileList = folder.listFiles();
        BufferedInputStream origin = null;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath
                        .substring(basePathLength);

                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }

    /*
     * gets the last path component
     *
     * Example: getLastPathComponent("downloads/example/fileToZip");
     * Result: "fileToZip"
     */
    public static String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        String lastPathComponent = segments[segments.length - 1];
        return lastPathComponent;
    }

    // deleters folder and all sub folders and files
    private static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

}
