package flai.blogger;

import android.app.Activity;
import android.content.Context;
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
import android.widget.TextView;

import flai.blogger.helpers.IntentHelper;
import flai.blogger.model.BlogEntry;
import flai.blogger.model.BlogPost;


// TODO: Instead of allowing images with "null", create some kind of "DefaultImage" ??
public class EntryListAdapter extends BaseAdapter {

    private BlogPost _currentBlogPost = new BlogPost();
    public View.OnClickListener ImageEntryClickedListener = null;

    public EntryListAdapter(BlogPost blogPost) {
        _currentBlogPost = blogPost;
    }

    public void setBlogPost(BlogPost blogPost) {
        _currentBlogPost = blogPost;
        this.notifyDataSetChanged();
    }

    @Override
    public int getViewTypeCount() {
        return 3; // image, header, text, in that order
    }

    // not used really
    @Override
    public int getItemViewType(int position) {
        BlogEntry item = this.getItem(position);
        return (item instanceof BlogEntry.TextEntry) ? 0 : (item instanceof  BlogEntry.HeaderEntry) ? 1 : 2;
    }

    @Override
    public int getCount() {
        return _currentBlogPost.entries().size();
    }

    @Override
    public BlogEntry getItem(int position) {
        return _currentBlogPost.entries().get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0; // not used
    }

    // getView is called when new item is added to the list.
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View entryView = convertView;
        final BlogEntry entry = _currentBlogPost.entries().get(position);

        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(entry instanceof BlogEntry.ImageEntry) {
            class ImageEntryViewHolder {
                public Button ChangeImageButton;
                public ImageView ImageView;
                public EditText ImageText;
                public TextView ImageFilenameText;

                public int Position;
            }

            final ImageEntryViewHolder viewHolder;
            if(entryView == null) {
                entryView = inflater.inflate(R.layout.image_entry_view, parent, false);

                viewHolder = new ImageEntryViewHolder();
                viewHolder.ChangeImageButton = (Button)entryView.findViewById(R.id.change_image_button);
                viewHolder.ImageView = (ImageView) entryView.findViewById(R.id.image_entry_image);
                viewHolder.ImageText = (EditText)entryView.findViewById(R.id.image_entry_text);
                viewHolder.ImageFilenameText = (TextView)entryView.findViewById(R.id.image_entry_filename);

                entryView.setTag(viewHolder);
            }
            else {
                viewHolder = (ImageEntryViewHolder)entryView.getTag();
            }

            viewHolder.Position = position;
            viewHolder.ImageText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void afterTextChanged(Editable s) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    BlogEntry.ImageEntry imageEntry = (BlogEntry.ImageEntry) getItem(viewHolder.Position);
                    imageEntry.setImageText(s.toString()); // update the entry's text when textField changes
                }
            });

            viewHolder.ChangeImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // give 100 + position as parameter (the 'position' can then later be calculated in MainPage.onActivityResult with value - 100)
                    IntentHelper.showImagePicker((Activity) parent.getContext(), 100 + viewHolder.Position, false);
                }
            });

            viewHolder.ImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ImageEntryClickedListener != null) {
                        ImageEntryClickedListener.onClick(v);
                    }
                }
            });

            BlogEntry.ImageEntry imageEntry = (BlogEntry.ImageEntry)entry;
            viewHolder.ImageText.setText(imageEntry.getImageText());
            viewHolder.ImageFilenameText.setText(imageEntry.getImage().getFilename());
            viewHolder.ImageView.setImageBitmap(imageEntry.getImage().getThumbnail());
        }
        else if(entry instanceof BlogEntry.TextEntry) {
            class TextEntryViewHolder {
                public EditText Text;
                public int Position;
            }

            final TextEntryViewHolder viewHolder;
            if(entryView == null) {
                entryView = inflater.inflate(R.layout.text_entry_view, parent, false);

                viewHolder = new TextEntryViewHolder();
                entryView.setTag(viewHolder);

                viewHolder.Text = (EditText)entryView.findViewById(R.id.text_entry_text);
                viewHolder.Text.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void afterTextChanged(Editable s) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        BlogEntry.TextEntry textEntry = (BlogEntry.TextEntry) getItem(viewHolder.Position);
                        textEntry.setText(s.toString()); // update the entry's text when textField changes
                    }
                });
            }
            else {
                viewHolder = (TextEntryViewHolder)entryView.getTag();
            }

            viewHolder.Position = position;

            BlogEntry.TextEntry textEntry = (BlogEntry.TextEntry)entry;
            viewHolder.Text.setText(textEntry.getText());
        }
        else if(entry instanceof BlogEntry.HeaderEntry) {
            class HeaderEntryViewHolder {
                public EditText Header;
                public int Position;
            }

                final HeaderEntryViewHolder viewHolder;
                if(entryView == null) {
                    entryView = inflater.inflate(R.layout.header_entry_view, parent, false);

                    viewHolder = new HeaderEntryViewHolder();
                    entryView.setTag(viewHolder);

                    viewHolder.Header = (EditText)entryView.findViewById(R.id.header_entry_text);
                    viewHolder.Header.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                        @Override
                        public void afterTextChanged(Editable s) { }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            BlogEntry.HeaderEntry headerEntry = (BlogEntry.HeaderEntry) getItem(viewHolder.Position);
                            headerEntry.setHeaderText(s.toString()); // update the entry's text when textField changes
                        }
                    });
                }
                else {
                    viewHolder = (HeaderEntryViewHolder)entryView.getTag();
                }

                viewHolder.Position = position;

                BlogEntry.HeaderEntry textEntry = (BlogEntry.HeaderEntry)entry;
                viewHolder.Header.setText(textEntry.getHeaderText());
        }

        // these buttons are on the right side of the entries (move up, down, delete buttons)
        Button moveUp = (Button)entryView.findViewById(R.id.move_up_entry);
        Button moveDown = (Button)entryView.findViewById(R.id.move_down_entry);
        Button deleteButton = (Button)entryView.findViewById(R.id.delete_entry);

        moveUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position == 0) return; // if the  item is already on top, then ignore

                BlogEntry entryOneHigher = _currentBlogPost.entries().get(position - 1);
                _currentBlogPost.entries().set(position - 1, entry);
                _currentBlogPost.entries().set(position, entryOneHigher);

                notifyDataSetChanged();
            }
        });

        moveDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == _currentBlogPost.entries().size() - 1)
                    return; // if the item is already on bottom, then ignore

                BlogEntry entryOneLower = _currentBlogPost.entries().get(position + 1);
                _currentBlogPost.entries().set(position + 1, entry);
                _currentBlogPost.entries().set(position, entryOneLower);

                notifyDataSetChanged();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _currentBlogPost.entries().remove(position);

                notifyDataSetChanged();
            }
        });

        return entryView;
    }

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

    public void refresh() {
        this.notifyDataSetChanged();
    }
}
