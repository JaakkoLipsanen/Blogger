package flai.blogger.helpers;

import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by Jaakko on 11.11.2016.
 */
public class UIHelper {
    // sets the entry ListView's height based on how many items there are in the list
    public static boolean setListViewHeightBasedOnItems(ListView listView) {
        // TODO: THIS IS NOT WORKING ATM
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            // int totalItemsHeight = 240 + numberOfItems * 240; // was too small
            int totalItemsHeight = 800 + numberOfItems * 800; // default height

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
}
