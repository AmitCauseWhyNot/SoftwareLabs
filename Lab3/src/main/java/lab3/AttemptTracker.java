package lab3;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/** Shared by every login in this process; unknown addresses never create state. */
public final class AttemptTracker {
    private final ConcurrentHashMap<String, UserState> states = new ConcurrentHashMap<>();

    public AttemptTracker(Collection<User> users, LoginConfig config) {
        this(users, config, System::nanoTime);
    }

    AttemptTracker(Collection<User> users, LoginConfig config, LongSupplier clock) {
        for (User user : users)
            states.computeIfAbsent(user.getName(), key -> new UserState(config, clock));
    }

    public boolean contains(String email) { return email != null && states.containsKey(email); }
    public long remainingSeconds(String email) {
        return contains(email) ? states.get(email).remainingSeconds() : 0;
    }
    public LoginResult recordFailure(String email) {
        return contains(email) ? states.get(email).recordFailure() : LoginResult.error(-1);
    }
    public LoginResult recordSuccess(String email) {
        return contains(email) ? states.get(email).recordSuccess() : LoginResult.error(-1);
    }
    public int failureCount(String email) { return contains(email) ? states.get(email).failureCount() : 0; }
    public int trackedCount() { return states.size(); }
}
