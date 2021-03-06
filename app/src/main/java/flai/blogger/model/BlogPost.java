package flai.blogger.model;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import flai.blogger.Settings;

/**
 * Created by Jaakko on 11.11.2016.
 */
public class BlogPost {
    private static final String DEFAULT_TITLE = "Default";

    private String _title;
    private String _trip;
    private DayRange _dayRange;

    private ArrayList<BlogEntry> _entries;
    private Image _mainImage;

    public BlogPost() {
        this(DEFAULT_TITLE, Settings.getTripName().orElse("No Trip"), new DayRange(0, 0), new ArrayList<BlogEntry>(), new Image(null, null));
    }

    public BlogPost(String title, String trip, DayRange dayRange, List<BlogEntry> entries, Image mainImage) {
        _title = title;
        _trip = trip;
        _dayRange = dayRange;

        _entries = new ArrayList<>(entries);
        _mainImage = mainImage;
    }

    public String getTitle() { return _title; }
    public void setTitle(String title) { _title = title; }

    public String getTrip() { return _trip; }
    public void setTrip(String trip) { _trip = trip; }

    public DayRange getDayRange() {  return _dayRange; }

    public List<BlogEntry> entries() {
        return _entries;
    }

    public Image getMainImage() {
        return _mainImage;
    }

    public List<Uri> getAllImageUris() {
        HashSet<Uri> uris = new HashSet<>();
        if(_mainImage.getImageUri() != null) {
            uris.add(_mainImage.getImageUri());
        }

        for(BlogEntry entry : _entries) {
            if(entry instanceof BlogEntry.ImageEntry) {
                BlogEntry.ImageEntry imageEntry = (BlogEntry.ImageEntry)entry;
                if(imageEntry.getImage().getImageUri() != null) {
                    uris.add(imageEntry.getImage().getImageUri());
                }
            }
        }

        return new ArrayList<>(uris);
    }

    public boolean canBeSaved() {
        if(_title == null || _title.trim().length() == 0) {
            return false;
        }

        if(_dayRange.StartDate > _dayRange.EndDate) {
            return false;
        }

        // if(_mainImage.getUri() == null) { return false; }

        return true;
    }
}
