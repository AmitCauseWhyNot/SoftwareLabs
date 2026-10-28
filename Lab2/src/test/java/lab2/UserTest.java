package lab2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @Test
    void acceptsBoundaryLengthsAndEitherLetterCase() {
        assertDoesNotThrow(() -> new User("a".repeat(44) + "@b.com", "abcd123!"));
        assertDoesNotThrow(() -> new User("_+%-@1.CO", "AB123456789!"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"hello", "@b.com", "a@.com", "a@b.c", "a@@b.com",
            "a@-b.com", "a@_b.com", "a!b@c.com", "a b@c.com", "a@b.com\n", "é@b.com"})
    void rejectsInvalidEmail(String name) {
        assertEquals("Please enter a valid Email as username",
                assertThrows(IllegalArgumentException.class,
                        () -> new User(name, "abcd123!")).getMessage());
    }

    @Test
    void reportsLengthErrorsBeforeContentErrors() {
        assertEquals("Username is too long, try something shorter",
                assertThrows(IllegalArgumentException.class,
                        () -> new User("?".repeat(51), "x")).getMessage());
        assertEquals("Your password is too short, add more characters",
                assertThrows(IllegalArgumentException.class,
                        () -> new User("a@b.co", "~~~~~~~")).getMessage());
        assertEquals("Your password is too long, try a shorter one",
                assertThrows(IllegalArgumentException.class,
                        () -> new User("a@b.co", "~".repeat(13))).getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcdefg!", "1234567!", "abcd1234", "abcd12 !",
            "abcd12\t!", "abcd12\n!", "ébcd123!", "abcd123١"})
    void requiresLetterDigitAndAllowedSymbol(String password) {
        assertEquals("Please enter a valid password",
                assertThrows(IllegalArgumentException.class,
                        () -> new User("a@b.co", password)).getMessage());
    }

    @Test
    void checksEveryAsciiPunctuationCharacter() {
        String allowed = "!@#$%^&*()_+-=";
        for (char symbol = '!'; symbol <= '~'; symbol++) {
            if (!Character.isLetterOrDigit(symbol)) {
                String password = "abcd123" + symbol;
                if (allowed.indexOf(symbol) >= 0) {
                    assertDoesNotThrow(() -> new User("a@b.co", password));
                } else {
                    assertThrows(IllegalArgumentException.class,
                            () -> new User("a@b.co", password));
                }
            }
        }
    }

    @Test
    void matchesBothCredentialsExactlyWithoutTrimmingOrIgnoringCase() {
        User user = new User("a@b.co", "abcd123!");
        assertTrue(user.matchesCredentials("a@b.co", "abcd123!"));
        assertFalse(user.matchesCredentials("A@b.co", "abcd123!"));
        assertFalse(user.matchesCredentials("a@b.co", "ABCD123!"));
        assertFalse(user.matchesCredentials("a@b.co", "abcd123! "));
        assertFalse(user.matchesCredentials(null, null));
    }
}
