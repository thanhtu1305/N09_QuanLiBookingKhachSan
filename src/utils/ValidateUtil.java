package utils;

public class ValidateUtil {
    private ValidateUtil() {
    }

    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isPhone(String phone) {
        return phone != null && phone.matches("0\\d{9,10}");
    }

    public static boolean isEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}
