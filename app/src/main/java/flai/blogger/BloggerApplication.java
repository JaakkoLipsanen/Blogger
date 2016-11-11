package flai.blogger;

import android.app.Application;
import android.content.Context;

public class BloggerApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        BloggerApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return BloggerApplication.context;
    }
}