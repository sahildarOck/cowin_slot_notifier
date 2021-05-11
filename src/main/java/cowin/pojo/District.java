package cowin.pojo;

/**
 * @author sahil.srivastava
 * @since 06/05/21
 */
public class District {

    private int districtId;
    private String districtName;
    private String[] channels;
    private long lastSlackNotifiedForNonAvailabilityTime;
    private long lastSlackNotifiedForAvailabilityTime;
    private boolean slackOnceNotifiedForNonAvailability;
    private boolean slackOnceNotifiedForAvailability;
    private String lastSlackTextSentOnAvailability;
    private boolean slotAvailableOnLastRequest;

    private District(int districtId, String districtName, String... channels) {
        this.districtId = districtId;
        this.districtName = districtName;
        this.channels = channels;
    }

    public int getDistrictId() {
        return districtId;
    }

    public void setDistrictId(int districId) {
        this.districtId = districId;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public static District getObject(int districtId, String districtName, String... channels) {
        if (channels.length == 0) {
            throw new RuntimeException("No channels passed while instantating object for district: " + districtName);
        }
        return new District(districtId, districtName, channels);
    }

    public String[] getChannels() {
        return channels;
    }

    public void setChannels(String[] channels) {
        this.channels = channels;
    }

    public long getLastSlackNotifiedForNonAvailabilityTime() {
        return lastSlackNotifiedForNonAvailabilityTime;
    }

    public void setLastSlackNotifiedForNonAvailabilityTime(long lastSlackNotifiedForNonAvailabilityTime) {
        this.lastSlackNotifiedForNonAvailabilityTime = lastSlackNotifiedForNonAvailabilityTime;
    }

    public long getLastSlackNotifiedForAvailabilityTime() {
        return lastSlackNotifiedForAvailabilityTime;
    }

    public void setLastSlackNotifiedForAvailabilityTime(long lastSlackNotifiedForAvailabilityTime) {
        this.lastSlackNotifiedForAvailabilityTime = lastSlackNotifiedForAvailabilityTime;
    }

    public boolean isSlackOnceNotifiedForNonAvailability() {
        return slackOnceNotifiedForNonAvailability;
    }

    public void setSlackOnceNotifiedForNonAvailability(boolean slackOnceNotifiedForNonAvailability) {
        this.slackOnceNotifiedForNonAvailability = slackOnceNotifiedForNonAvailability;
    }

    public boolean isSlackOnceNotifiedForAvailability() {
        return slackOnceNotifiedForAvailability;
    }

    public void setSlackOnceNotifiedForAvailability(boolean slackOnceNotifiedForAvailability) {
        this.slackOnceNotifiedForAvailability = slackOnceNotifiedForAvailability;
    }

    public String getLastSlackTextSentOnAvailability() {
        return lastSlackTextSentOnAvailability;
    }

    public void setLastSlackTextSentOnAvailability(String lastSlackTextSentOnAvailability) {
        this.lastSlackTextSentOnAvailability = lastSlackTextSentOnAvailability;
    }

    public boolean isSlotAvailableOnLastRequest() {
        return slotAvailableOnLastRequest;
    }

    public void setSlotAvailableOnLastRequest(boolean slotAvailableOnLastRequest) {
        this.slotAvailableOnLastRequest = slotAvailableOnLastRequest;
    }
}
