package model;

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
