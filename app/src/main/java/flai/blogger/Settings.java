package flai.blogger;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.Calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

/**
 * Created by Jaakko on 27.10.2017.
 */
public class Settings {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private static SharedPreferences getPreferences() {
        return BloggerApplication.getAppContext().getSharedPreferences("preferences", Context.MODE_PRIVATE);
    }

    public static Optional<String> getTripName() {
        return Optional.ofNullable(getPreferences().getString("tripName", null));
    }

    public static void setTripName(String value) {
        SharedPreferences.Editor preferenceEditor = getPreferences().edit();

        preferenceEditor.putString("tripName", value);
        preferenceEditor.commit();
    }

    public static Optional<String> getTripStartDateString() {
        return getTripStartDate().map(d -> dateFormat.format(d));
    }

    public static Optional<Date> getTripStartDate() {
        Optional<String> saved = Optional.ofNullable(getPreferences().getString("tripStartDate", null));
        return saved.map(Settings::parseDate).orElse(Optional.empty());
    }

    public static void setTripStartDate(String dateAsStr) {
        SharedPreferences.Editor preferenceEditor = getPreferences().edit();

        Optional<Date> parsed = parseDate(dateAsStr);
        if(parsed.isPresent()) {
            preferenceEditor.putString("tripStartDate", dateFormat.format(parsed.get()));
            preferenceEditor.commit();
        }
    }

    private static Optional<Date> parseDate(String str) {
        try {
            return Optional.of(dateFormat.parse(str));
        }
        catch (ParseException e) {
        }

        return Optional.empty();
    }
}
