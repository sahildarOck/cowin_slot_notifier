package cowin.enumeration;

/**
 * @author sahil.srivastava
 * @since 03/06/21
 */
public enum Vaccine {

    COVAXIN("COVAXIN"),
    COVISHIELD("COVISHIELD");

    private String str;

    Vaccine(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }
}
