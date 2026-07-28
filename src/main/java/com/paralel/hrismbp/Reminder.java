package com.paralel.hrismbp; // Sesuaikan dengan nama package projek Anda

public record Reminder(
        int id,
        String title,
        String type,           // "RECURRING_DAYS" atau "MONTHLY_DATE"
        String startDate,
        int intervalDays,
        int monthlyDay,
        int daysBeforeNotice,
        String notes,
        boolean isActive
) {}