package cowin.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sahil.srivastava
 * @since 06/05/21
 */
public class EighteenToFortyFiveCenter {

    private String centerName;
    private String address;
    private int pinCode;
    private List<EighteenToFortyFiveSession> sessions;

    private EighteenToFortyFiveCenter(String centerName, String address, int pinCode, List<EighteenToFortyFiveSession> sessions) {
        this.centerName = centerName;
        this.address = address;
        this.pinCode = pinCode;
        this.sessions = sessions;
    }

    private EighteenToFortyFiveCenter(String centerName, String address, int pinCode, EighteenToFortyFiveSession... sessions) {
        this.centerName = centerName;
        this.address = address;
        this.pinCode = pinCode;
        this.sessions = new ArrayList<>();
        for (EighteenToFortyFiveSession session : sessions) {
            this.sessions.add(session);
        }
    }

    public String getCenterName() {
        return centerName;
    }

    public void setCenterName(String centerName) {
        this.centerName = centerName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPinCode() {
        return pinCode;
    }

    public void setPinCode(int pinCode) {
        this.pinCode = pinCode;
    }

    public List<EighteenToFortyFiveSession> getSessions() {
        return sessions;
    }

    public void setSessions(List<EighteenToFortyFiveSession> sessions) {
        this.sessions = sessions;
    }

    public static EighteenToFortyFiveCenter getObject(String centerName, String address, int pinCode, List<EighteenToFortyFiveSession> sessions) {
        return new EighteenToFortyFiveCenter(centerName, address, pinCode, sessions);
    }

    public static EighteenToFortyFiveCenter getObject(String centerName, String address, int pinCode, EighteenToFortyFiveSession... sessions) {
        return new EighteenToFortyFiveCenter(centerName, address, pinCode, sessions);
    }
}
