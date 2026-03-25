package model;

import gui.counterstaff.CounterStaffDashboard;

/**
 * SUBCLASS — CounterStaff extends User
 */
public class CounterStaff extends User {

    public CounterStaff(String userID, String username, String password,
                        String firstName, String lastName, String email, String phone) {
        super(userID, username, password, "CounterStaff", firstName, lastName, email, phone);
    }

    @Override
    public void showDashboard() {
        new CounterStaffDashboard(this).setVisible(true);
    }
}
