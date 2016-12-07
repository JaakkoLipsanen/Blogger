package flai.blogger.helpers;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

/**
 * Created by Jaakko on 21.11.2016.
 */
public class IntentHelper {

    private static final String GooglePhotosPackageName = "com.google.android.apps.photos";
    public static void showImagePicker(Activity parent, int requestID, boolean allowMultipleImages) {
        String packageHandler = null;
        if(isPackageInstalled(parent, GooglePhotosPackageName)) {
            packageHandler = GooglePhotosPackageName;
        }

        createIntent(parent, requestID, "image/*", "Select Image", allowMultipleImages, packageHandler);
    }

    public static void showZipPicker(Activity parent, int requestID) {
        createIntent(parent, requestID, "application/zip", "Select Zip", false, null);
    }

    private static void createIntent(Activity parent, int requestID, String type, String displayName, boolean allowMultiple, String forcedHandler) {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType(type);

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType(type);

        Intent chooserIntent = Intent.createChooser(getIntent, displayName);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        if(allowMultiple) {
            getIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }

        // so yeah, you can give package name (like  "com.google.android.apps.photos" and
        // if that package is a choice for the intent, then use that automatically
        if(forcedHandler != null) {
            setComponentToPackageIfFound(parent, pickIntent, forcedHandler); // okay.. this doesnt actually work....
        }


        parent.startActivityForResult(chooserIntent, requestID);
    }

    private static boolean isPackageInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            return packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES) != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static void setComponentToPackageIfFound(Activity activity, Intent intent, String packageName) {
        List<ResolveInfo> resolveInfoList = activity.getPackageManager().queryIntentActivities(intent, 0);
        for (int i = 0; i < resolveInfoList.size(); i++) {
            ResolveInfo info = resolveInfoList.get(i);
            if(info != null && info.activityInfo.packageName.equals(packageName)) {
                intent.setComponent(new ComponentName(packageName, info.activityInfo.name));
                return;
            }
        }
    }
}
