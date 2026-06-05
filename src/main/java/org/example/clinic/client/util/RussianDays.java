package org.example.clinic.client.util;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;


public final class RussianDays {

    private static final Locale RU = new Locale("ru");

    private RussianDays() {
    }

    public static String display(DayOfWeek day) {
        if (day == null) {
            return "";
        }
        String name = day.getDisplayName(TextStyle.FULL_STANDALONE, RU);
        if (name.isEmpty()) {
            return name;
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
