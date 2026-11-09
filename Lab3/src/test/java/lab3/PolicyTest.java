package lab3;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PolicyTest {
    private final User user = new User("student@haifa.ac.il", "abcd123!");

    @Test void argumentsRequireExactlyTwoPositiveIntegers() {
        assertEquals(new LoginConfig(3, 30), LoginConfig.parse(List.of("3", "30")));
        for (List<String> args : List.of(List.<String>of(), List.of("1"),
                List.of("1", "2", "3"), List.of("0", "1"), List.of("1", "-1"),
                List.of("1.5", "3"), List.of("abc", "2"), List.of("2147483648", "1"))) {
            assertThrows(IllegalArgumentException.class, () -> LoginConfig.parse(args));
        }
        assertThrows(IllegalArgumentException.class, () -> new LoginConfig(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new LoginConfig(1, 0));
    }

    @Test void blockStartsOnNthFailureAndBlockedAttemptsNeverExtendIt() {
        AtomicLong time = new AtomicLong(100);
        AttemptTracker tracker = new AttemptTracker(List.of(user), new LoginConfig(2, 3), time::get);
        assertEquals(1, tracker.recordFailure(user.getName()).attemptsLeft());
        assertEquals(LoginResult.Status.BLOCKED, tracker.recordFailure(user.getName()).status());
        time.addAndGet(1_000_000_000L);
        assertEquals(2, tracker.recordFailure(user.getName()).secondsLeft());
        assertEquals(2, tracker.failureCount(user.getName()));
        assertEquals(LoginResult.Status.BLOCKED, tracker.recordSuccess(user.getName()).status());
        time.addAndGet(2_000_000_000L);
        assertEquals(0, tracker.remainingSeconds(user.getName()));
        assertEquals(0, tracker.failureCount(user.getName()));
        assertEquals(1, tracker.recordFailure(user.getName()).attemptsLeft());
    }

    @Test void successResetsFailuresAndUnknownEmailsHaveNoState() {
        AttemptTracker tracker = new AttemptTracker(List.of(user), new LoginConfig(2, 1));
        tracker.recordFailure(user.getName());
        assertEquals(LoginResult.Status.SUCCESS, tracker.recordSuccess(user.getName()).status());
        assertEquals(0, tracker.failureCount(user.getName()));
        assertEquals(1, tracker.recordFailure(user.getName()).attemptsLeft());
        assertEquals(-1, tracker.recordFailure("unknown@example.com").attemptsLeft());
        assertFalse(tracker.contains("unknown@example.com"));
        assertEquals(0, tracker.remainingSeconds("unknown@example.com"));
        assertEquals(1, tracker.trackedCount());
    }

    @Test void expiryRoundsUpAndAccountsRemainIndependent() {
        AtomicLong time = new AtomicLong();
        User other = new User("other@example.com", "abcd123!");
        AttemptTracker tracker = new AttemptTracker(List.of(user, other), new LoginConfig(1, 1), time::get);
        tracker.recordFailure(user.getName());
        time.set(999_999_999);
        assertEquals(1, tracker.remainingSeconds(user.getName()));
        assertEquals(LoginResult.Status.SUCCESS, tracker.recordSuccess(other.getName()).status());
        time.incrementAndGet();
        assertEquals(0, tracker.remainingSeconds(user.getName()));
        assertEquals(LoginResult.Status.BLOCKED, tracker.recordFailure(user.getName()).status());
    }
}
