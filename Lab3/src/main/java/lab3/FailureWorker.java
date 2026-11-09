package lab3;

import java.util.concurrent.BlockingQueue;

/** Two daemon consumers sleep inside take(), rather than polling an empty queue. */
public final class FailureWorker extends Thread {
    private final BlockingQueue<FailedAttempt> queue;
    private final AttemptTracker tracker;

    FailureWorker(int number, BlockingQueue<FailedAttempt> queue, AttemptTracker tracker) {
        super("lab3-FailureWorker-" + number);
        this.queue = queue;
        this.tracker = tracker;
        setDaemon(true);
    }

    @Override public void run() {
        try {
            while (!isInterrupted()) {
                FailedAttempt attempt = queue.take();
                attempt.reply().put(tracker.recordFailure(attempt.email()));
            }
        } catch (InterruptedException error) { interrupt(); }
    }
}
