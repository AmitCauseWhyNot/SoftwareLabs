package lab2;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class LoginViewTest {
    @TempDir Path directory;

    @BeforeAll
    static void startJavaFx() throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            ready.countDown();
        });
        assertTrue(ready.await(15, TimeUnit.SECONDS), "JavaFX did not start");
    }

    @AfterAll
    static void stopJavaFx() {
        Platform.exit();
    }

    @Test
    void fxmlShowsInlineErrorAndLoadsNewWelcomeSceneForValidUser() throws Exception {
        Path users = Path.of("users.txt");
        onFxThread(() -> {
            Stage stage = new Stage();
            try {
                LoginApp.showLogin(stage, users);
                Scene login = stage.getScene();
                TextField username = (TextField) login.lookup("#usernameField");
                PasswordField password = (PasswordField) login.lookup("#passwordField");
                Button button = (Button) login.lookup("#loginButton");
                Label message = (Label) login.lookup("#messageLabel");
                assertNotNull(username);
                assertNotNull(password);
                assertTrue(button.isDefaultButton());
                assertEquals("", message.getText());
                capture(login, "01-login.png");

                username.setText("B_Kernighan@mail.com");
                password.setText("wrong");
                button.fire();
                assertSame(login, stage.getScene());
                assertEquals("Invalid username or password.", message.getText());
                assertEquals(1, Window.getWindows().size(), "Errors must not open a popup");
                capture(login, "02-invalid-login.png");

                password.setText("!a01011942");
                button.fire();
                assertNotSame(login, stage.getScene());
                Label welcome = (Label) stage.getScene().lookup("#welcomeMessage");
                assertNotNull(welcome);
                assertTrue(welcome.getText().contains("Welcome"));
                assertEquals(1, Window.getWindows().size());
                capture(stage.getScene(), "03-welcome.png");
            } finally {
                stage.hide();
            }
            return null;
        });
    }

    @Test
    void missingInputFileShowsInlineErrorAndDisablesLogin() throws Exception {
        onFxThread(() -> {
            Stage stage = new Stage();
            try {
                LoginApp.showLogin(stage, directory.resolve("missing.txt"));
                assertTrue(((Button) stage.getScene().lookup("#loginButton")).isDisabled());
                assertEquals("Could not read users file.",
                        ((Label) stage.getScene().lookup("#messageLabel")).getText());
                assertEquals(1, Window.getWindows().size());
            } finally {
                stage.hide();
            }
            return null;
        });
    }

    @Test
    void blankCredentialsDoNotOpenWelcomeScreen() throws Exception {
        Path users = directory.resolve("users.txt");
        Files.writeString(users, "student@haifa.ac.il abcd123!\n");
        onFxThread(() -> {
            Stage stage = new Stage();
            try {
                LoginApp.showLogin(stage, users);
                Scene login = stage.getScene();
                ((Button) login.lookup("#loginButton")).fire();
                assertSame(login, stage.getScene());
                assertEquals("Invalid username or password.",
                        ((Label) login.lookup("#messageLabel")).getText());
            } finally {
                stage.hide();
            }
            return null;
        });
    }

    private static <T> T onFxThread(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(15, TimeUnit.SECONDS);
    }

    private static void capture(Scene scene, String name) throws Exception {
        String location = System.getProperty("lab2.screenshots");
        if (location == null) {
            return;
        }
        scene.getRoot().applyCss();
        scene.getRoot().layout();
        WritableImage image = scene.snapshot(null);
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int[] pixels = new int[width * height];
        image.getPixelReader().getPixels(0, 0, width, height,
                PixelFormat.getIntArgbInstance(), pixels, 0, width);
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        result.setRGB(0, 0, width, height, pixels, 0, width);
        Path path = Files.createDirectories(Path.of(location)).resolve(name);
        ImageIO.write(result, "png", path.toFile());
    }
}
