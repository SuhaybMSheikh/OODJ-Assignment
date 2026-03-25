package model;

import gui.technician.TechnicianDashboard;

/**
 * SUBCLASS — Technician extends User
 */
public class Technician extends User {

    public Technician(String userID, String username, String password,
                      String firstName, String lastName, String email, String phone) {
        super(userID, username, password, "Technician", firstName, lastName, email, phone);
    }

    @Override
    public void showDashboard() {
        new TechnicianDashboard(this).setVisible(true);
    }
}
