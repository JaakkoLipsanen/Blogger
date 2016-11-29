package flai.blogger.helpers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import flai.blogger.BloggerApplication;

/**
 * Created by Jaakko on 29.11.2016.
 */
public class DialogHelper {
    public static void showErrorDialog(String errorText) {
        Log.e("blogger", errorText);
        new AlertDialog.Builder(BloggerApplication.getAppContext())
                .setTitle("ERROR")
                .setMessage(errorText)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static void showErrorDialog(String errorText, Exception e) {
        DialogHelper.showErrorDialog(errorText + "\n" + Log.getStackTraceString(e));
    }
}
