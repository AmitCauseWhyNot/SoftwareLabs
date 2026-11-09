package lab3;

import java.util.function.LongSupplier;

/** One monitor protects each account's failure count and monotonic block deadline. */
public final class UserState {
    private final LoginConfig config;
    private final LongSupplier clock;
    private int failures;
    private boolean blocked;
    private long blockedUntil;

    UserState(LoginConfig config, LongSupplier clock) {
        this.config = config;
        this.clock = clock;
    }

    public synchronized long remainingSeconds() {
        if (!blocked) return 0;
        long remaining = blockedUntil - clock.getAsLong();
        if (remaining <= 0) {
            blocked = false;
            failures = 0;
            return 0;
        }
        return (remaining + 999_999_999L) / 1_000_000_000L;
    }

    public synchronized LoginResult recordFailure() {
        long remaining = remainingSeconds();
        if (remaining > 0) return LoginResult.blocked(remaining);
        failures++;
        if (failures == config.maxFailures()) {
            blocked = true;
            blockedUntil = clock.getAsLong() + config.blockSeconds() * 1_000_000_000L;
            return LoginResult.blocked(config.blockSeconds());
        }
        return LoginResult.error(config.maxFailures() - failures);
    }

    public synchronized LoginResult recordSuccess() {
        // Recheck here: another login may have blocked this account after the first check.
        long remaining = remainingSeconds();
        if (remaining > 0) return LoginResult.blocked(remaining);
        failures = 0;
        return LoginResult.success();
    }

    public synchronized int failureCount() {
        remainingSeconds();
        return failures;
    }
}
