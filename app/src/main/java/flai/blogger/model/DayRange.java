package flai.blogger.model;

/**
 * Created by Jaakko on 11.11.2016.
 */
public class DayRange {
    public int StartDate;
    public int EndDate;

    public DayRange(int startDate, int endDate) {
        this.StartDate = startDate;
        this.EndDate = endDate;
    }

    public boolean isValid() {
        return this.EndDate >= this.StartDate;
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
