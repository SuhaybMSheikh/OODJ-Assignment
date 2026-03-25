package model;

/**
 * MODEL CLASS — Appointment
 * --------------------------
 * Represents one row from appointments.txt
 * Format: A001|C001|U003|2025-05-10|09:00|Normal|Pending|U002
 *         [0]  [1]  [2]  [3]        [4]   [5]    [6]     [7]
 */
public class Appointment {

    private String appointmentID;
    private String customerID;      // links to customers.txt
    private String technicianID;    // links to users.txt (role = Technician)
    private String date;            // format: YYYY-MM-DD
    private String time;            // format: HH:MM
    private String serviceType;     // "Normal" or "Major"
    private String status;          // "Pending" or "Completed"
    private String counterStaffID;  // the staff who booked it

    public Appointment(String appointmentID, String customerID, String technicianID,
                       String date, String time, String serviceType,
                       String status, String counterStaffID) {
        this.appointmentID  = appointmentID;
        this.customerID     = customerID;
        this.technicianID   = technicianID;
        this.date           = date;
        this.time           = time;
        this.serviceType    = serviceType;
        this.status         = status;
        this.counterStaffID = counterStaffID;
    }

    // Converts this object back to a line for appointments.txt
    public String toFileString() {
        return appointmentID + "|" + customerID + "|" + technicianID + "|" +
               date + "|" + time + "|" + serviceType + "|" + status + "|" + counterStaffID;
    }

    // GETTERS
    public String getAppointmentID()  { return appointmentID; }
    public String getCustomerID()     { return customerID; }
    public String getTechnicianID()   { return technicianID; }
    public String getDate()           { return date; }
    public String getTime()           { return time; }
    public String getServiceType()    { return serviceType; }
    public String getStatus()         { return status; }
    public String getCounterStaffID() { return counterStaffID; }

    // SETTERS
    public void setStatus(String status)           { this.status = status; }
    public void setTechnicianID(String techID)     { this.technicianID = techID; }
    public void setDate(String date)               { this.date = date; }
    public void setTime(String time)               { this.time = time; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
}
