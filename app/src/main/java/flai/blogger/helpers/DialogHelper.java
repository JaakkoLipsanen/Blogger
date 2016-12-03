package flai.blogger.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import flai.blogger.BloggerApplication;


/**
 * Created by Jaakko on 29.11.2016.
 */
public class DialogHelper {
    public static void showErrorDialog(Context context, String errorText) {
        Log.e("blogger", errorText);
        new AlertDialog.Builder(context)
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

    public static void showErrorDialog(Context context, String errorText, Exception e) {
        DialogHelper.showErrorDialog(context, errorText + "\n" + Log.getStackTraceString(e));
    }

    public static void showErrorToast(String errorText) {
        Toast.makeText(BloggerApplication.getAppContext(), errorText , Toast.LENGTH_LONG).show();
    }

    public static void showErrorToast(String errorText, Exception e) {
        DialogHelper.showErrorToast(errorText  + "\n" + Log.getStackTraceString(e));
    }
}
