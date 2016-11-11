package flai.blogger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import flai.blogger.helpers.IOHelper;
import flai.blogger.helpers.PermissionHelper;
import flai.blogger.helpers.UIHelper;
import flai.blogger.model.BlogEntry;
import flai.blogger.model.BlogPost;
import flai.blogger.model.DayRange;
import flai.blogger.model.Image;

// okei, tää appi crashaa joskus kun valitsee kuvaa. syy: kun kuvaa valitaan niin galleria appi avautuu. koska tätä bloggeri appia ei oo enää näkyvissä, nii tää appi "stopataan". ja android voi poistaa stopatun appin muistista halutessaan.
// pitäs jotenkin säätää et onnistuis

public class MainPage extends AppCompatActivity {
    private static final int ChangeMainImageID = 1;
    private static final int LoadBlogPostID = 2;

    private EntryListAdapter _listAdapter;
    private ListView _entryListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionHelper.requestExternalStoragePermissions(this);

        setContentView(R.layout.activity_main_page);

        _listAdapter = new EntryListAdapter();
        _entryListView = (ListView) findViewById(R.id.itemList);
        _entryListView.setAdapter(_listAdapter);

        this.setupAddNewEntryButton();
        this.setupChangeMainImageButton();
        this.setupSaveButton();
        this.setupLoadButton();
        this.setupLastImageClickedImage();
    }

    /* Called when loading blog post or after selecting an image */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK) {
            return;
        }

        Uri uri = data.getData();
        if(requestCode == LoadBlogPostID) { /* Loading new blog post */
            BlogPost blogPost = LoadBlogPost.loadBlogPost(uri);
            this.initializeUiFromBlogPost(blogPost);
        }
        else if (requestCode == ChangeMainImageID) { /* Changing main image */
            this.changeMainImage(uri);
        }
        else if(requestCode >= 100 && requestCode < 1000) { /* Changing image entry */
            this.changeEntryImage(uri, requestCode - 100);
        }
    }

    private void changeEntryImage(Uri uri, int entryPosition) {
        EntryType.Image entry = (EntryType.Image) _listAdapter.getItem(entryPosition);
        entry.uri = uri;

        ImageView imageEntryView = (ImageView) _listAdapter.getViewByPosition(entryPosition, _entryListView).findViewById(R.id.image_entry_image);
        Bitmap bitmap = IOHelper.loadImage(uri, 384, 384);
        imageEntryView.setImageBitmap(bitmap);
    }

    private void changeMainImage(Uri uri) {
        _listAdapter.MainImage.uri = uri;

        ImageButton mainButton = (ImageButton) findViewById(R.id.mainImageButton);
        Bitmap bitmap = IOHelper.loadImage(uri, 160, 160);
        mainButton.setImageBitmap(bitmap);
    }

    private void initializeUiFromBlogPost(BlogPost blogPost) {
        _listAdapter.reset();

        this.setBlogTitle(blogPost.getTitle());
        this.setBlogDayRange(blogPost.getDayRange());
        this.changeMainImage(blogPost.getMainImage().getImageUri());

        for(BlogEntry entry : blogPost.entries()) {
            if(entry instanceof  BlogEntry.TextEntry) {
                BlogEntry.TextEntry textEntry = (BlogEntry.TextEntry)entry;
                _listAdapter.add(new EntryType.Text(textEntry.getText()));
            }
            else if(entry instanceof  BlogEntry.HeaderEntry) {
                BlogEntry.HeaderEntry headerEntry = (BlogEntry.HeaderEntry)entry;
                _listAdapter.add(new EntryType.Header(headerEntry.getHeaderText()));
            }
            else if(entry instanceof  BlogEntry.ImageEntry) {
                BlogEntry.ImageEntry imageEntry = (BlogEntry.ImageEntry) entry;
                _listAdapter.add(new EntryType.Image(imageEntry.getImage().getImageUri()));
            }
        }

        UIHelper.setListViewHeightBasedOnItems(_entryListView);
    }

    private String getBlogTitle() {
        return ((EditText)findViewById(R.id.title_edit_text)).getText().toString();
    }

    private DayRange getBlogDayRange() {
        return DayRange.parse(((EditText) findViewById(R.id.date_range_min)).getText().toString() + "-" + ((EditText) findViewById(R.id.date_range_max)).getText().toString());
    }

    private void setBlogTitle(String title) {
        ((EditText)findViewById(R.id.title_edit_text)).setText(title);
    }

    private void setBlogDayRange(DayRange dayRange) { // format == "X-Y"
        ((EditText)findViewById(R.id.date_range_min)).setText(Integer.toString(dayRange.StartDate));
        ((EditText)findViewById(R.id.date_range_max)).setText(Integer.toString(dayRange.EndDate));
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

    private void setupLastImageClickedImage() {
        final ImageView latestImageEntryClickedImage = (ImageView)findViewById(R.id.latest_entry_click_image);
        _listAdapter.onImageEntryImageClicked = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = (ImageView)v;
                System.out.println("onClick!!");
                if(img != null) {
                    latestImageEntryClickedImage.setImageDrawable(img.getDrawable());
                }
            }
        };
    }

    private void setupLoadButton() {
        final Button loadButton = (Button)findViewById(R.id.load_button);
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // from http://stackoverflow.com/questions/5309190/android-pick-images-from-gallery
                //
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("application/zip");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("application/zip");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Zip");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
                startActivityForResult(chooserIntent, LoadBlogPostID);
            }
        });
    }

    private void setupSaveButton() {
        final Button saveButton = (Button)findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BlogPost blogPost = new BlogPost(
                        getBlogTitle(),
                        getBlogDayRange(),
                        _listAdapter.getBlogEntries(),
                        new Image(_listAdapter.MainImage.uri));

                SaveBlogPost.saveBlogPost(blogPost);
            }
        });
    }

    private void setupChangeMainImageButton() {
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
                startActivityForResult(chooserIntent, ChangeMainImageID);
            }
        });
    }

    private void setupAddNewEntryButton() {
        final FloatingActionButton addNewEntryButton = (FloatingActionButton) findViewById(R.id.add_fab);
        addNewEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] items = new String[]{"Text", "Header", "Image"};
                new AlertDialog.Builder(view.getContext())
                        .setTitle("New entry")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO: create new item here!

                                // 0 == text, 1 == header, 2 == image, 3 == image group
                                EntryType entry = (which == EntryType.TYPE_TEXT) ? new EntryType.Text() : ((which == EntryType.TYPE_HEADER) ? new EntryType.Header() : ((which == EntryType.TYPE_IMAGE) ? new EntryType.Image() : new EntryType.ImageGroup()));
                                _listAdapter.add(entry);

                                UIHelper.setListViewHeightBasedOnItems(_entryListView);
                            }
                        }).show();
            }
        });

    }
}
