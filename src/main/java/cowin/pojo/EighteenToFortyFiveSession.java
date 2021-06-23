package cowin.pojo;

/**
 * @author sahil.srivastava
 * @since 06/05/21
 */
public class EighteenToFortyFiveSession {

    private String date;
    private int available_capacity_dose1;
    private int available_capacity_dose2;
    private String vaccine;

    private EighteenToFortyFiveSession(String date, int available_capacity_dose1, int available_capacity_dose2, String vaccine) {
        this.date = date;
        this.available_capacity_dose1 = available_capacity_dose1;
        this.available_capacity_dose2 = available_capacity_dose2;
        this.vaccine = vaccine;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getAvailable_capacity_dose1() {
        return available_capacity_dose1;
    }

    public void setAvailable_capacity_dose1(int available_capacity_dose1) {
        this.available_capacity_dose1 = available_capacity_dose1;
    }

    public int getAvailable_capacity_dose2() {
        return available_capacity_dose2;
    }

    public void setAvailable_capacity_dose2(int available_capacity_dose2) {
        this.available_capacity_dose2 = available_capacity_dose2;
    }

    public static EighteenToFortyFiveSession getObject(String date, int available_capacity_dose1, int available_capacity_dose2, String vaccine) {
        return new EighteenToFortyFiveSession(date, available_capacity_dose1, available_capacity_dose2, vaccine);
    }

    public String getVaccine() {
        return vaccine;
    }

    public void setVaccine(String vaccine) {
        this.vaccine = vaccine;
    }
}
