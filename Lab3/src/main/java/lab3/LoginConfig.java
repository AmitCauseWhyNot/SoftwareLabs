package lab3;

import java.util.List;

/** Runtime policy: never substitute hard-coded defaults for invalid arguments. */
public record LoginConfig(int maxFailures, int blockSeconds) {
    public LoginConfig {
        if (maxFailures < 1 || blockSeconds < 1)
            throw new IllegalArgumentException("n and t must be positive integers.");
    }

    public static LoginConfig parse(List<String> arguments) {
        if (arguments.size() != 2)
            throw new IllegalArgumentException("Enter exactly two arguments: n t.");
        try {
            return new LoginConfig(Integer.parseInt(arguments.get(0)), Integer.parseInt(arguments.get(1)));
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("n and t must be positive integers.", error);
        }
    }
}
