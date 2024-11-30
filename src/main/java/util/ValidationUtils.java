package util;

import java.util.regex.Pattern;

public class ValidationUtils {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[A-Za-z\\s\\-']{2,50}$"
    );

    private static final Pattern DEPARTMENT_PATTERN = Pattern.compile(
            "^[A-Za-z\\s\\-&]{2,50}$"
    );

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isValidDepartment(String department) {
        return department != null && DEPARTMENT_PATTERN.matcher(department).matches();
    }

    public static String formatValidationErrors(String firstName, String lastName,
                                                String email, String department) {
        StringBuilder errors = new StringBuilder();

        if (!isValidName(firstName)) {
            errors.append("Invalid first name\n");
        }
        if (!isValidName(lastName)) {
            errors.append("Invalid last name\n");
        }
        if (!isValidEmail(email)) {
            errors.append("Invalid email format\n");
        }
        if (!isValidDepartment(department)) {
            errors.append("Invalid department name\n");
        }

        return errors.toString();
    }
}