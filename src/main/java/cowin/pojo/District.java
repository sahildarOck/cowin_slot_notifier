package cowin.pojo;

/**
 * @author sahil.srivastava
 * @since 06/05/21
 */
public class District {

    private int districtId;
    private String districtName;
    private String[] slackChannels;
    private String[] telegramChannelIDs;
    private long lastNotifiedForNonAvailabilityTime;
    private long lastNotifiedForAvailabilityTime;
    private boolean onceNotifiedForNonAvailability;
    private boolean onceNotifiedForAvailability;
    private String lastSlackTextSentOnAvailability;
    private boolean slotAvailableOnLastRequest;
    private boolean covaxin;
    private boolean covishield;

    private District(int districtId, String districtName, String[] slackChannels, String[] telegramChannelIDs) {
        this.districtId = districtId;
        this.districtName = districtName;
        this.slackChannels = slackChannels;
        this.telegramChannelIDs = telegramChannelIDs;
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

    public static District getObject(int districtId, String districtName, String[] slackChannels, String[] telegramChannelIDs) {
//        if (slackChannels.length == 0) {
//            throw new RuntimeException("No slackChannels passed while instantating object for district: " + districtName);
//        }
        return new District(districtId, districtName, slackChannels, telegramChannelIDs);
    }

    public String[] getSlackChannels() {
        return slackChannels;
    }

    public void setSlackChannels(String[] slackChannels) {
        this.slackChannels = slackChannels;
    }

    public String[] getTelegramChannelIDs() {
        return telegramChannelIDs;
    }

    public void setTelegramChannelIDs(String[] telegramChannelIDs) {
        this.telegramChannelIDs = telegramChannelIDs;
    }

    public long getLastNotifiedForNonAvailabilityTime() {
        return lastNotifiedForNonAvailabilityTime;
    }

    public void setLastNotifiedForNonAvailabilityTime(long lastNotifiedForNonAvailabilityTime) {
        this.lastNotifiedForNonAvailabilityTime = lastNotifiedForNonAvailabilityTime;
    }

    public long getLastNotifiedForAvailabilityTime() {
        return lastNotifiedForAvailabilityTime;
    }

    public void setLastNotifiedForAvailabilityTime(long lastNotifiedForAvailabilityTime) {
        this.lastNotifiedForAvailabilityTime = lastNotifiedForAvailabilityTime;
    }

    public boolean isOnceNotifiedForNonAvailability() {
        return onceNotifiedForNonAvailability;
    }

    public void setOnceNotifiedForNonAvailability(boolean onceNotifiedForNonAvailability) {
        this.onceNotifiedForNonAvailability = onceNotifiedForNonAvailability;
    }

    public boolean isOnceNotifiedForAvailability() {
        return onceNotifiedForAvailability;
    }

    public void setOnceNotifiedForAvailability(boolean onceNotifiedForAvailability) {
        this.onceNotifiedForAvailability = onceNotifiedForAvailability;
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
