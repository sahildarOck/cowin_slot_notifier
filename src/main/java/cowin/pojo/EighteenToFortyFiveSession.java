package cowin.pojo;

/**
 * @author sahil.srivastava
 * @since 06/05/21
 */
public class EighteenToFortyFiveSession {

    private String date;
    private int availableCapacity;

    private EighteenToFortyFiveSession(String date, int availableCapacity) {
        this.date = date;
        this.availableCapacity = availableCapacity;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getAvailableCapacity() {
        return availableCapacity;
    }

    public void setAvailableCapacity(int availableCapacity) {
        this.availableCapacity = availableCapacity;
    }

    public static EighteenToFortyFiveSession getObject(String date, int availableCapacity) {
        return new EighteenToFortyFiveSession(date, availableCapacity);
    }
}
