package lab2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Loads valid Lab1 users and checks that both credentials belong to the same user. */
public final class UserRepository {
    private UserRepository() {
    }

    public static ArrayList<User> load(Path file, PrintStream errors) throws IOException {
        ArrayList<User> users = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.trim().split("\\s+", 3);
                try {
                    users.add(new User(fields[0], fields.length == 2 ? fields[1] : null));
                } catch (IllegalArgumentException error) {
                    errors.println(line + ": " + error.getMessage());
                }
            }
        }
        return users;
    }

    public static boolean authenticate(List<User> users, String name, String password) {
        return users.stream().anyMatch(user -> user.matchesCredentials(name, password));
    }
}
