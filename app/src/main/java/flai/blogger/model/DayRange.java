package flai.blogger.model;

/**
 * Created by Jaakko on 11.11.2016.
 */
public class DayRange {
    public final int StartDate;
    public final int EndDate;

    public DayRange(int startDate, int endDate) {
        assert startDate >= 0 && endDate >= 0 && startDate <= endDate;

        this.StartDate = startDate;
        this.EndDate = endDate;
    }

    @Override
    public String toString() {
        return this.StartDate + "-" + this.EndDate;
    }

    public static DayRange parse(String str) {
        String[] parts = str.split("-");
        if(parts.length == 2) {
            try {
                int start = Integer.parseInt(parts[0]);
                int end = Integer.parseInt(parts[1]);

                return new DayRange(start, end);
            }
            catch(Exception e) { } // invalid format
        }

        return new DayRange(0, 0);
    }
}
