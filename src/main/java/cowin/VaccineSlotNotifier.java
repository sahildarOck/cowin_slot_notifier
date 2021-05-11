package cowin;

import cowin.helper.DateTimeHelper;
import cowin.pojo.*;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class VaccineSlotNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(VaccineSlotNotifier.class);

    /******** Fields for CO-WIN API ********/
    private static String USER_AGENT_HEADER = "User-Agent";
    private static String USER_AGENT_HEADER_VALUE = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36";
    private static String DISTRICT_PARAM = "district_id";
    private static List<District> districts;
    private static String DATE_PARAM = "date";
    private static String DATE_FORMAT = "dd-MM-yyyy";
    private static String FIND_BY_DISTRICT_URL = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByDistrict";

    /******** Fields for Slack API ********/
    private static final String BOT_BEARER_TOKEN = "";
    private static final String BOT_NAME = "automation-reporter";
    private static final String POST_API_URL = "https://slack.com/api/chat.postMessage";

    /******** Other fields ********/
    private static final long DEFAULT_WAIT_TIME = 2 * 60 * 1000;
    private static final long SLACK_POST_INTERVAL = 30 * 60 * 1000;

    public static void main(String[] args) {
        VaccineSlotNotifier vaccineSlotNotifier = new VaccineSlotNotifier();

        vaccineSlotNotifier.setUp();
        vaccineSlotNotifier.doIt();
    }

    private void setUp() {
        districts = new ArrayList<>();
//        districts.add(District.getObject(294, "BBMP", "whitehat_scan", "bangalore_vaccine_slots"));
        districts.add(District.getObject(294, "BBMP", "whitehat_scan"));
//        districts.add(District.getObject(641, "Chandauli", "whitehat_scan"));
    }

    private void doIt() {
        while (true) {
            for (District district : districts) {
                String currentTime = getCurrentTime();
                Map<String, EighteenToFortyFiveCenter> eighteenToFortyFiveCenters = getEighteenToFortyFiveCentersWithAvailableSlots(district, currentTime);
                if (!eighteenToFortyFiveCenters.isEmpty()) {
                    LOGGER.trace("Yayy! Slots available for district: " + district.getDistrictName() + "Checked at time: " + currentTime);
                    sendToSlack(getSlackTextWhenSlotFound(eighteenToFortyFiveCenters, district), district);
                } else {
                    LOGGER.trace("No slots available for district: " + district.getDistrictName() + " for the next 2 weeks. Checked at time: " + currentTime);
                    if (!district.isSlackOnceNotifiedForNonAvailability() || System.currentTimeMillis() - district.getLastSlackNotifiedForNonAvailabilityTime() >= SLACK_POST_INTERVAL) {
                        sendToSlack(getSlackTextWhenSlotNotFound(district), district);
                        district.setLastSlackNotifiedForNonAvailabilityTime(System.currentTimeMillis());
                        district.setSlackOnceNotifiedForNonAvailability(true);
                    }
                }
            }

            LOGGER.trace("Waiting for 2 minutes...!!!");
            try {
                sleep(DEFAULT_WAIT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Map<String, EighteenToFortyFiveCenter> getEighteenToFortyFiveCentersWithAvailableSlots(District district, String currentTime) {
        Map<String, EighteenToFortyFiveCenter> eighteenToFortyFiveCenters = new HashMap<>();

        for (int i = 0; i < 15; i += 7) {
            String date = DateTimeHelper.addDaysToDate(DateTimeHelper.getLocalCurrentDate(DATE_FORMAT), DATE_FORMAT, i);

            LOGGER.trace("Sending GET request for district: " + district.getDistrictName() + " and date: " + date + " at time: " + currentTime);

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
                        } else {
                            continue;
                        }
                    }
                }
            } else {
                LOGGER.error("Error in request for district: " + district.getDistrictName() + " and date: " + date + " at time: " + currentTime + "\n" + response.getBody().asString());
            }
        }
        return eighteenToFortyFiveCenters;
    }

    private String getSlackTextWhenSlotFound(Map<String, EighteenToFortyFiveCenter> eighteenToFortyFiveCenters, District district) {
        StringBuilder stringBuilder = new StringBuilder();
        if (eighteenToFortyFiveCenters == null || eighteenToFortyFiveCenters.size() == 0) { // Condition not expected at present - may be needed in future
            stringBuilder.append("No slots available for district: " + district.getDistrictName());
        } else {
            stringBuilder.append("<!channel> Hey guys! Vaccine slots available now for the following centres in " + district.getDistrictName() + " for the next 3 weeks.\n");
            stringBuilder.append("Please visit: <https://www.cowin.gov.in/|CoWIN Portal> now to book the slots...!!!\n");
            for (EighteenToFortyFiveCenter eighteenToFortyFiveCenter : eighteenToFortyFiveCenters.values()) {
                stringBuilder.append(">Center: " + eighteenToFortyFiveCenter.getCenterName() + " - " + "Pincode - " + eighteenToFortyFiveCenter.getPinCode() + "\n");
                for (EighteenToFortyFiveSession eighteenToFortyFiveSession : eighteenToFortyFiveCenter.getSessions()) {
                    stringBuilder.append(">" + eighteenToFortyFiveSession.getDate() + " - " + eighteenToFortyFiveSession.getAvailableCapacity() + " slot(s)\n");
                }
                stringBuilder.append("\n\n");
            }
        }
        return stringBuilder.toString();
    }

    private String getSlackTextWhenSlotNotFound(District district) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Hey guys! No vaccine slots available for " + district.getDistrictName() + " district for the next 3 weeks..!!\n");
        stringBuilder.append(">_Checking for slots every 2 minutes_\n");
        stringBuilder.append(">_Notifying immediately if found, else, every 30 minutes for non-availability_");
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

            LOGGER.trace("Slack response: {}", response.asString());

            if (!response.jsonPath().getBoolean("ok")) {
                if ("not_in_channel".equals(response.jsonPath().getString("error"))) {
                    LOGGER.error("Could not post to {} because, {} is not added in the channel...!!!", channel, BOT_NAME);
                } else if ("channel_not_found".equals(response.jsonPath().getString("error"))) {
                    LOGGER.error("Could not post to {} because, either {} is not added in the channel or the channel does not exist...!!!", channel, BOT_NAME);
                } else if ("is_archived".equals(response.jsonPath().getString("error"))) {
                    LOGGER.error("Could not post to {} because, it is archived...!!!", channel);
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

    private String getTimeAsString(LocalDateTime localDateTime) {
        return localDateTime.getHour() + ":" + localDateTime.getMinute() + ":" + localDateTime.getSecond();
    }
}
