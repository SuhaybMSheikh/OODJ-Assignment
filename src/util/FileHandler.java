package util;

import model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UTILITY CLASS — FileHandler
 * 
 * This is the ONLY class that directly reads and writes .txt files.
 * Every dashboard calls these static methods instead of doing file I/O themselves.
 *
 * HOW TO USE (from any other class):
 *   List<User> users = FileHandler.loadAllUsers();
 *   FileHandler.saveAllUsers(users);
 *
 * All methods are STATIC — you never need to create a FileHandler object.
 */
public class FileHandler {

    // FILE PATHS
    // Using relative paths so the project works on any computer.
    // These paths are relative to wherever you RUN the program from (the project root).
    private static final String USERS_FILE        = "src/data/users.txt";
    private static final String CUSTOMERS_FILE    = "src/data/customers.txt";
    private static final String APPOINTMENTS_FILE = "src/data/appointments.txt";
    private static final String SERVICES_FILE     = "src/data/services.txt";
    private static final String PAYMENTS_FILE     = "src/data/payments.txt";
    private static final String FEEDBACKS_FILE    = "src/data/feedbacks.txt";
    private static final String COMMENTS_FILE     = "src/data/comments.txt";

    //  USERS
    /**
     * Reads users.txt and returns a List of User objects.
     * Each line is split by "|" and parsed into the correct subclass.
     */
    public static List<User> loadAllUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;  // skip blank lines

                // Split the line into parts using "|" as the separator
                // parts[0]=ID, [1]=username, [2]=password, [3]=role,
                // [4]=firstName, [5]=lastName, [6]=email, [7]=phone
                String[] parts = line.split("\\|");
                if (parts.length < 8) continue;  // skip malformed lines

                String role = parts[3];
                User user;

                // Create the correct subclass based on the role field
                switch (role) {
                    case "Manager":
                        user = new Manager(parts[0], parts[1], parts[2],
                                           parts[4], parts[5], parts[6], parts[7]);
                        break;
                    case "CounterStaff":
                        user = new CounterStaff(parts[0], parts[1], parts[2],
                                                parts[4], parts[5], parts[6], parts[7]);
                        break;
                    case "Technician":
                        user = new Technician(parts[0], parts[1], parts[2],
                                              parts[4], parts[5], parts[6], parts[7]);
                        break;
                    case "Customer":
                        user = new Customer(parts[0], parts[1], parts[2],
                                            parts[4], parts[5], parts[6], parts[7]);
                        break;
                    default:
                        continue;  // unknown role — skip
                }
                users.add(user);
            }
        } catch (IOException e) {
            System.err.println("Error reading users file: " + e.getMessage());
        }
        return users;
    }

    /**
     * Overwrites users.txt with the provided list.
     * Call this after any add/edit/delete operation on users.
     */
    public static void saveAllUsers(List<User> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (User u : users) {
                writer.write(u.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving users file: " + e.getMessage());
        }
    }

    /**
     * Finds a user by username — used during login.
     * Returns null if not found.
     */
    public static User findUserByUsername(String username) {
        for (User u : loadAllUsers()) {
            if (u.getUsername().equals(username)) return u;
        }
        return null;
    }

    /**
     * Generates the next available User ID (e.g. if U004 exists, returns U005).
     */
    public static String generateNextUserID() {
        List<User> users = loadAllUsers();
        int max = 0;
        for (User u : users) {
            try {
                int num = Integer.parseInt(u.getUserID().substring(1)); // strip the "U"
                if (num > max) max = num;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("U%03d", max + 1);  // e.g. "U005"
    }

    //  APPOINTMENTS
    public static List<Appointment> loadAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length < 8) continue;
                list.add(new Appointment(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7]));
            }
        } catch (IOException e) {
            System.err.println("Error reading appointments: " + e.getMessage());
        }
        return list;
    }

    public static void saveAllAppointments(List<Appointment> appointments) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(APPOINTMENTS_FILE))) {
            for (Appointment a : appointments) {
                writer.write(a.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving appointments: " + e.getMessage());
        }
    }

    public static String generateNextAppointmentID() {
        List<Appointment> list = loadAllAppointments();
        int max = 0;
        for (Appointment a : list) {
            try {
                int num = Integer.parseInt(a.getAppointmentID().substring(1));
                if (num > max) max = num;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("A%03d", max + 1);
    }

    //  PAYMENTS
    public static List<Payment> loadAllPayments() {
        List<Payment> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(PAYMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length < 5) continue;
                list.add(new Payment(p[0], p[1], Double.parseDouble(p[2]), p[3], p[4]));
            }
        } catch (IOException e) {
            System.err.println("Error reading payments: " + e.getMessage());
        }
        return list;
    }

    public static void saveAllPayments(List<Payment> payments) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PAYMENTS_FILE))) {
            for (Payment pay : payments) {
                writer.write(pay.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving payments: " + e.getMessage());
        }
    }

    public static String generateNextPaymentID() {
        List<Payment> list = loadAllPayments();
        int max = 0;
        for (Payment p : list) {
            try {
                int num = Integer.parseInt(p.getPaymentID().substring(1));
                if (num > max) max = num;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("P%03d", max + 1);
    }

    //  FEEDBACKS
    public static List<Feedback> loadAllFeedbacks() {
        List<Feedback> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FEEDBACKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                // Split into max 3 parts — feedback text may contain "|"
                String[] p = line.split("\\|", 3);
                if (p.length < 3) continue;
                list.add(new Feedback(p[0], p[1], p[2]));
            }
        } catch (IOException e) {
            System.err.println("Error reading feedbacks: " + e.getMessage());
        }
        return list;
    }

    public static void saveAllFeedbacks(List<Feedback> feedbacks) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FEEDBACKS_FILE))) {
            for (Feedback f : feedbacks) {
                writer.write(f.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving feedbacks: " + e.getMessage());
        }
    }

    //  COMMENTS
    public static List<Comment> loadAllComments() {
        List<Comment> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(COMMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|", 3);
                if (p.length < 3) continue;
                list.add(new Comment(p[0], p[1], p[2]));
            }
        } catch (IOException e) {
            System.err.println("Error reading comments: " + e.getMessage());
        }
        return list;
    }

    public static void saveAllComments(List<Comment> comments) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(COMMENTS_FILE))) {
            for (Comment c : comments) {
                writer.write(c.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving comments: " + e.getMessage());
        }
    }

    //  SERVICES (prices)
    /**
     * Returns price for a given service type ("Normal" or "Major").
     * Returns 0.0 if not found.
     */
    public static double getServicePrice(String serviceType) {
        try (BufferedReader reader = new BufferedReader(new FileReader(SERVICES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 2 && p[0].equalsIgnoreCase(serviceType)) {
                    return Double.parseDouble(p[1]);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading services: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Returns duration in hours for a service type.
     * Normal = 1, Major = 3.
     */
    public static int getServiceDuration(String serviceType) {
        try (BufferedReader reader = new BufferedReader(new FileReader(SERVICES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 3 && p[0].equalsIgnoreCase(serviceType)) {
                    return Integer.parseInt(p[2]);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading services: " + e.getMessage());
        }
        return 1;
    }

    /**
     * Updates the price for a service type.
     * Manager uses this when setting prices.
     */
    public static void updateServicePrice(String serviceType, double newPrice) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(SERVICES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 2 && p[0].equalsIgnoreCase(serviceType)) {
                    // Replace the price in this line but keep the duration
                    line = p[0] + "|" + String.format("%.2f", newPrice) + "|" + (p.length >= 3 ? p[2] : "1");
                }
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading services: " + e.getMessage());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SERVICES_FILE))) {
            for (String l : lines) {
                writer.write(l);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing services: " + e.getMessage());
        }
    }
}
