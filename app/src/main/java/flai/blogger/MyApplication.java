package flai.blogger;

import android.app.Application;
import android.content.Context;

/**
 * Created by Jaakko on 06.04.2016.
 */
public class MyApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}