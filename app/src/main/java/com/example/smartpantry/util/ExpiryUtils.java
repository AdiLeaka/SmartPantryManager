package com.example.smartpantry.util;

import com.example.smartpantry.model.PantryItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Helpers for the optional expiry date (stored as ISO text, yyyy-MM-dd).
 */
public final class ExpiryUtils {

    /** Items expiring within this many days are flagged when alerts are on. */
    public static final int SOON_DAYS = 3;

    private static final long MILLIS_PER_DAY = 24L * 60L * 60L * 1000L;

    private ExpiryUtils() {
    }

    public static String toIso(int year, int monthZeroBased, int day) {
        return String.format(Locale.US, "%04d-%02d-%02d", year, monthZeroBased + 1, day);
    }

    private static Date parse(String iso) {
        if (iso == null || iso.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            format.setLenient(false);
            return format.parse(iso);
        } catch (ParseException e) {
            return null;
        }
    }

    /** "05 Nov 2026" style text for display, or the raw value if it cannot be parsed. */
    public static String toDisplay(String iso) {
        Date date = parse(iso);
        if (date == null) {
            return iso == null ? "" : iso;
        }
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
    }

    /** Whole days from today until the date (negative = already expired), or null if no valid date. */
    public static Long daysUntilExpiry(String iso) {
        Date date = parse(iso);
        if (date == null) {
            return null;
        }
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long diff = date.getTime() - today.getTimeInMillis();
        return Math.round(diff / (double) MILLIS_PER_DAY);
    }

    /** How many items are already expired or expire within SOON_DAYS. */
    public static int countExpiringSoon(List<PantryItem> items) {
        int count = 0;
        for (PantryItem item : items) {
            Long days = daysUntilExpiry(item.getExpiryDate());
            if (days != null && days <= SOON_DAYS) {
                count++;
            }
        }
        return count;
    }
}
