package lab3;

/** A short-lived Thread subclass; join() publishes its result to LoginTask. */
public final class BlockCheckThread extends Thread {
    private final AttemptTracker tracker;
    private final String email;
    private long secondsLeft;

    BlockCheckThread(AttemptTracker tracker, String email) {
        super("lab3-BlockCheckThread");
        this.tracker = tracker;
        this.email = email;
    }
    @Override public void run() { secondsLeft = tracker.remainingSeconds(email); }
    public long secondsLeft() { return secondsLeft; }
}
