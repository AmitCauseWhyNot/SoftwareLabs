package lab2;

import java.io.IOException;
import java.nio.file.Path;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Starts the JavaFX application and loads users before showing the login screen. */
public final class LoginApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Path file = getParameters().getRaw().isEmpty()
                ? Path.of("users.txt") : Path.of(getParameters().getRaw().get(0));
        showLogin(stage, file);
    }

    static void showLogin(Stage stage, Path file) throws IOException {
        FXMLLoader loader = new FXMLLoader(LoginApp.class.getResource("login.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();
        try {
            controller.setUsers(UserRepository.load(file, System.err));
        } catch (IOException error) {
            System.err.println("Could not read users file: " + error.getMessage());
            controller.showFileError();
        }
        stage.setTitle("GCM - Login");
        stage.setScene(new Scene(root));
        stage.setMinWidth(420);
        stage.setMinHeight(360);
        // Explicit exit applies to both the login scene and its Welcome replacement.
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
