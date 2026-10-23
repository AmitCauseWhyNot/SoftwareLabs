import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;

/** Reads users, reports invalid rows, and prints valid users sorted by username. */
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
