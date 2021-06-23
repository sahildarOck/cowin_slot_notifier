package cowin;

import cowin.enumeration.Vaccine;
import cowin.helper.DateTimeHelper;
import cowin.pojo.*;
import io.restassured.response.Response;

import java.time.LocalDateTime;
import java.util.*;

import static io.restassured.RestAssured.given;
import static java.lang.Thread.sleep;

/**
 * @author sahil.srivastava
 * @since 06/05/21
 */
public class VaccineSlotNotifier2 {

    /******** Fields for CO-WIN API ********/
    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String USER_AGENT_HEADER_VALUE = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36";
    private static final String DISTRICT_PARAM = "district_id";
    private static List<District> districts;
    private static final String DATE_PARAM = "date";
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String FIND_BY_DISTRICT_URL_PUBLIC = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByDistrict";
    private static final String FIND_BY_DISTRICT_URL_PROTECTED = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/calendarByDistrict";
    private static final String COWIN_BEARER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJmOThlYWYzNi1lNjA2LTQyYjItYTlkYy1hYWQzMzM2NTRjNTgiLCJ1c2VyX2lkIjoiZjk4ZWFmMzYtZTYwNi00MmIyLWE5ZGMtYWFkMzMzNjU0YzU4IiwidXNlcl90eXBlIjoiQkVORUZJQ0lBUlkiLCJtb2JpbGVfbnVtYmVyIjo5NTkxMzUwMDk5LCJiZW5lZmljaWFyeV9yZWZlcmVuY2VfaWQiOjMzNTcyNjUxNjQ1MjEwLCJzZWNyZXRfa2V5IjoiYjVjYWIxNjctNzk3Ny00ZGYxLTgwMjctYTYzYWExNDRmMDRlIiwic291cmNlIjoiY293aW4iLCJ1YSI6Ik1vemlsbGEvNS4wIChNYWNpbnRvc2g7IEludGVsIE1hYyBPUyBYIDEwXzE1XzcpIEFwcGxlV2ViS2l0LzUzNy4zNiAoS0hUTUwsIGxpa2UgR2Vja28pIENocm9tZS85MS4wLjQ0NzIuNzcgU2FmYXJpLzUzNy4zNiIsImRhdGVfbW9kaWZpZWQiOiIyMDIxLTA2LTAzVDEzOjAwOjIzLjk5N1oiLCJpYXQiOjE2MjI3MjUyMjMsImV4cCI6MTYyMjcyNjEyM30.qsIizAG9H2iUPputJf0em1cpaUUMg6gh63sVRggN9K4";

    /******** Fields for Slack API ********/
    private static final String BOT_BEARER_TOKEN = "";
    private static final String BOT_NAME = "automation-reporter";
    private static final String SLACK_POST_API_URL = "https://slack.com/api/chat.postMessage";

    /******** Fields for Telegram API ********/
    private static final String TELEGRAM_BOT_TOKEN = "";
    private static final String TELEGRAM_POST_API_URL = "https://api.telegram.org/" + TELEGRAM_BOT_TOKEN + "/sendMessage?parse_mode=html";

    /******** Other fields ********/
    private static final long DEFAULT_WAIT_TIME = 7000; // 7 secs
    private static final long SLACK_POST_NON_AVAILABILITY_INTERVAL = 6 * 60 * 60 * 1000;
    private static final long SLACK_POST_AVAILABILITY_INTERVAL = 10 * 60 * 1000;

    private boolean isDataCached;

    public static void main(String[] args) throws InterruptedException {
        VaccineSlotNotifier2 vaccineSlotNotifier = new VaccineSlotNotifier2();

        vaccineSlotNotifier.setUp();
        vaccineSlotNotifier.doIt();
    }

    private void setUp() {
        districts = new ArrayList<>();
        districts.add(District.getObject(696, "Varanasi", new String[]{}, new String[]{"-1001373442340"}));
//        districts.add(District.getObject(654, "Gorakhpur", new String[]{}, new String[]{"-1001331674930"}));

//        districts.add(District.getObject(470, "Nabarangpur", new String[]{"whitehat_scan"}, new String[]{}));
//        districts.add(District.getObject(670, "Lucknow", new String[]{}, new String[]{"-1001289521803"}));

//        districts.add(District.getObject(294, "BBMP", new String[]{"vaccine_slots_bangalore"}, new String[]{"-505429961"}));
//        districts.add(District.getObject(276, "Bangalore Rural", new String[]{"vaccine_slots_bangalore"}, new String[]{"-505429961"}));
//        districts.add(District.getObject(265, "Bangalore Urban", new String[]{"vaccine_slots_bangalore"}, new String[]{"-505429961"}));

//        districts.add(District.getObject(641, "Chandauli", new String[]{"vaccine_slots_temp"}, new String[]{"-1001215233480"}));
//        districts.add(District.getObject(676, "Meerut", new String[]{"vaccine_slots_temp"}, new String[]{"-1001223948279"}));
//        districts.add(District.getObject(240, "Ranchi", new String[]{"vaccine_slots_ranchi"}, new String[]{"-1001458395980"}));
    }

    private void doIt() throws InterruptedException {
        while (true) {
            for (District district : districts) {
                String currentTime = getCurrentTime();
                Map<String, EighteenToFortyFiveCenter> eighteenToFortyFiveCenters = getEighteenToFortyFiveCentersWithAvailableSlots(district, currentTime);
                if (!eighteenToFortyFiveCenters.isEmpty()) {
                    System.out.println("Yayy! Slots available in district: " + district.getDistrictName() + " Checked at time: " + currentTime);
                    String slackTextWhenSlotFound = getSlackTextWhenSlotFound(eighteenToFortyFiveCenters, district);
                    if (!district.isOnceNotifiedForAvailability() || !district.isSlotAvailableOnLastRequest() ||
                            (slackTextWhenSlotFound.equals(district.getLastSlackTextSentOnAvailability()) && System.currentTimeMillis() - district.getLastNotifiedForAvailabilityTime() >= SLACK_POST_AVAILABILITY_INTERVAL) ||
                            !slackTextWhenSlotFound.equals(district.getLastSlackTextSentOnAvailability())) {
                        sendToSlack(slackTextWhenSlotFound, district);
                        sendToTelegram(getTelegramTextWhenSlotFound(eighteenToFortyFiveCenters, district), district);
                        district.setLastNotifiedForAvailabilityTime(System.currentTimeMillis());
                        district.setOnceNotifiedForAvailability(true);
                        district.setLastSlackTextSentOnAvailability(slackTextWhenSlotFound);
                        district.setSlotAvailableOnLastRequest(true);
                    }
                } else {
                    System.out.println("No slots available in district: " + district.getDistrictName() + " for the next 1 week. Checked at time: " + currentTime);
                    if (!district.isOnceNotifiedForNonAvailability() || district.isSlotAvailableOnLastRequest() || System.currentTimeMillis() - district.getLastNotifiedForNonAvailabilityTime() >= SLACK_POST_NON_AVAILABILITY_INTERVAL) {
                        sendToSlack(getSlackTextWhenSlotNotFound(district), district);
                        sendToTelegram(getTelegramTextWhenSlotNotFound(district), district);
                        district.setLastNotifiedForNonAvailabilityTime(System.currentTimeMillis());
                        district.setOnceNotifiedForNonAvailability(true);
                        district.setSlotAvailableOnLastRequest(false);
                    }
                }
            }

            System.out.println("Waiting for " + DEFAULT_WAIT_TIME / 1000 + " seconds...!!!\n--------------------------------------------------------------\n\n");
            try {
                sleep(DEFAULT_WAIT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Map<String, EighteenToFortyFiveCenter> getEighteenToFortyFiveCentersWithAvailableSlots(District district, String currentTime) throws InterruptedException {
        Map<String, EighteenToFortyFiveCenter> eighteenToFortyFiveCenters = new HashMap<>();

        for (int i = 0; i < 1; i += 7) {
            String date = DateTimeHelper.addDaysToDate(DateTimeHelper.getLocalCurrentDate(DATE_FORMAT), DATE_FORMAT, i);

            System.out.println("Sending GET request for district: " + district.getDistrictName() + " and date: " + date + " at time: " + currentTime);

            Response response;
//            int tries = 0;
//            do {
//                isDataCached = false;
//                response = given().header(USER_AGENT_HEADER, USER_AGENT_HEADER_VALUE)
//                        .param(DISTRICT_PARAM, district.getDistrictId())
//                        .param(DATE_PARAM, date)
//                        .param("Authorization", COWIN_BEARER_TOKEN)
//                        .get(FIND_BY_DISTRICT_URL_PROTECTED);
////                tries++;
////                System.out.println("Number of tries: " + tries);
//                if (response.getStatusCode() == 401) {
////                    if (tries < 2) {
////                        sleep(5000); // To avoid unauthenticated access on next run
////                    } else {
//                    isDataCached = true;
//                    System.out.println("Data is cached...!!!");
//                    response = given().header(USER_AGENT_HEADER, USER_AGENT_HEADER_VALUE)
//                            .param(DISTRICT_PARAM, district.getDistrictId())
//                            .param(DATE_PARAM, date)
//                            .get(FIND_BY_DISTRICT_URL_PUBLIC);
////                    }
//                }
//            }
//            while (response.getStatusCode() == 401);

            isDataCached = true;
            System.out.println("Data is cached...!!!");
            response = given().header(USER_AGENT_HEADER, USER_AGENT_HEADER_VALUE)
                    .param(DISTRICT_PARAM, district.getDistrictId())
                    .param(DATE_PARAM, date)
                    .get(FIND_BY_DISTRICT_URL_PUBLIC);

            if (response.getStatusCode() == 200) {
                List<Center> centerList = response.jsonPath().getList("centers", Center.class);

                for (Center center : centerList) {
                    for (Session session : center.getSessions()) {
                        if (session.getMin_age_limit() < 45 && session.getAvailable_capacity_dose1() > 0 && session.getVaccine().equals(Vaccine.COVAXIN.toString())) { // TODO: Generalise this - temp change for Vikas
                            EighteenToFortyFiveSession eighteenToFortyFiveSession = EighteenToFortyFiveSession.getObject(session.getDate(), session.getAvailable_capacity_dose1(), session.getAvailable_capacity_dose2(), session.getVaccine());
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

    private String getSlackTextWhenSlotFound
            (Map<String, EighteenToFortyFiveCenter> eighteenToFortyFiveCenters, District district) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("<!channel> `" + (isDataCached ? "CACHED DATA" : "REAL-TIME DATA") + "` Vaccine slots available now at the following centres in `" + district.getDistrictName() + "` for the next 1 week.\n");
        stringBuilder.append("Please visit <https://selfregistration.cowin.gov.in|CoWIN Portal> now to book the slots...!!!\n");
        int index = 1;
        for (EighteenToFortyFiveCenter eighteenToFortyFiveCenter : eighteenToFortyFiveCenters.values()) {
            stringBuilder.append(index++ + ". *Center*: _" + eighteenToFortyFiveCenter.getCenterName() + " - ");
            stringBuilder.append(eighteenToFortyFiveCenter.getAddress() + " - " + eighteenToFortyFiveCenter.getPinCode() + "_\n");
            for (EighteenToFortyFiveSession eighteenToFortyFiveSession : eighteenToFortyFiveCenter.getSessions()) {
                String slotsText = eighteenToFortyFiveSession.getAvailable_capacity_dose1() > 1 ? "slots" : "slot";
                stringBuilder.append("\t\t• " + eighteenToFortyFiveSession.getDate() + " - " + eighteenToFortyFiveSession.getAvailable_capacity_dose1() + " " + slotsText + " - " + eighteenToFortyFiveSession.getVaccine() + "\n");
            }
            stringBuilder.append("\n\n");
        }

        return stringBuilder.toString();
    }

    private List<String> getTelegramTextWhenSlotFound
            (Map<String, EighteenToFortyFiveCenter> eighteenToFortyFiveCenters, District district) {
        if (district.getTelegramChannelIDs().length > 0) {
            List<String> texts = new ArrayList<>();
            int totalCentres = eighteenToFortyFiveCenters.size();
            Iterator itr = eighteenToFortyFiveCenters.values().iterator();
            for (int i = 0; i < totalCentres; i += 10) {

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("<code>" + (isDataCached ? "CACHED DATA" : "REAL-TIME DATA") + "</code>\n\n");
                stringBuilder.append("Vaccine slots ✅ <b><u>available</u></b> now at the following centres in <b><u>" + district.getDistrictName() + "</u></b> for the next 1 week.\n");
                stringBuilder.append("Please visit <a href=\\\"https://selfregistration.cowin.gov.in\\\">CoWIN Portal</a> now to book the slots...!!!\n\n");
                int index = 1;
                for (int j = 0; j < 10 && itr.hasNext(); j++) {
                    EighteenToFortyFiveCenter eighteenToFortyFiveCenter = (EighteenToFortyFiveCenter) itr.next();
                    stringBuilder.append(index++ + ". <b>Center</b>: <i>" + eighteenToFortyFiveCenter.getCenterName() + " - ");
                    stringBuilder.append(eighteenToFortyFiveCenter.getAddress() + " - " + eighteenToFortyFiveCenter.getPinCode() + "</i>\n");
                    for (EighteenToFortyFiveSession eighteenToFortyFiveSession : eighteenToFortyFiveCenter.getSessions()) {
                        String slotsText = eighteenToFortyFiveSession.getAvailable_capacity_dose1() > 1 ? "slots" : "slot";
                        stringBuilder.append("\t\t• " + eighteenToFortyFiveSession.getDate() + " - " + eighteenToFortyFiveSession.getAvailable_capacity_dose1() + " " + slotsText + " - " + eighteenToFortyFiveSession.getVaccine() + "\n");
                    }
                    stringBuilder.append("\n\n");
                }
                texts.add(stringBuilder.toString());
            }

            return texts;
        } else {
            return null;
        }
    }

    private String getSlackTextWhenSlotNotFound(District district) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("`" + (isDataCached ? "CACHED DATA" : "REAL-TIME DATA") + "`");
        stringBuilder.append(" No vaccine slots available in `" + district.getDistrictName() + "` district for the next 1 week..!!\n");
        stringBuilder.append(">_Checking for slots every 7 seconds_\n");
        stringBuilder.append(">_Notifying immediately if found, else, every 6 hours for non-availability_");

        return stringBuilder.toString();
    }

    private String getTelegramTextWhenSlotNotFound(District district) {
        if (district.getTelegramChannelIDs().length > 0) {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("<code>" + (isDataCached ? "CACHED DATA" : "REAL-TIME DATA") + "</code>\n\n");
            stringBuilder.append("❌ <b><u>NO</u></b> vaccine slots available in <b><u>" + district.getDistrictName() + "</u></b> district for the next 1 week..!!\n\n");
            stringBuilder.append("<i>Checking for slots every 7 seconds\n\n");
            stringBuilder.append("Notifying immediately if found, else, every 6 hours for non-availability</i>");

            return stringBuilder.toString();
        } else {
            return "";
        }
    }

    private void sendToSlack(String text, District district) {
        for (String channel : district.getSlackChannels()) {
            String payLoad = "{\"channel\" : \"" + channel.trim() + "\", \"text\": \"" + text + "\", \"as_user\": true}";
            Response response = given().header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + BOT_BEARER_TOKEN)
                    .and().body(payLoad)
                    .and().response().statusCode(200)
                    .when().post(SLACK_POST_API_URL);

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

    private void sendToTelegram(String text, District district) {
        sendToTelegram(new ArrayList<>(Arrays.asList(text)), district);
    }

    private void sendToTelegram(List<String> texts, District district) {
        try {
            sleep(1000); // Added to avoid API hit limit
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (String id : district.getTelegramChannelIDs()) {
            for (String text : texts) {
                String payLoad = "{\"chat_id\" : \"" + id + "\", \"text\": \"" + text + "\"}";

                Response response = given().header("Content-Type", "application/json")
                        .and().body(payLoad)
                        .post(TELEGRAM_POST_API_URL);

                if (response.statusCode() == 429) {
                    System.out.println("Error code 429 occurred...!!! Not sending the telegram message\n");
                    return;
                }

                if (response.statusCode() == 400 && response.jsonPath().getString("description").contains("supergroup chat")) {
                    int newChatId = response.jsonPath().getInt("parameters.migrate_to_chat_id");
                    payLoad = "{\"chat_id\" : \"" + newChatId + "\", \"text\": \"" + text + "\"}";

                    System.out.println("Supergroup chat occurred...!!! Sending the telegram message with new Chat Id: " + newChatId + "\n");

                    response = given().header("Content-Type", "application/json")
                            .and().body(payLoad)
                            .post(TELEGRAM_POST_API_URL);
                }

                if (!response.jsonPath().getBoolean("ok")) {
                    System.out.println("Error while sending the telegram message...!!!");
                    System.out.println("Printing payLoad:\n" + payLoad);
                    System.out.println("\n\nPrinting response:\n");
                    response.prettyPrint();
                    throw new RuntimeException("Error sending Telegram message...!!!");
                }
            }
        }
    }

    private String getCurrentTime() {
        LocalDateTime nowTime = LocalDateTime.now();
        return nowTime.getHour() + ":" + nowTime.getMinute() + ":" + nowTime.getSecond();
    }
}
