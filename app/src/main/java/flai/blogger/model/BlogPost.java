package flai.blogger.model;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Jaakko on 11.11.2016.
 */
public class BlogPost {
    private String _title;
    private DayRange _dayRange;

    private ArrayList<BlogEntry> _entries;
    private Image _mainImage;


    public BlogPost() {
        this("Default", new DayRange(1, 2), new ArrayList<BlogEntry>(), new Image(null));
    }

    public BlogPost(String title, DayRange dayRange, List<BlogEntry> entries, Image mainImage) {
        _title = title;
        _dayRange = dayRange;

        _entries = new ArrayList<>(entries);
        _mainImage = mainImage;
    }

    public String getTitle() { return _title; }
    public void setTitle(String title) { _title = title; }

    public DayRange getDayRange() {  return _dayRange; }

    public List<BlogEntry> entries() {
        return _entries;
    }

    public Image getMainImage() {
        return _mainImage;
    }

    public List<Uri> getAllImageUris() {
        HashSet<Uri> uris = new HashSet<>();
        if(_mainImage != null) {
            uris.add(_mainImage.getImageUri());
        }

        for(BlogEntry entry : _entries) {
            if(entry instanceof BlogEntry.ImageEntry) {
                BlogEntry.ImageEntry imageEntry = (BlogEntry.ImageEntry)entry;
                uris.add(imageEntry.getImage().getImageUri());
            }
        }

        return new ArrayList<>(uris);
    }
}
