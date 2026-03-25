package model;

import gui.manager.ManagerDashboard;

/**
 * SUBCLASS — Manager extends User
 * --------------------------------
 * OOP CONCEPT: INHERITANCE
 * Manager inherits all fields/methods from User and adds its own behaviour.
 *
 * OOP CONCEPT: POLYMORPHISM
 * showDashboard() is overridden here to open the Manager-specific window.
 */
public class Manager extends User {

    // CONSTRUCTOR — calls the parent (User) constructor using super()
    public Manager(String userID, String username, String password,
                   String firstName, String lastName, String email, String phone) {
        super(userID, username, password, "Manager", firstName, lastName, email, phone);
    }

    /**
     * OVERRIDES the abstract method from User.
     * Opens the Manager dashboard window.
     */
    @Override
    public void showDashboard() {
        new ManagerDashboard(this).setVisible(true);
    }
}
