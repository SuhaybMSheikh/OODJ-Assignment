package model;

/**
 * MODEL CLASS — Payment
 * 
 * Represents one row from payments.txt
 * Format: P001|A001|50.00|2025-05-10|Paid
 *         [0]  [1]  [2]   [3]        [4]
 */
public class Payment {

    private String paymentID;
    private String appointmentID;
    private double amount;
    private String date;
    private String status;  // "Paid"

    public Payment(String paymentID, String appointmentID,
                   double amount, String date, String status) {
        this.paymentID      = paymentID;
        this.appointmentID  = appointmentID;
        this.amount         = amount;
        this.date           = date;
        this.status         = status;
    }

    public String toFileString() {
        return paymentID + "|" + appointmentID + "|" +
               String.format("%.2f", amount) + "|" + date + "|" + status;
    }

    // GETTERS
    public String getPaymentID()     { return paymentID; }
    public String getAppointmentID() { return appointmentID; }
    public double getAmount()        { return amount; }
    public String getDate()          { return date; }
    public String getStatus()        { return status; }

    // SETTERS
    public void setStatus(String status) { this.status = status; }
    public void setAmount(double amount) { this.amount = amount; }
}
