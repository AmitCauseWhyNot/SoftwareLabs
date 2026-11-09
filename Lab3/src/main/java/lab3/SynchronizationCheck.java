package lab3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/** Isolated demonstration: 20 x 499 failures, followed by 19 and the blocking 20th. */
public final class SynchronizationCheck implements Runnable {
    public record Report(int afterThreads, int afterNineteen, boolean blockedEarly, boolean blockedOnLast) {
        public boolean passed() {
            return afterThreads == 9_980 && afterNineteen == 9_999 && !blockedEarly && blockedOnLast;
        }
        @Override public String toString() {
            return (passed() ? "PASS" : "FAIL") + " | 20 x 499 = " + afterThreads
                    + " | +19 = " + afterNineteen + " | blocked early: " + blockedEarly
                    + " | +20 blocked: " + blockedOnLast;
        }
    }
    private final LoginService service;
    private final String email;
    private final Consumer<String> callback;

    SynchronizationCheck(LoginService service, String email, Consumer<String> callback) {
        this.service = service; this.email = email; this.callback = callback;
    }

    public static Report check(LoginService service, String email) throws InterruptedException {
        AttemptTracker tracker = new AttemptTracker(List.of(new User(email, "abcd123!")), new LoginConfig(10_000, 60));
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();
        try {
            for (int i = 0; i < 20; i++) threads.add(service.start(() -> {
                try {
                    start.await();
                    for (int j = 0; j < 499 && !Thread.currentThread().isInterrupted(); j++)
                        tracker.recordFailure(email);
                } catch (InterruptedException error) { Thread.currentThread().interrupt(); }
            }, "Stress-" + i));
            start.countDown();
            for (Thread thread : threads) thread.join();
            int afterThreads = tracker.failureCount(email);
            boolean early = tracker.remainingSeconds(email) > 0;
            for (int i = 0; i < 19; i++)
                early |= tracker.recordFailure(email).status() == LoginResult.Status.BLOCKED;
            int afterNineteen = tracker.failureCount(email);
            boolean blocked = tracker.recordFailure(email).status() == LoginResult.Status.BLOCKED;
            return new Report(afterThreads, afterNineteen, early, blocked);
        } finally {
            start.countDown();
            for (Thread thread : threads) if (thread.isAlive()) thread.interrupt();
        }
    }

    @Override public void run() {
        try { callback.accept(check(service, email).toString()); }
        catch (InterruptedException error) { Thread.currentThread().interrupt(); }
        catch (IllegalStateException error) { if (!service.isClosed()) throw error; }
    }

    public static void main(String[] args) throws InterruptedException {
        try (LoginService service = new LoginService(List.of(), new LoginConfig(1, 1))) {
            System.out.println(check(service, "stress@example.com"));
        }
    }
}
