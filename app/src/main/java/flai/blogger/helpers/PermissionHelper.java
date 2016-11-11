package flai.blogger.helpers;

import android.app.Activity;
import android.os.Build;

/**
 * Created by Jaakko on 12.11.2016.
 */
public class PermissionHelper {

    // okay, normally you should add onRequestPermissionsResult and check whether the user denied or accepted the permissions
    // and actually, TODO: atm I'm actually using internal storage (since Enviroment.getExternalStorage()) actually returns internal storage on Honor 7 Lite
    public static void requestExternalStoragePermissions(Activity caller) {
        // only if the API is Marshmellow or newer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            caller.requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, 10);
        }
    }
}
