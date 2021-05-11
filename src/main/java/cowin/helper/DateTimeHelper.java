package cowin.helper;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

/**
 * @author sahil.srivastava
 * @since 07/05/21
 */
public class DateTimeHelper {

    private static final String DEFAULT_DATE_FORMAT = "MM/dd/yyyy";

    private DateTimeHelper() {
    }

    public static DateTimeFormatter getDefaultDateTimeFormatter() {
        return DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
    }

    /**
     * Method to get the current date of the local machine in {@link #DEFAULT_DATE_FORMAT}format
     *
     * @return current date
     */
    public static String getLocalCurrentDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
    }

    /**
     * Method to get current date of the local machine in the outputFormat
     *
     * @param outputFormat Output format in which date is required
     * @return Today's date in output format
     */
    public static String getLocalCurrentDate(String outputFormat) {
        return reformatDate(getLocalCurrentDate(), DEFAULT_DATE_FORMAT, outputFormat);
    }

    /**
     * Method to get a date in a specific format
     *
     * @param date         Date whose format os to be changed
     * @param inputFormat  Input format of the date
     * @param outputFormat Required output format of the date
     * @return Date in the outputFormat
     */
    public static String reformatDate(String date, String inputFormat, String outputFormat) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(inputFormat);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outputFormat);
        LocalDate localDate = LocalDate.parse(date, inputFormatter);
        return localDate.format(outputFormatter);
    }

    /**
     * Method to get a date in a specific format when input date format is {@link #DEFAULT_DATE_FORMAT}
     *
     * @param date         Date whose format os to be changed
     * @param outputFormat Required output format of the date
     * @return Date in the outputFormat
     */
    public static String reformatDate(String date, String outputFormat) {
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outputFormat);
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
        return localDate.format(outputFormatter);
    }

    /**
     * Method to add/subtract days to/from a date
     *
     * @param date         Date from which days are to added/subtracted
     * @param inputFormat  Input format of the date
     * @param outputFormat Required output format of the date
     * @param daysToAdd    +ve with days if days are to be added else -ve with days to be subtracted
     * @return New date in the outputFormat after the difference is added/subtracted from the date
     */
    public static String addDaysToDate(String date, String inputFormat, String outputFormat, int daysToAdd) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(inputFormat);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outputFormat);
        LocalDate inputLocalDate = LocalDate.parse(date, inputFormatter);
        return inputLocalDate.plusDays(daysToAdd).format(outputFormatter);
    }

    /**
     * Method to add/subtract days to/from a date where input date and output date format are same
     *
     * @param date      Date from which days are to added/subtracted
     * @param format    Format in which the input date is given. Also, the format of the output date
     * @param daysToAdd +ve with days if days are to be added else -ve with days to be subtracted
     * @return New date after the difference is added/subtracted from the date
     */
    public static String addDaysToDate(String date, String format, int daysToAdd) {
        return addDaysToDate(date, format, format, daysToAdd);
    }

    /**
     * Method to add/subtract days to/from a date where the input date and output date format are {@link #DEFAULT_DATE_FORMAT}
     *
     * @param date      Date from which days are to added/subtracted
     * @param daysToAdd +ve with days if days are to be added else -ve with days to be subtracted
     * @return New date after the difference is added/subtracted from the date
     */
    public static String addDaysToDate(String date, int daysToAdd) {
        return addDaysToDate(date, DEFAULT_DATE_FORMAT, daysToAdd);
    }

    /**
     * Method to get current quarter from the current calender month
     *
     * @return Current quarter from calender month
     */
    public static int getCurrentQuarterNumber() {
        int month = Calendar.getInstance().get(Calendar.MONTH);
        return ((month) / 3 + 1);
    }

    /**
     * Method to get current year from current calendar date
     *
     * @return Current year from calendar date
     */
    public static int getCurrentYear() {
        return (Calendar.getInstance().get(Calendar.YEAR));
    }

    /**
     * Method to get current month from calendar date
     *
     * @return It returns the month's number and starts from 1, so for january it returns 1
     */
    public static int getCurrentMonth() {
        return (Calendar.getInstance().get(Calendar.MONTH)) + 1;
    }

    /**
     * Returns the date of the specified day of a week in the same week as the specified date.
     *
     * @param dayOfWeek DayOfWeek enum value that specify the day of the week to return.
     * @param month     Integer value for month.
     * @param day       Integer value for day of a month.
     * @param year      Integer value for year.
     * @return Date string in MM/dd/yyyy format of Thursday from the same week.
     */
    public static String getDateForDayOfWeek(DayOfWeek dayOfWeek, int month, int day, int year) {
        LocalDate localDate = LocalDate.of(year, month, day);
        int diff = dayOfWeek.getValue() - localDate.getDayOfWeek().getValue();

        return localDate.plusDays(diff).format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
    }

    /**
     * Method to get an object of LocalDateTime from a String format
     *
     * @param dateTime    dateTime in String format
     * @param inputFormat input format of the dateTime
     * @return
     */
    public static LocalDateTime getLocalDateTime(String dateTime, String inputFormat) {
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(inputFormat));
    }
}