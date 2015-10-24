package com.spartansoftwareinc.tipp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

class FormattingUtil {

    private static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    
    static Date parseTIPPDate(String dateString) {
        try {
            SimpleDateFormat df = new SimpleDateFormat(FORMAT);
            return df.parse(dateString);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date string: " + 
                                               dateString);
        }
    }
    
    static String writeTIPPDate(Date date) {
        SimpleDateFormat df = new SimpleDateFormat(FORMAT);
        return df.format(date);
    }
    
    // Check that a given location string satisfies the spec restrictions:
    // - Allowed chars: a-z, A-z, 0-9, underscore/dash/period/space
    // - Should not be prefixed with '/'
    // - length of path including section is < 240 chars
    // - '.' and '..' not present as path components
    private static final Pattern VALID_LOCATION_PATH = Pattern.compile("[a-zA-Z0-9_\\-\\. ]*");
    static boolean validLocationString(TIPPSection section, String location) {
        if ((section.getType().getDefaultName() + "/" + location).length() >= 240) {
            return false; 
        }
        if (location.startsWith("/")) {
            return false;
        }
        String[] parts = location.split("/");
        for (String p : parts) {
            if (!VALID_LOCATION_PATH.matcher(p).matches()) {
                return false;
            }
            if (".".equals(p) || "..".equals(p)) {
                return false;
            }
        }
        return true;
    }
}
