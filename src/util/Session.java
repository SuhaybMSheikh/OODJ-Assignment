package util;

import model.User;

/**
 * UTILITY CLASS — Session
 *
 * Stores the currently logged-in user so any class can access it.
 *
 * OOP CONCEPT: This is a SINGLETON-style utility — one shared state.
 * All fields and methods are static, so there's only ever ONE session.
 *
 * HOW TO USE:
 *   // After login:
 *   Session.setCurrentUser(user);
 *
 *   // In any dashboard to know who is logged in:
 *   User me = Session.getCurrentUser();
 *
 *   // On logout:
 *   Session.clearSession();
 */
public class Session {

    // The currently logged-in user. null = no one is logged in.
    private static User currentUser = null;

    // Private constructor — prevents anyone from doing: new Session()
    private Session() {}

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void clearSession() {
        currentUser = null;
    }
}
