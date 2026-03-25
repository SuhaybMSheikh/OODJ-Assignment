package model;

/**
 * MODEL CLASS — Comment
 * 
 * Represents one row from comments.txt
 * Format: A001|C001|Great service, very professional!
 *         [0]  [1]  [2...]
 */
public class Comment {

    private String appointmentID;
    private String customerID;
    private String commentText;

    public Comment(String appointmentID, String customerID, String commentText) {
        this.appointmentID = appointmentID;
        this.customerID    = customerID;
        this.commentText   = commentText;
    }

    public String toFileString() {
        return appointmentID + "|" + customerID + "|" + commentText;
    }

    public String getAppointmentID() { return appointmentID; }
    public String getCustomerID()    { return customerID; }
    public String getCommentText()   { return commentText; }
    public void   setCommentText(String text) { this.commentText = text; }
}
