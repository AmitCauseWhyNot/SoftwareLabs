package lab3;

import java.util.function.LongConsumer;
import javafx.application.Platform;

/** Sleeps off the UI thread and derives each tick from the actual account deadline. */
public final class CountdownTask implements Runnable {
    private final LoginService service;
    private final String email;
    private final LongConsumer update;

    CountdownTask(LoginService service, String email, LongConsumer update) {
        this.service = service;
        this.email = email;
        this.update = update;
    }

    @Override public void run() {
        try {
            while (!service.isClosed() && !Thread.currentThread().isInterrupted()) {
                long seconds = service.tracker().remainingSeconds(email);
                Platform.runLater(() -> { if (!service.isClosed()) update.accept(seconds); });
                if (seconds == 0) return;
                Thread.sleep(1_000);
            }
        } catch (InterruptedException error) { Thread.currentThread().interrupt(); }
    }
}
