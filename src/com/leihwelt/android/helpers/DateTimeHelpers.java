package com.leihwelt.android.helpers;

import org.joda.time.DateTime;

public class DateTimeHelpers {
    public static boolean isToday(DateTime selected) {
        DateTime now = DateTime.now();
        return selected.getYear() == now.getYear() && selected.getDayOfYear() == now.getDayOfYear();
    }

}
