package lab3;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ThreadingTest {
    private final User user = new User("student@haifa.ac.il", "abcd123!");

    private LoginResult attempt(LoginService service, String email, String password) throws Exception {
        return new LoginTask(service, email, password, result -> {}).authenticate();
    }

    @Test void twoDaemonWorkersReplyAndValidationFailuresCount() throws Exception {
        try (LoginService service = new LoginService(List.of(user), new LoginConfig(2, 1))) {
            assertEquals(2, service.workers().size());
            assertTrue(service.workers().stream().allMatch(Thread::isDaemon));
            assertEquals(-1, attempt(service, "unknown@example.com", "bad").attemptsLeft());
            assertEquals(1, attempt(service, user.getName(), "bad").attemptsLeft());
            assertEquals(LoginResult.Status.BLOCKED, attempt(service, user.getName(), "bad").status());
            assertEquals(LoginResult.Status.BLOCKED, attempt(service, user.getName(), "abcd123!").status());
            assertEquals(2, service.tracker().failureCount(user.getName()));
        }
    }

    @Test void concurrentLoginTasksCannotLoseUpdatesOrBypassBlock() {
        assertTimeoutPreemptively(Duration.ofSeconds(15), () -> {
            try (LoginService service = new LoginService(List.of(user), new LoginConfig(20, 30));
                 ExecutorService pool = Executors.newFixedThreadPool(20)) {
                CountDownLatch start = new CountDownLatch(1);
                List<Future<LoginResult>> answers = new ArrayList<>();
                for (int i = 0; i < 20; i++) answers.add(pool.submit(() -> {
                    start.await();
                    return attempt(service, user.getName(), "wrong");
                }));
                start.countDown();
                int blocked = 0;
                for (Future<LoginResult> answer : answers) {
                    if (answer.get(10, TimeUnit.SECONDS).status() == LoginResult.Status.BLOCKED) blocked++;
                }
                assertEquals(1, blocked);
                assertEquals(20, service.tracker().failureCount(user.getName()));
                assertEquals(LoginResult.Status.BLOCKED, attempt(service, user.getName(), "abcd123!").status());
            }
        });
    }

    @Test void requiredTwentyThreadExperimentPreservesEveryUpdate() throws Exception {
        try (LoginService service = new LoginService(List.of(user), new LoginConfig(3, 1))) {
            SynchronizationCheck.Report report = SynchronizationCheck.check(service, user.getName());
            assertEquals(9_980, report.afterThreads());
            assertEquals(9_999, report.afterNineteen());
            assertFalse(report.blockedEarly());
            assertTrue(report.blockedOnLast());
            assertTrue(report.passed());
            assertEquals(0, service.tracker().failureCount(user.getName()), "Experiment must use isolated state");
        }
    }

    @Test void interruptionStopsCountdownAndAllOwnedThreads() throws Exception {
        LoginService service = new LoginService(List.of(user), new LoginConfig(1, 30));
        CountDownLatch entered = new CountDownLatch(1);
        service.start(() -> {
            entered.countDown();
            try { Thread.sleep(30_000); }
            catch (InterruptedException error) { Thread.currentThread().interrupt(); }
        }, "test-sleeper");
        assertTrue(entered.await(5, TimeUnit.SECONDS));
        service.close();
        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            while (!service.isStopped()) Thread.sleep(10);
        });
        assertThrows(IllegalStateException.class, () -> service.start(() -> {}, "closed"));
    }
}
