package cowin;

import cowin.helper.DateTimeHelper;
import cowin.pojo.*;
import io.restassured.response.Response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static java.lang.Thread.sleep;

/**
 * @author sahil.srivastava
 * @since 06/05/21
 */
public class VaccineSlotNotifier2 {

    /******** Fields for CO-WIN API ********/
    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String USER_AGENT_HEADER_VALUE = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36";
    private static final String DISTRICT_PARAM = "district_id";
    private static List<District> districts;
    private static final String DATE_PARAM = "date";
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String FIND_BY_DISTRICT_URL = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByDistrict";

    /******** Fields for Slack API ********/
    private static final String BOT_BEARER_TOKEN = "";
    private static final String BOT_NAME = "automation-reporter";
    private static final String POST_API_URL = "https://slack.com/api/chat.postMessage";

    /******** Other fields ********/
    private static final long DEFAULT_WAIT_TIME = 40000; // 40 secs
    private static final long SLACK_POST_NON_AVAILABILITY_INTERVAL = 4 * 60 * 60 * 1000;
    private static final long SLACK_POST_AVAILABILITY_INTERVAL = 10 * 60 * 1000;

    public static void main(String[] args) {
        VaccineSlotNotifier2 vaccineSlotNotifier = new VaccineSlotNotifier2();

        vaccineSlotNotifier.setUp();
        vaccineSlotNotifier.doIt();
    }

    private void setUp() {
        districts = new ArrayList<>();
        districts.add(District.getObject(294, "BBMP", "vaccine_slots_bangalore"));
        districts.add(District.getObject(276, "Bangalore Rural", "vaccine_slots_bangalore"));
        districts.add(District.getObject(265, "Bangalore Urban", "vaccine_slots_bangalore"));
        districts.add(District.getObject(641, "Chandauli", "vaccine_slots_temp"));
        districts.add(District.getObject(696, "Varanasi", "vaccine_slots_temp"));
        districts.add(District.getObject(676, "Meerut", "vaccine_slots_temp"));
        districts.add(District.getObject(470, "Nabarangpur", "whitehat_scan"));
    }

    private void doIt() {
        while (true) {
            for (District district : districts) {
                String currentTime = getCurrentTime();
                Map<String, EighteenToFortyFiveCenter> eighteenToFortyFiveCenters = getEighteenToFortyFiveCentersWithAvailableSlots(district, currentTime);
                if (!eighteenToFortyFiveCenters.isEmpty()) {
                    System.out.println("Yayy! Slots available in district: " + district.getDistrictName() + "Checked at time: " + currentTime);
                    String slackTextWhenSlotFound = getSlackTextWhenSlotFound(eighteenToFortyFiveCenters, district);
                    if (!district.isSlackOnceNotifiedForAvailability() || !district.isSlotAvailableOnLastRequest() ||
                            (slackTextWhenSlotFound.equals(district.getLastSlackTextSentOnAvailability()) && System.currentTimeMillis() - district.getLastSlackNotifiedForAvailabilityTime() >= SLACK_POST_AVAILABILITY_INTERVAL) ||
                            !slackTextWhenSlotFound.equals(district.getLastSlackTextSentOnAvailability())) {
                        sendToSlack(slackTextWhenSlotFound, district);
                        district.setLastSlackNotifiedForAvailabilityTime(System.currentTimeMillis());
                        district.setSlackOnceNotifiedForAvailability(true);
                        district.setLastSlackTextSentOnAvailability(slackTextWhenSlotFound);
                        district.setSlotAvailableOnLastRequest(true);
                    }
                } else {
                    System.out.println("No slots available in district: " + district.getDistrictName() + " for the next 2 weeks. Checked at time: " + currentTime);
                    if (!district.isSlackOnceNotifiedForNonAvailability() || district.isSlotAvailableOnLastRequest() || System.currentTimeMillis() - district.getLastSlackNotifiedForNonAvailabilityTime() >= SLACK_POST_NON_AVAILABILITY_INTERVAL) {
                        sendToSlack(getSlackTextWhenSlotNotFound(district), district);
                        district.setLastSlackNotifiedForNonAvailabilityTime(System.currentTimeMillis());
                        district.setSlackOnceNotifiedForNonAvailability(true);
                        district.setSlotAvailableOnLastRequest(false);
                    }
                }
            }

            System.out.println("Waiting for " + DEFAULT_WAIT_TIME / 1000 + " seconds...!!!");
            try {
                sleep(DEFAULT_WAIT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Map<String, EighteenToFortyFiveCenter> getEighteenToFortyFiveCentersWithAvailableSlots(District district, String currentTime) {
        Map<String, EighteenToFortyFiveCenter> eighteenToFortyFiveCenters = new HashMap<>();

        for (int i = 0; i < 8; i += 7) {
            String date = DateTimeHelper.addDaysToDate(DateTimeHelper.getLocalCurrentDate(DATE_FORMAT), DATE_FORMAT, i);

            System.out.println("Sending GET request for district: " + district.getDistrictName() + " and date: " + date + " at time: " + currentTime);

            Response response = given().header(USER_AGENT_HEADER, USER_AGENT_HEADER_VALUE)
                    .param(DISTRICT_PARAM, district.getDistrictId())
                    .param(DATE_PARAM, date)
                    .get(FIND_BY_DISTRICT_URL);

            if (response.getStatusCode() == 200) {
                List<Center> centerList = response.jsonPath().getList("centers", Center.class);

                for (Center center : centerList) {
                    for (Session session : center.getSessions()) {
                        if (session.getMin_age_limit() < 45 && session.getAvailable_capacity() > 0) {
                            EighteenToFortyFiveSession eighteenToFortyFiveSession = EighteenToFortyFiveSession.getObject(session.getDate(), session.getAvailable_capacity());
                            if (eighteenToFortyFiveCenters.containsKey(center.getName())) {
                                eighteenToFortyFiveCenters.get(center.getName()).getSessions().add(eighteenToFortyFiveSession);
                            } else {
                                eighteenToFortyFiveCenters.put(center.getName(), EighteenToFortyFiveCenter.getObject(center.getName(), center.getAddress(), center.getPincode(), eighteenToFortyFiveSession));
                            }
                        }
                    }
                }
            } else {
                System.out.println("Error in request for district: " + district.getDistrictName() + " and date: " + date + " at time: " + currentTime + "\n" + response.getBody().asString());
            }
        }
        return eighteenToFortyFiveCenters;
    }

    private String getSlackTextWhenSlotFound(Map<String, EighteenToFortyFiveCenter> eighteenToFortyFiveCenters, District district) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("<!channel> Hey guys! Vaccine slots available now for 18-45 age group at the following centres in `" + district.getDistrictName() + "` for the next 2 weeks.\n");
        stringBuilder.append("Please visit: <https://www.cowin.gov.in/|CoWIN Portal> now to book the slots...!!!\n");
        int index = 1;
        for (EighteenToFortyFiveCenter eighteenToFortyFiveCenter : eighteenToFortyFiveCenters.values()) {
            stringBuilder.append(index++ + ". *Center*: _" + eighteenToFortyFiveCenter.getCenterName() + "_ - _");
            stringBuilder.append(eighteenToFortyFiveCenter.getAddress() + " - " + eighteenToFortyFiveCenter.getPinCode() + "_\n");
            for (EighteenToFortyFiveSession eighteenToFortyFiveSession : eighteenToFortyFiveCenter.getSessions()) {
                String slotsText = eighteenToFortyFiveSession.getAvailableCapacity() > 1 ? "slots" : "slot";
                stringBuilder.append("\t\t• " + eighteenToFortyFiveSession.getDate() + " - " + eighteenToFortyFiveSession.getAvailableCapacity() + " " + slotsText + "\n");
            }
            stringBuilder.append("\n\n");
        }

        return stringBuilder.toString();
    }

    private String getSlackTextWhenSlotNotFound(District district) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Hey guys! No vaccine slots available for 18-45 age group in `" + district.getDistrictName() + "` district for the next 2 weeks..!!\n");
        stringBuilder.append(">_Checking for slots every 40 seconds_\n");
        stringBuilder.append(">_Notifying immediately if found, else, every 4 hours for non-availability_");
        return stringBuilder.toString();
    }

    private void sendToSlack(String text, District district) {
        for (String channel : district.getChannels()) {
            String payLoad = "{\"channel\" : \"" + channel.trim() + "\", \"text\": \"" + text + "\", \"as_user\": true}";
            Response response = given().header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + BOT_BEARER_TOKEN)
                    .and().body(payLoad)
                    .and().response().statusCode(200)
                    .when().post(POST_API_URL);

            if (!response.jsonPath().getBoolean("ok")) {
                if ("not_in_channel".equals(response.jsonPath().getString("error"))) {
                    System.out.println("Could not post to " + channel + " because, " + BOT_NAME + " is not added in the channel...!!!");
                } else if ("channel_not_found".equals(response.jsonPath().getString("error"))) {
                    System.out.println("Could not post to " + channel + " because, either " + BOT_NAME + " is not added in the channel or the channel does not exist...!!!");
                } else if ("is_archived".equals(response.jsonPath().getString("error"))) {
                    System.out.println("Could not post to " + channel + " because, it is archived...!!!");
                } else {
                    throw new RuntimeException("Error sending Slack message: " + response.jsonPath().getString("error"));
                }
            }
        }
    }

    private String getCurrentTime() {
        LocalDateTime nowTime = LocalDateTime.now();
        return nowTime.getHour() + ":" + nowTime.getMinute() + ":" + nowTime.getSecond();
    }
}
