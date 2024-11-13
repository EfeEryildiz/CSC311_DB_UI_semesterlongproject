package service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.prefs.Preferences;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserSession {
    private static volatile UserSession instance;
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Preferences userPreferences;
    private static final String PREF_NODE = "com.example.app.usersession";
    private static final MyLogger logger = new MyLogger();

    private String userName;
    private String password;
    private String privileges;
    private boolean isAuthenticated;
    private long loginTime;

    private UserSession(String userName, String password, String privileges) {
        this.userPreferences = Preferences.userRoot().node(PREF_NODE);
        this.userName = userName;
        this.password = hashPassword(password);
        this.privileges = privileges;
        this.isAuthenticated = true;
        this.loginTime = System.currentTimeMillis();

        saveToPreferences();
        logger.makeLog("New user session created for: " + userName);
    }

    private void saveToPreferences() {
        userPreferences.put("USERNAME", userName);
        userPreferences.put("PASSWORD", password); // Already hashed
        userPreferences.put("PRIVILEGES", privileges);
        userPreferences.putLong("LOGIN_TIME", loginTime);
    }

    public static UserSession getInstance(String userName, String password, String privileges) {
        UserSession result = instance;
        if (result == null) {
            lock.writeLock().lock();
            try {
                result = instance;
                if (result == null) {
                    instance = result = new UserSession(userName, password, privileges);
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        return result;
    }

    public static UserSession getInstance(String userName, String password) {
        return getInstance(userName, password, "NONE");
    }

    private String hashPassword(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainPassword.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.makeLog("Password hashing failed: " + e.getMessage());
            return plainPassword; // Fallback to plain password if hashing fails
        }
    }

    public boolean verifyPassword(String inputPassword) {
        lock.readLock().lock();
        try {
            String hashedInput = hashPassword(inputPassword);
            return this.password.equals(hashedInput);
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getUserName() {
        lock.readLock().lock();
        try {
            return userName;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getPrivileges() {
        lock.readLock().lock();
        try {
            return privileges;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isAuthenticated() {
        lock.readLock().lock();
        try {
            return isAuthenticated;
        } finally {
            lock.readLock().unlock();
        }
    }

    public long getSessionDuration() {
        lock.readLock().lock();
        try {
            return System.currentTimeMillis() - loginTime;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void updatePrivileges(String newPrivileges) {
        lock.writeLock().lock();
        try {
            this.privileges = newPrivileges;
            saveToPreferences();
            logger.makeLog("Privileges updated for user: " + userName);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void cleanUserSession() {
        lock.writeLock().lock();
        try {
            this.userName = null;
            this.password = null;
            this.privileges = null;
            this.isAuthenticated = false;
            this.loginTime = 0;

            // Clear preferences
            userPreferences.remove("USERNAME");
            userPreferences.remove("PASSWORD");
            userPreferences.remove("PRIVILEGES");
            userPreferences.remove("LOGIN_TIME");

            instance = null;
            logger.makeLog("User session cleaned");
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static boolean hasStoredCredentials() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        return prefs.get("USERNAME", null) != null &&
                prefs.get("PASSWORD", null) != null;
    }

    public static UserSession restoreSession() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        String userName = prefs.get("USERNAME", null);
        String password = prefs.get("PASSWORD", null);
        String privileges = prefs.get("PRIVILEGES", "NONE");

        if (userName != null && password != null) {
            UserSession restored = new UserSession(userName, password, privileges);
            restored.password = password; // Use stored hashed password
            restored.loginTime = prefs.getLong("LOGIN_TIME", System.currentTimeMillis());
            instance = restored;
            logger.makeLog("Session restored for user: " + userName);
            return restored;
        }
        return null;
    }

    @Override
    public String toString() {
        lock.readLock().lock();
        try {
            return "UserSession{" +
                    "userName='" + userName + '\'' +
                    ", privileges=" + privileges +
                    ", authenticated=" + isAuthenticated +
                    ", sessionDuration=" + getSessionDuration() + "ms" +
                    '}';
        } finally {
            lock.readLock().unlock();
        }
    }
}