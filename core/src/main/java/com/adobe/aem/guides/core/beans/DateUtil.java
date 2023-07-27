package com.adobe.aem.guides.core.beans;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    private static final String INPUT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String OUTPUT_DATE_FORMAT = "dd MMMM, yyyy";

    public static String formatDate(String inputDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(INPUT_DATE_FORMAT);
            Date date = inputFormat.parse(inputDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat(OUTPUT_DATE_FORMAT);
            return outputFormat.format(date);
        } catch (ParseException e) {
            // Handle the exception if the date parsing fails.
            e.printStackTrace();
        }

        return inputDate; // Return the original date if parsing fails.
    }
}