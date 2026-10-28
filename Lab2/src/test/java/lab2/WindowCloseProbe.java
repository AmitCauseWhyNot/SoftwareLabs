package lab2;

import java.nio.file.Path;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/** Runs each close scenario in its own JVM because Platform.exit stops the toolkit. */
public class WindowCloseProbe {
    public static void main(String[] args) {
        Application.launch(Probe.class, args);
        System.out.println("Closed " + args[0]);
    }

    public static class Probe extends Application {
        @Override
        public void start(Stage stage) throws Exception {
            // Explicit exit must work even when implicit last-window exit is disabled.
            Platform.setImplicitExit(false);
            String screen = getParameters().getRaw().get(0);
            LoginApp.showLogin(stage, Path.of(getParameters().getRaw().get(1)));
            if (screen.equals("welcome")) {
                ((TextField) stage.getScene().lookup("#usernameField")).setText("student@haifa.ac.il");
                ((PasswordField) stage.getScene().lookup("#passwordField")).setText("abcd123!");
                ((Button) stage.getScene().lookup("#loginButton")).fire();
                if (stage.getScene().lookup("#welcomeMessage") == null) {
                    throw new AssertionError("Welcome FXML was not loaded");
                }
            }
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        }
    }
}
