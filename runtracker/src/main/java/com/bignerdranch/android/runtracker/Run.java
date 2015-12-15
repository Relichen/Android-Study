package com.bignerdranch.android.runtracker;

import java.util.Date;

/**
 * @author Zhuo
 *         2015/12/15
 */
public class Run {
    private Date mStartDate;

    public Run() {
        mStartDate = new Date();
    }

    public Date getStartDate() {
        return mStartDate;
    }

    public void setStartDate(Date startDate) {
        mStartDate = startDate;
    }

    public int getDurationSeconds(long endMills) {
        return (int) ((endMills - mStartDate.getTime()) / 1000);
    }

    public static String formatDuration(int durationSecond) {
        int seconds = durationSecond % 60;
        int minutes = ((durationSecond - seconds) / 60) % 60;
        int hours = (durationSecond - (minutes * 60) - seconds) / 3600;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
