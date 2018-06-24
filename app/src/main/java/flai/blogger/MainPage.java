package flai.blogger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import flai.blogger.helpers.DialogHelper;
import flai.blogger.helpers.IntentHelper;
import flai.blogger.helpers.MiscHelper;
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
    private static final int LoadNewImagesID = 3;

    private BlogPost _currentBlogPost = new BlogPost();
    private EntryListAdapter _listAdapter;
    private ListView _entryListView;

    private EditText _blogPostTitleEditText;
    private EditText _blogPostStartDayEditText;
    private EditText _blogPostEndDayEditText;
    private ImageView _blogPostMainImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionHelper.requestExternalStoragePermissions(this);

        setContentView(R.layout.activity_main_page);
        _listAdapter = new EntryListAdapter(_currentBlogPost);
        _entryListView = (ListView) findViewById(R.id.itemList);
        _entryListView.setAdapter(_listAdapter);

        _blogPostTitleEditText = ((EditText)findViewById(R.id.title_edit_text));
        _blogPostStartDayEditText = ((EditText)findViewById(R.id.date_range_min));
        _blogPostEndDayEditText = ((EditText)findViewById(R.id.date_range_max));
        _blogPostMainImageView = (ImageButton) findViewById(R.id.mainImageButton);;

        this.setupAddNewEntryButton();
        this.setupChangeMainImageButton();
        this.setupChangeTripButton();
        this.setupSaveButton();
        this.setupLoadButton();
        this.setupOnImageEntryClicked();

        _blogPostTitleEditText.setText(_currentBlogPost.getTitle());
        _blogPostTitleEditText.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) { }

                    @Override
                    public void afterTextChanged(Editable s) {
                        _currentBlogPost.setTitle(s.toString());
                    }
                });

        _blogPostStartDayEditText.setText(Integer.toString(_currentBlogPost.getDayRange().StartDate));
        _blogPostStartDayEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                Integer day = MiscHelper.parseIntegerOrNull(s.toString());
                _currentBlogPost.getDayRange().StartDate = (day != null) ? day : 0;
            }
        });

        _blogPostEndDayEditText.setText(Integer.toString(_currentBlogPost.getDayRange().EndDate));
        _blogPostEndDayEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                Integer day = MiscHelper.parseIntegerOrNull(s.toString());
                _currentBlogPost.getDayRange().EndDate = (day != null) ? day : 0;
            }
        });

        updateTripTextFromSettings();
    }

    /* Called when loading blog post or after selecting an image */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK) {
            return;
        }

        Uri uri = data.getData();
        if(requestCode == LoadBlogPostID) { /* Loading new blog post */
            _currentBlogPost = LoadBlogPost.loadBlogPost(uri);
            this.refreshUI();
        }
        else if (requestCode == ChangeMainImageID) { /* Changing main image */
            this.changeMainImage(uri);
        }
        else if(requestCode == LoadNewImagesID) {
            if(data.getClipData() == null) {
                this.loadImage(new Image(uri, null));
            }
            else {
                this.loadNewImages(data.getClipData());
            }
        }
        else if(requestCode >= 100 && requestCode < 1000) { /* Changing image entry */
            this.changeEntryImage(uri, requestCode - 100);
        }
    }

    private void loadImage(Image image)  {
        if (!image.isValid()) {
            DialogHelper.showErrorDialog(MainPage.this, "Invalid image, file path can't be determined for some reason. Try moving the file or something");
            return;
        }

        _currentBlogPost.entries().add(new BlogEntry.ImageEntry(image));
        _listAdapter.refresh();

        UIHelper.setListViewHeightBasedOnItems(_entryListView);
    }

    private void loadNewImages(ClipData clipData) {
        List<Image> images =
            IntStream.range(0, clipData.getItemCount())
            .mapToObj(i -> new Image(clipData.getItemAt(i).getUri(), null))
            .collect(Collectors.toList());

        Stream<Image> validImages = images.stream().filter(Image::isValid);
        List<Image> invalidImages = images.stream().filter(img -> !img.isValid()).collect(Collectors.toList());

        Stream<Image> sortedImages =
            validImages
            .sorted(Comparator.comparing(Image::getFilename));

        sortedImages.forEach(this::loadImage);
        if(invalidImages.size() > 0) {
            String errorMessage =
                invalidImages.size() + " invalid images, file path could not be determined for some reason. Try to move the file somewhere else or something\n" +
                invalidImages.stream().map(img -> img.getImageUri().getPath()).collect(Collectors.joining("\n"));

            DialogHelper.showErrorDialog(MainPage.this, errorMessage);
        }
    }

    private void changeEntryImage(Uri uri, int entryPosition) {
        BlogEntry.ImageEntry entry = (BlogEntry.ImageEntry) _listAdapter.getItem(entryPosition);
        entry.getImage().setImageUri(uri);

        ImageView imageEntryView = (ImageView) _listAdapter.getViewByPosition(entryPosition, _entryListView).findViewById(R.id.image_entry_image);
        imageEntryView.setImageBitmap(entry.getImage().getThumbnail());

        _listAdapter.refresh();
    }

    private void changeMainImage(Uri uri) {
        _currentBlogPost.getMainImage().setImageUri(uri);
        _blogPostMainImageView.setImageBitmap(_currentBlogPost.getMainImage().getThumbnail());
    }

    private void refreshUI() {
        _listAdapter.setBlogPost(_currentBlogPost);

        this.setBlogTitle(_currentBlogPost.getTitle());
        this.setBlogDayRange(_currentBlogPost.getDayRange());
        this.changeMainImage(_currentBlogPost.getMainImage().getImageUri());

        UIHelper.setListViewHeightBasedOnItems(_entryListView);
    }

    private void setBlogTitle(String title) {
        _blogPostTitleEditText.setText(title);
    }

    private void setBlogDayRange(DayRange dayRange){ // format == "X-Y"
        _blogPostStartDayEditText.setText(Integer.toString(dayRange.StartDate));
        _blogPostEndDayEditText.setText(Integer.toString(dayRange.EndDate));
    }

    // display the clicked image entry in fullscreen popup
    private void setupOnImageEntryClicked() {
        _listAdapter.ImageEntryClickedListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView clickedImageView = (ImageView)v;
                if(clickedImageView == null || clickedImageView.getDrawable() == null) {
                    return;
                }

                final ImageView imageViewToDisplay = new ImageView(MainPage.this); // will be fullscreen
                imageViewToDisplay.setImageDrawable(clickedImageView.getDrawable());

                final Dialog dialog = new Dialog(MainPage.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

                dialog.setContentView(imageViewToDisplay);
                imageViewToDisplay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        };
    }

    @Override // display yes/no popup dialog when exiting with back button
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setTitle("Closing App")
            .setMessage("Are you sure you want to close Blogger?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            })
            .setNegativeButton("No", null)
            .show();
    }

    private void setupLoadButton() {
        final Button loadButton = (Button)findViewById(R.id.load_button);
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentHelper.showZipPicker(MainPage.this, LoadBlogPostID);
            }
        });
    }

    private void setupSaveButton() {
        final Button saveButton = (Button)findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!_currentBlogPost.canBeSaved()) {
                    DialogHelper.showErrorDialog(MainPage.this, "Blog post can't be saved (invalid title, dayrange etc)");
                    return;
                }

                SaveBlogPost.saveBlogPost(_currentBlogPost);
            }
        });
    }

    private void setupChangeMainImageButton() {
        _blogPostMainImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentHelper.showImagePicker(MainPage.this, ChangeMainImageID, false);
            }
        });
    }

    private void setupChangeTripButton() {
        final EditText tripEditText = (EditText) findViewById(R.id.trip_edit_text);
        tripEditText.setOnClickListener(view -> {
            Context context = view.getContext();
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);

            final EditText tripNameEditText = new EditText(context);
            tripNameEditText.setHint("Trip name");
            tripNameEditText.setText(Settings.getTripName().orElse(""));
            layout.addView(tripNameEditText);

            final EditText tripStartDateEditText = new EditText(context);
            tripStartDateEditText.setHint("Start date ('17.12.1995' for example)");
            tripStartDateEditText.setText(Settings.getTripStartDateString().orElse(""));
            layout.addView(tripStartDateEditText);

            new AlertDialog.Builder(view.getContext())
                    .setTitle("Change trip")
                    .setView(layout)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String newTripName = tripNameEditText.getText().toString().trim();
                        if(newTripName.length() > 0) {
                            Settings.setTripName(newTripName);
                        }

                        String newTripStartDate = tripStartDateEditText.getText().toString().trim();
                        if(newTripStartDate.length() > 0) {
                           try {
                               Settings.setTripStartDate(newTripStartDate);
                           }
                           catch(Exception e) {
                               new AlertDialog.Builder(view.getContext())
                                       .setTitle("Error")
                                       .setMessage("Error: probably invalid date format")
                                       .setNeutralButton("OK", (a, b) -> { })
                                       .show();
                           }
                        }

                        updateTripTextFromSettings();
                        dialog.dismiss();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                    .show();
        });

        tripEditText.setText("Morocco 2017 (current day: 30)");
    }

    private void updateTripTextFromSettings() {
        String newTripEditText = "";
        newTripEditText += Settings.getTripName().orElse("Trip not set");
        newTripEditText += Settings.getTripStartDate().map(d -> " (Current day: " + (TimeUnit.DAYS.convert(new Date().getTime() - d.getTime(), TimeUnit.MILLISECONDS) + 1) + ")").orElse(" (No start date set)");

        final EditText tripEditText = (EditText) findViewById(R.id.trip_edit_text);
        tripEditText.setText(newTripEditText);

        _currentBlogPost.setTrip(Settings.getTripName().orElse("No Trip"));
    }

    private void setupAddNewEntryButton() {
        final FloatingActionButton addNewEntryButton = (FloatingActionButton) findViewById(R.id.add_fab);
        addNewEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int TextIndex = 0;
                final int HeaderIndex = 1;
                final int ImageIndex = 2;

                final String[] items = new String[]{"Text", "Header", "Image"};
                new AlertDialog.Builder(view.getContext())
                        .setTitle("New entry")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO: create new item here!

                                // on entry type == image, don't add a new entry but
                                // show image picker and allow choosing multiple images
                                if (which == ImageIndex) {
                                    chooseNewImages();
                                    return;
                                }
                                else {
                                    if (which == TextIndex) {
                                        _currentBlogPost.entries().add(new BlogEntry.TextEntry(""));
                                    }
                                    else { // header
                                        _currentBlogPost.entries().add(new BlogEntry.HeaderEntry(""));
                                    }

                                    UIHelper.setListViewHeightBasedOnItems(_entryListView);
                                }
                            }
                        }).show();
            }
        });
    }


    private void chooseNewImages() {
        IntentHelper.showImagePicker(this, LoadNewImagesID, true);
    }
}
