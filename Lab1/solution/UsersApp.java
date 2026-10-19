import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Pattern;

public class UsersApp {
    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Usage: java UsersApp [users.txt]");
            System.exit(1);
            return;
        }
        try (BufferedReader input = args.length == 0
                ? new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))
                : Files.newBufferedReader(Paths.get(args[0]), StandardCharsets.UTF_8)) {
            ArrayList<User> users = new ArrayList<>();
            String line;
            while ((line = input.readLine()) != null) {
                String[] fields = line.trim().split("\\s+", 3);
                try {
                    users.add(new User(fields[0], fields.length == 2 ? fields[1] : null));
                } catch (IllegalArgumentException error) {
                    // Keep diagnostics separate so stdout matches the supplied out.txt.
                    System.err.println(line + ": " + error.getMessage());
                }
            }
            users.sort(Comparator.comparing(User::getName));
            users.forEach(System.out::println);
        } catch (IOException error) {
            System.err.println("Could not read input: " + error.getMessage());
            System.exit(1);
        }
    }
}

// Package-private so the required submission consists of one source file.
final class User {
    private static final Pattern EMAIL = Pattern.compile(
            "[A-Za-z0-9._%+-]+@[A-Za-z0-9][A-Za-z0-9.-]*\\.[A-Za-z]{2,}");
    // The final range permits printable ASCII except '~'; the lookaheads require all three types.
    private static final Pattern PASSWORD = Pattern.compile(
            "(?=.*[A-Za-z])(?=.*[0-9])(?=.*[^A-Za-z0-9])[!-}]+");
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
