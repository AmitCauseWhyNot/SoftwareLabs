package lab3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/** Owns the shared policy, two failure workers, and interruptible application threads. */
public final class LoginService implements AutoCloseable {
    private final List<User> users;
    private final AttemptTracker tracker;
    private final BlockingQueue<FailedAttempt> failures = new LinkedBlockingQueue<>();
    private final List<FailureWorker> workers = new ArrayList<>();
    private final List<Thread> threads = new ArrayList<>();
    private volatile boolean closed;

    public LoginService(List<User> users, LoginConfig config) {
        this.users = List.copyOf(users);
        tracker = new AttemptTracker(users, config);
        for (int i = 1; i <= 2; i++) {
            FailureWorker worker = new FailureWorker(i, failures, tracker);
            workers.add(worker);
            startThread(worker);
        }
    }

    public AttemptTracker tracker() { return tracker; }
    List<User> users() { return users; }
    List<FailureWorker> workers() { return List.copyOf(workers); }
    public boolean isClosed() { return closed; }

    public void submit(String email, String password, Consumer<LoginResult> callback) {
        start(new LoginTask(this, email, password, callback), "LoginTask");
    }

    LoginResult recordFailure(String email) throws InterruptedException {
        FailedAttempt attempt = new FailedAttempt(email);
        failures.put(attempt);
        return attempt.reply().take();
    }

    public Thread start(Runnable task, String name) {
        Thread thread = new Thread(task, "lab3-" + name);
        startThread(thread);
        return thread;
    }

    synchronized void startThread(Thread thread) {
        if (closed) throw new IllegalStateException("Login service is closed.");
        threads.removeIf(existing -> !existing.isAlive());
        threads.add(thread);
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized boolean isStopped() {
        return closed && threads.stream().noneMatch(Thread::isAlive);
    }

    @Override public synchronized void close() {
        closed = true;
        for (Thread thread : threads) thread.interrupt();
        failures.clear();
    }
}
