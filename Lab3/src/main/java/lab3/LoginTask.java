package lab3;

import java.util.function.Consumer;
import javafx.application.Platform;

/** One Runnable per click; only the final callback touches the JavaFX thread. */
public final class LoginTask implements Runnable {
    private final LoginService service;
    private final String email;
    private final String password;
    private final Consumer<LoginResult> callback;

    LoginTask(LoginService service, String email, String password, Consumer<LoginResult> callback) {
        this.service = service;
        this.email = email;
        this.password = password;
        this.callback = callback;
    }

    LoginResult authenticate() throws InterruptedException {
        BlockCheckThread check = new BlockCheckThread(service.tracker(), email);
        service.startThread(check);
        check.join();
        if (check.secondsLeft() > 0) return LoginResult.blocked(check.secondsLeft());
        boolean matches = false;
        try {
            new User(email, password); // Reuse Lab1's constructor validation and its exceptions.
            matches = UserRepository.authenticate(service.users(), email, password);
        } catch (IllegalArgumentException error) {
            // A malformed password for a known account is still a failed login.
        }
        if (matches) return service.tracker().recordSuccess(email);
        if (!service.tracker().contains(email)) return LoginResult.error(-1);
        return service.recordFailure(email);
    }

    @Override public void run() {
        try {
            LoginResult result = authenticate();
            Platform.runLater(() -> { if (!service.isClosed()) callback.accept(result); });
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
        } catch (IllegalStateException error) {
            if (!service.isClosed()) throw error;
        }
    }
}
