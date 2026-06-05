package org.example.clinic.client.model;

public class Statistics {
    private long totalUsers;
    private long totalDoctors;
    private long totalPatients;
    private long totalAppointments;
    private long plannedAppointments;
    private long confirmedAppointments;
    private long completedAppointments;
    private long cancelledAppointments;
    private long missedAppointments;
    private long pendingReminders;

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long v) { this.totalUsers = v; }
    public long getTotalDoctors() { return totalDoctors; }
    public void setTotalDoctors(long v) { this.totalDoctors = v; }
    public long getTotalPatients() { return totalPatients; }
    public void setTotalPatients(long v) { this.totalPatients = v; }
    public long getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(long v) { this.totalAppointments = v; }
    public long getPlannedAppointments() { return plannedAppointments; }
    public void setPlannedAppointments(long v) { this.plannedAppointments = v; }
    public long getConfirmedAppointments() { return confirmedAppointments; }
    public void setConfirmedAppointments(long v) { this.confirmedAppointments = v; }
    public long getCompletedAppointments() { return completedAppointments; }
    public void setCompletedAppointments(long v) { this.completedAppointments = v; }
    public long getCancelledAppointments() { return cancelledAppointments; }
    public void setCancelledAppointments(long v) { this.cancelledAppointments = v; }
    public long getMissedAppointments() { return missedAppointments; }
    public void setMissedAppointments(long v) { this.missedAppointments = v; }
    public long getPendingReminders() { return pendingReminders; }
    public void setPendingReminders(long v) { this.pendingReminders = v; }
}
