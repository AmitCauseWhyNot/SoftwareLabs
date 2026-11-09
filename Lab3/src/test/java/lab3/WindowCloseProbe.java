package lab3;

import java.nio.file.Path;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/** Separate process: implicit exit is disabled to test explicit shutdown. */
public final class WindowCloseProbe {
    public static void main(String[] args) { Application.launch(Probe.class, args); }
    public static final class Probe extends Application {
        private LoginApp.Prepared prepared;
        private LoginApp app;
        @Override public void init() throws Exception { prepared = LoginApp.prepare(Path.of("users.txt")); }
        @Override public void start(Stage stage) throws Exception {
            Platform.setImplicitExit(false);
            app = new LoginApp();
            app.open(stage, prepared, List.of("1", "30"));
            String mode = getParameters().getRaw().get(0);
            Thread coordinator = new Thread(() -> {
                try {
                    if (!mode.equals("login")) {
                        LoginViewTest.fx(() -> {
                            ((TextField) stage.getScene().lookup("#usernameField")).setText("B_Kernighan@mail.com");
                            ((PasswordField) stage.getScene().lookup("#passwordField"))
                                    .setText(mode.equals("welcome") ? "!a01011942" : "wrong");
                            ((Button) stage.getScene().lookup("#loginButton")).fire();
                            return null;
                        });
                        LoginViewTest.await(() -> mode.equals("welcome")
                                ? stage.getScene().lookup("#welcomeMessage") != null
                                : ((Label) stage.getScene().lookup("#messageLabel")).getText().contains("החשבון חסום"));
                    }
                    LoginViewTest.fx(() -> {
                        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)); return null;
                    });
                } catch (Exception error) { error.printStackTrace(); System.exit(2); }
            }, "probe-coordinator");
            coordinator.setDaemon(true); coordinator.start();
        }
        @Override public void stop() { app.stop(); }
    }
}
