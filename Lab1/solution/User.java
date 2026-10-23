import java.util.regex.Pattern;

/** Immutable credentials; the constructor rejects invalid usernames and passwords. */
public final class User {
    private static final Pattern EMAIL = Pattern.compile(
            "[A-Za-z0-9._%+-]+@[A-Za-z0-9][A-Za-z0-9.-]*\\.[A-Za-z]{2,}");
    // Symbols follow the instructor's Moodle clarification of 22 October 2026.
    private static final Pattern PASSWORD = Pattern.compile(
            "(?=.*[A-Za-z])(?=.*[0-9])(?=.*[!@#$%^&*()_+=-])[A-Za-z0-9!@#$%^&*()_+=-]+");
    private final String name;
    private final String password;

    public User(String name, String password) {
        if (name != null && name.length() > 50) {
            throw new IllegalArgumentException("Username is too long, try something shorter");
        }
        if (name == null || !EMAIL.matcher(name).matches()) {
            throw new IllegalArgumentException("Please enter a valid Email as username");
        }
        if (password == null) {
            throw new IllegalArgumentException("Please enter a valid password");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Your password is too short, add more characters");
        }
        if (password.length() > 12) {
            throw new IllegalArgumentException("Your password is too long, try a shorter one");
        }
        if (!PASSWORD.matcher(password).matches()) {
            throw new IllegalArgumentException("Please enter a valid password");
        }
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " " + password;
    }
}
