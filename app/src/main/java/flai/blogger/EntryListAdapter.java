package flai.blogger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import flai.blogger.model.BlogEntry;
import flai.blogger.model.Image;

/**
 * Created by Jaakko on 04.04.2016.
 */
public class EntryListAdapter extends BaseAdapter {
    private final ArrayList<EntryType> list = new ArrayList<>();
    public EntryType.Image MainImage = new EntryType.Image();

    public View.OnClickListener onImageEntryImageClicked = null;

    @Override
    public int getViewTypeCount() {
        return 4; // image, image group, header, text, in that order
    }

    // not used really
    @Override
    public int getItemViewType(int position) {
        EntryType item = this.getItem(position);
        return item.getEntryType();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public EntryType getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0; // not used
    }

    // getView is called when new item is added to the list.
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View entryView = convertView;
        final EntryType entry = list.get(position);

        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(entry instanceof EntryType.Image) {

            entryView = inflater.inflate(R.layout.image_entry_view, parent, false);

            final EntryType.Image imgE = (EntryType.Image)entry;
            EditText textField = (EditText)entryView.findViewById(R.id.image_entry_text);
            textField.setText(imgE.text);

            textField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override
                public void afterTextChanged(Editable s) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    imgE.text = s.toString(); // update the entry's text when textField changes
                }
            });

            Button changeImageButton = (Button)entryView.findViewById(R.id.change_image_button);
            changeImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                // give 100 + position as parameter (the 'position' can then later be calculated in MainPage.onActivityResult with value - 100)
                ((Activity) parent.getContext()).startActivityForResult(chooserIntent, 100 + position);
                }
            });

            final ImageView imageEntryView = (ImageView) entryView.findViewById(R.id.image_entry_image);
            imageEntryView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                System.out.println("XXXASD");
                if(onImageEntryImageClicked != null) {
                    onImageEntryImageClicked.onClick(v);
                }
                }
            });

            // set the entry's image to ImageView
            Uri imageUri = ((EntryType.Image) entry).uri;
            if(imageUri != null) {
                InputStream inputStream = null;
                try {
                    inputStream = parent.getContext().getContentResolver().openInputStream(imageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    System.out.println("ERROR: ENTRYLISTADAPTER.GETVIEWISSÄ KUSEE");
                    return entryView;
                }


                imageEntryView.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeStream(inputStream), 384, 384, false));
            }
        }
        else if(entry instanceof  EntryType.Text) {
            entryView = inflater.inflate(R.layout.text_entry_view, parent, false);

            final EntryType.Text textEntry = (EntryType.Text)entry;
            EditText textField = (EditText)entryView.findViewById(R.id.text_entry_text);
            textField.setText(textEntry.text);

            textField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override
                public void afterTextChanged(Editable s) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    textEntry.text = s.toString(); // update the entry's text when textField changes
                }
            });
        }
        else if(entry instanceof  EntryType.Header) {
            entryView = inflater.inflate(R.layout.header_entry_view, parent, false);

            final EntryType.Header textEntry = (EntryType.Header)entry;
            EditText textField = (EditText)entryView.findViewById(R.id.text_entry_text);
            textField.setText(textEntry.header);

            textField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override
                public void afterTextChanged(Editable s) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    textEntry.header = s.toString(); // update the entry's text when textField changes
                }
            });
        }


        // these buttons are on the right side of the entries (move up, down, delete buttons)
        Button moveUp = (Button)entryView.findViewById(R.id.move_up_entry);
        Button moveDown = (Button)entryView.findViewById(R.id.move_down_entry);
        Button deleteButton = (Button)entryView.findViewById(R.id.delete_entry);

        moveUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position == 0) return; // if the item is already on top, then ignore

                EntryType entryOneHigher = list.get(position - 1);
                list.set(position - 1, entry);
                list.set(position, entryOneHigher);

                notifyDataSetChanged();
            }
        });

        moveDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position == list.size() - 1) return; // if the item is already on bottom, then ignore

                EntryType entryOneLower = list.get(position + 1);
                list.set(position + 1, entry);
                list.set(position, entryOneLower);

                notifyDataSetChanged();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.remove(position);

                notifyDataSetChanged();
            }
        });

        return entryView;
    }

    public void add(EntryType entry) {
        list.add(entry);
        this.notifyDataSetChanged();
    }

    // returns all image uri's in the entries, including MainImage
    public HashSet<Uri> allImageUris() {
        HashSet<Uri> uris = new HashSet<>();
        for(EntryType entry : list) {
            if(entry instanceof EntryType.Image) {
                uris.add(((EntryType.Image)entry).uri);
            }
        }

        uris.add(this.MainImage.uri);
        return uris;
    }

    // gets the view of the entry in 'pos'
    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    // reset. called when blog post is loaded from file
    public void reset() {
        this.list.clear();
        this.MainImage.uri = null;
        this.notifyDataSetChanged();
    }

    public List<BlogEntry> getBlogEntries() {
        ArrayList<BlogEntry> blogEntries = new ArrayList<>();
        for(EntryType entry : list) {
            if(entry.getEntryType() == EntryType.TYPE_TEXT) {
                blogEntries.add(new BlogEntry.TextEntry(((EntryType.Text)entry).text));
            }
            else if(entry.getEntryType() == EntryType.TYPE_HEADER) {
                blogEntries.add(new BlogEntry.HeaderEntry(((EntryType.Header)entry).header));
            }
            else if(entry.getEntryType() == EntryType.TYPE_IMAGE) {
                EntryType.Image imageEntry = (EntryType.Image)entry;
                blogEntries.add(new BlogEntry.ImageEntry(new Image(imageEntry.uri), imageEntry.text));
            }
        }

        return blogEntries;
    }
}
