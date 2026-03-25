package model;

/**
 * ABSTRACT SUPERCLASS — User
 * ---------------------------
 * This is the parent class for ALL user types in the system.
 * Manager, CounterStaff, Technician, and Customer all EXTEND this class.
 *
 * OOP CONCEPT: ABSTRACTION + INHERITANCE
 * - "abstract" means you cannot create a plain User object directly.
 *   You must create one of the specific subtypes (Manager, etc.)
 * - All subclasses INHERIT the fields and methods defined here.
 * - showDashboard() is abstract — every subclass MUST implement it.
 */
public abstract class User {

    // ENCAPSULATION: fields are private — only accessible via getters/setters
    private String userID;
    private String username;
    private String password;
    private String role;       // "Manager", "CounterStaff", "Technician", "Customer"
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    // CONSTRUCTOR — called when a subclass object is created
    public User(String userID, String username, String password, String role,
                String firstName, String lastName, String email, String phone) {
        this.userID    = userID;
        this.username  = username;
        this.password  = password;
        this.role      = role;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.email     = email;
        this.phone     = phone;
    }

    // ABSTRACT METHOD — every subclass must provide their own version of this
    public abstract void showDashboard();

    // Returns full name as a single string
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Converts this user object back into a line for the .txt file
    // Format: U001|admin|admin123|Manager|Admin|User|admin@apu.edu.my|0123456789
    public String toFileString() {
        return userID + "|" + username + "|" + password + "|" + role + "|" +
               firstName + "|" + lastName + "|" + email + "|" + phone;
    }

    // ─── GETTERS ────────────────────────────────────────────────────────────
    public String getUserID()    { return userID; }
    public String getUsername()  { return username; }
    public String getPassword()  { return password; }
    public String getRole()      { return role; }
    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName; }
    public String getEmail()     { return email; }
    public String getPhone()     { return phone; }

    // ─── SETTERS ────────────────────────────────────────────────────────────
    public void setUsername(String username)   { this.username = username; }
    public void setPassword(String password)   { this.password = password; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName)   { this.lastName = lastName; }
    public void setEmail(String email)         { this.email = email; }
    public void setPhone(String phone)         { this.phone = phone; }
}
