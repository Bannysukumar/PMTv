package com.smart.pmtv;

public class ScheduleModel {
    private String scheduleId;
    private String programName;
    private String hostName;
    private String timeString; // e.g., "10:00 AM"
    private String dayOfWeek;

    public ScheduleModel() {
        // Empty constructor for Firestore
    }

    public ScheduleModel(String scheduleId, String programName, String hostName, String timeString, String dayOfWeek) {
        this.scheduleId = scheduleId;
        this.programName = programName;
        this.hostName = hostName;
        this.timeString = timeString;
        this.dayOfWeek = dayOfWeek;
    }

    public String getScheduleId() { return scheduleId; }
    public String getProgramName() { return programName; }
    public String getHostName() { return hostName; }
    public String getTimeString() { return timeString; }
    public String getDayOfWeek() { return dayOfWeek; }
}
