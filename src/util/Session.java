package util;

import model.User;


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
