package model;

/**
 * MODEL CLASS — Feedback
 * -----------------------
 * Represents one row from feedbacks.txt
 * Format: A001|U003|Replaced brake pads. All good.
 *         [0]  [1]  [2...]
 */
public class Feedback {

    private String appointmentID;
    private String technicianID;
    private String feedbackText;

    public Feedback(String appointmentID, String technicianID, String feedbackText) {
        this.appointmentID = appointmentID;
        this.technicianID  = technicianID;
        this.feedbackText  = feedbackText;
    }

    public String toFileString() {
        return appointmentID + "|" + technicianID + "|" + feedbackText;
    }

    public String getAppointmentID() { return appointmentID; }
    public String getTechnicianID()  { return technicianID; }
    public String getFeedbackText()  { return feedbackText; }
    public void   setFeedbackText(String text) { this.feedbackText = text; }
}
