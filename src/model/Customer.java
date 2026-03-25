package model;

import gui.customer.CustomerDashboard;

/**
 * SUBCLASS — Customer extends User
 */
public class Customer extends User {

    // Customers have an additional field: a separate customer profile ID
    private String customerID;

    public Customer(String userID, String username, String password,
                    String firstName, String lastName, String email, String phone,
                    String customerID) {
        super(userID, username, password, "Customer", firstName, lastName, email, phone);
        this.customerID = customerID;
    }

    // Convenience constructor without customerID (used during registration)
    public Customer(String userID, String username, String password,
                    String firstName, String lastName, String email, String phone) {
        this(userID, username, password, firstName, lastName, email, phone, "");
    }

    @Override
    public void showDashboard() {
        new CustomerDashboard(this).setVisible(true);
    }

    public String getCustomerID()              { return customerID; }
    public void   setCustomerID(String id)     { this.customerID = id; }
}
