package model;

/**
 * Singleton class to manage the currently authenticated user session
 * across all parts of the application.
 */
public class UserSession {
    private static UserSession instance;
    private User currentUser;
    
    // Private constructor to prevent instantiation
    private UserSession() {
    }
    
    /**
     * Get the singleton instance of UserSession
     * @return The UserSession instance
     */
    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
    
    /**
     * Set the currently authenticated user
     * @param user The authenticated user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Get the currently authenticated user
     * @return The current user, or null if no user is authenticated
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Check if a user is currently logged in
     * @return true if a user is authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return currentUser != null;
    }
    
    /**
     * Log out the current user
     */
    public void logout() {
        currentUser = null;
    }
}