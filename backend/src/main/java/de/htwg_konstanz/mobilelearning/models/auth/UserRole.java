package de.htwg_konstanz.mobilelearning.models.auth;

public class UserRole {

    public static final String STUDENT = "Student";
    public static final String PROF = "Prof";
    public static final String ADMIN = "Admin";

    public static String[] getAllRoles() {
        return new String[] { STUDENT, PROF, ADMIN };
    }
}