package lab3;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/** Each request has a private one-item reply queue, so replies cannot cross logins. */
public record FailedAttempt(String email, BlockingQueue<LoginResult> reply) {
    public FailedAttempt(String email) { this(email, new ArrayBlockingQueue<>(1)); }
}
