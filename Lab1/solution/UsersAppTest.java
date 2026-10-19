import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class UsersAppTest {
    private static final String EMAIL = "Please enter a valid Email as username";
    private static final String NAME_LENGTH = "Username is too long, try something shorter";
    private static final String SHORT = "Your password is too short, add more characters";
    private static final String LONG = "Your password is too long, try a shorter one";
    private static final String PASSWORD = "Please enter a valid password";
    private static int checks;

    public static void main(String[] args) throws Exception {
        valid("B_Kernighan@mail.com", "!a01011942");
        valid("hello@world.net", "01041972^H");
        valid("+first_error@this.row", "passw0rd%");
        valid("a.b_c%+d-e@sub-domain.example.co.il", "ABCD123!");
        String fifty = String.join("", Collections.nCopies(44, "a")) + "@b.com";
        valid(fifty, "abcd123!");
        valid("a@b.co", "ab123456789!");
        invalid("a" + fifty, "abcd123!", NAME_LENGTH);
        for (String name : new String[] {"", "hello", "@b.com", "a@.com", "a@b.c",
                "a@b.12", "a@@b.com", "a@-b.com", "a@_b.com", "a!b@c.com",
                "a b@c.com", "a@b.com\n", "a@b.com ", "\u00e9@b.com"}) {
            invalid(name, "abcd123!", EMAIL);
        }
        invalid(null, "abcd123!", EMAIL);
        invalid("a@b.co", null, PASSWORD);
        invalid("a@b.co", "", SHORT);
        invalid("a@b.co", "a12345!", SHORT);
        invalid("a@b.co", "a12345678901!", LONG);
        for (String password : new String[] {"abcdefg!", "1234567!", "abcd1234",
                "abcd123~", "abcd12 !", "abcd12\t!", "abcd12\n!", "\u00e9bcd123!",
                "abcd123" + (char) 0, "abcd123" + (char) 127, "abcd123\u0661"}) {
            invalid("a@b.co", password, PASSWORD);
        }
        invalid("bad", "x", EMAIL);
        invalid("?" + fifty, "x", NAME_LENGTH);
        invalid("a@b.co", "~~~~~~~", SHORT);
        invalid("a@b.co", "~~~~~~~~~~~~~", LONG);
        for (char symbol = '!'; symbol < '~'; symbol++) {
            if (!Character.isLetterOrDigit(symbol)) {
                valid("a@b.co", "abcd123" + symbol);
            }
        }

        Path lab = Paths.get(args.length == 0 ? "Lab1" : args[0]).toAbsolutePath();
        Path work = Files.createTempDirectory("lab1-tests-");
        try {
            Result sample = run(work, read(lab.resolve("Users.txt.txt")));
            equal(0, sample.status);
            equal(read(lab.resolve("out.txt")), sample.out);
            String expectedErrors = "hello 123456789: " + EMAIL + "\n"
                    + "password@error.now ~helloworld: " + PASSWORD + "\n"
                    + "thisEmailIsToLong@wow0123456789wow0123456789wow0123456789.co.il"
                    + "  @1234Abcd: " + NAME_LENGTH + "\n"
                    + "passwordTooShort@mail.ru 123: " + SHORT + "\n"
                    + "passwordToolong@mail.ru 123456789ABcdefg: " + LONG + "\n";
            equal(expectedErrors, sample.err);
            Result file = run(work, "", lab.resolve("Users.txt.txt").toString());
            equal(0, file.status);
            equal(sample.out, file.out);
            equal(sample.err, file.err);

            Result sorted = run(work, "z@b.co abcd123!\nB@b.co ABCD123!\n"
                    + "a@b.co abcd123!\na@b.co xyz1234!");
            equal(0, sorted.status);
            equal("B@b.co ABCD123!\na@b.co abcd123!\na@b.co xyz1234!\nz@b.co abcd123!\n", sorted.out);
            equal("", sorted.err);
            Result whitespace = run(work, "  b@b.co   abcd123!  \r\n\ta@b.co\tabcd123!\t");
            equal("a@b.co abcd123!\nb@b.co abcd123!\n", whitespace.out);
            equal("", whitespace.err);
            Result empty = run(work, "");
            equal(0, empty.status);
            equal("", empty.out);
            equal("", empty.err);
            Result malformed = run(work, "\nonlyname\na@b.co\na@b.co abcd123! extra\na@b.co abcd123!");
            equal(0, malformed.status);
            equal("a@b.co abcd123!\n", malformed.out);
            equal(": " + EMAIL + "\nonlyname: " + EMAIL + "\na@b.co: " + PASSWORD
                    + "\na@b.co abcd123! extra: " + PASSWORD + "\n", malformed.err);
            Result missing = run(work, "", work.resolve("missing.txt").toString());
            equal(1, missing.status);
            equal("", missing.out);
            equal(true, missing.err.startsWith("Could not read input: "));
            Result usage = run(work, "", "one", "two");
            equal(1, usage.status);
            equal("", usage.out);
            equal("Usage: java UsersApp [users.txt]\n", usage.err);
        } finally {
            Files.deleteIfExists(work.resolve("out.txt"));
            Files.deleteIfExists(work.resolve("errors.txt"));
            Files.delete(work);
        }
        System.out.println("Passed " + checks + " checks, including the supplied sample.");
    }

    private static void valid(String name, String password) {
        User user = new User(name, password);
        equal(name, user.getName());
        equal(name + " " + password, user.toString());
    }

    private static void invalid(String name, String password, String message) {
        try {
            new User(name, password);
        } catch (IllegalArgumentException error) {
            equal(message, error.getMessage());
            return;
        }
        throw new AssertionError("Accepted invalid credentials for " + name);
    }

    private static void equal(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected <" + expected + ">, got <" + actual + ">");
        }
        checks++;
    }

    private static String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    private static Result run(Path work, String input, String... args) throws Exception {
        ArrayList<String> command = new ArrayList<>(Arrays.asList(
                Paths.get(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("java.class.path"), "UsersApp"));
        Collections.addAll(command, args);
        Process process = new ProcessBuilder(command)
                .redirectOutput(work.resolve("out.txt").toFile())
                .redirectError(work.resolve("errors.txt").toFile()).start();
        try {
            process.getOutputStream().write(input.getBytes(StandardCharsets.UTF_8));
            process.getOutputStream().close();
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                throw new AssertionError("Application did not finish within ten seconds");
            }
            return new Result(process.exitValue(), read(work.resolve("out.txt")),
                    read(work.resolve("errors.txt")));
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly().waitFor();
            }
        }
    }

    private static class Result {
        final int status;
        final String out;
        final String err;

        Result(int status, String out, String err) {
            this.status = status;
            this.out = out;
            this.err = err;
        }
    }
}
