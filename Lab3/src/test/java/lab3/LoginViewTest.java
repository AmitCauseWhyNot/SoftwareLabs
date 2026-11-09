package lab3;

import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class LoginViewTest {
    @TempDir Path directory;
    private LoginApp app;
    private Stage stage;

    @BeforeAll static void startToolkit() throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        Platform.startup(() -> { Platform.setImplicitExit(false); ready.countDown(); });
        assertTrue(ready.await(15, TimeUnit.SECONDS));
    }
    @AfterAll static void stopToolkit() { Platform.exit(); }
    @AfterEach void close() throws Exception {
        fx(() -> { if (app != null) app.stop(); if (stage != null) stage.hide(); return null; });
    }
    static <T> T fx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
    static void await(Callable<Boolean> condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!fx(condition)) {
            if (System.nanoTime() > deadline) fail("Timed out waiting for the UI");
            Thread.sleep(20);
        }
    }
    private void open(Path users, List<String> args) throws Exception {
        LoginApp.Prepared prepared = LoginApp.prepare(users); // File work is off the FX thread.
        fx(() -> { app = new LoginApp(); stage = new Stage(); app.open(stage, prepared, args); return null; });
    }
    private Button login() { return (Button) stage.getScene().lookup("#loginButton"); }
    private Label message() { return (Label) stage.getScene().lookup("#messageLabel"); }
    private void attempt(String email, String password) throws Exception {
        fx(() -> {
            ((TextField) stage.getScene().lookup("#usernameField")).setText(email);
            ((PasswordField) stage.getScene().lookup("#passwordField")).setText(password);
            login().fire();
            assertTrue(login().isDisabled());
            assertTrue(stage.getScene().lookup("#usernameField").isDisabled());
            return null;
        });
    }

    @Test void invalidArgumentsAreCorrectedBeforeLoginAndSuccessLoadsWelcome() throws Exception {
        open(Path.of("users.txt"), List.of("0", "bad"));
        fx(() -> {
            assertNull(stage.getScene().lookup("#loginButton"));
            capture(stage.getScene(), "01-invalid-arguments.png");
            ((TextField) stage.getScene().lookup("#nField")).setText("0");
            ((TextField) stage.getScene().lookup("#tField")).setText("2");
            ((Button) stage.getScene().lookup("#applyButton")).fire();
            assertFalse(((Label) stage.getScene().lookup("#configError")).getText().isEmpty());
            ((TextField) stage.getScene().lookup("#nField")).setText("3");
            ((Button) stage.getScene().lookup("#applyButton")).fire();
            return null;
        });
        attempt("B_Kernighan@mail.com", "!a01011942");
        await(() -> stage.getScene().lookup("#welcomeMessage") != null);
        fx(() -> { capture(stage.getScene(), "02-success.png"); return null; });
    }

    @Test void liveCountdownUnlocksAndUnknownEmailIsNotTracked() throws Exception {
        open(Path.of("users.txt"), List.of("2", "2"));
        attempt("absent@example.com", "wrong");
        await(() -> !login().isDisabled());
        fx(() -> { assertEquals("פרטים שגויים", message().getText()); return null; });
        assertFalse(app.service().tracker().contains("absent@example.com"));
        attempt("B_Kernighan@mail.com", "wrong");
        await(() -> !login().isDisabled());
        fx(() -> {
            assertTrue(message().getText().contains("1"));
            capture(stage.getScene(), "03-attempts-left.png"); return null;
        });
        attempt("B_Kernighan@mail.com", "wrong");
        await(() -> message().getText().contains("החשבון חסום"));
        fx(() -> { assertTrue(login().isDisabled()); capture(stage.getScene(), "04-blocked.png"); return null; });
        // A queued FX action completes while the background countdown is sleeping.
        assertEquals("responsive", fx(() -> "responsive"));
        await(() -> message().getText().contains("1 שניות"));
        fx(() -> { capture(stage.getScene(), "05-countdown.png"); return null; });
        await(() -> !login().isDisabled());
        fx(() -> { assertEquals("", message().getText()); capture(stage.getScene(), "06-unblocked.png"); return null; });
        attempt("B_Kernighan@mail.com", "!a01011942");
        await(() -> stage.getScene().lookup("#welcomeMessage") != null);
    }

    @Test void missingFileIsInlineAndSynchronizationButtonRunsInBackground() throws Exception {
        open(directory.resolve("missing.txt"), List.of("3", "1"));
        fx(() -> { assertTrue(login().isDisabled()); assertTrue(message().getText().contains("קובץ")); return null; });
        close();
        open(Path.of("users.txt"), List.of("3", "1"));
        fx(() -> { ((Button) stage.getScene().lookup("#checkButton")).fire(); return null; });
        await(() -> ((Label) stage.getScene().lookup("#checkResult")).getText().contains("PASS"));
        fx(() -> { capture(stage.getScene(), "07-synchronized.png"); return null; });
    }

    @Test void anotherWindowCannotBypassTheSameAccountBlock() throws Exception {
        open(Path.of("users.txt"), List.of("1", "2"));
        LoginApp.Prepared prepared = LoginApp.prepare(Path.of("users.txt"));
        Stage second = fx(() -> {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
            javafx.scene.Parent root = loader.load(LoginApp.viewInput(prepared.loginXml()));
            ((LoginController) loader.getController()).configure(app.service(), prepared.welcomeXml());
            Stage window = new Stage(); window.setScene(new Scene(root)); window.show();
            assertThrows(IllegalStateException.class, () -> LoginApp.prepare(Path.of("users.txt")));
            return window;
        });
        try {
            attempt("B_Kernighan@mail.com", "wrong");
            await(() -> message().getText().contains("החשבון חסום"));
            fx(() -> {
                ((TextField) second.getScene().lookup("#usernameField")).setText("B_Kernighan@mail.com");
                ((PasswordField) second.getScene().lookup("#passwordField")).setText("!a01011942");
                ((Button) second.getScene().lookup("#loginButton")).fire();
                return null;
            });
            await(() -> ((Label) second.getScene().lookup("#messageLabel")).getText().contains("החשבון חסום"));
            assertEquals(1, app.service().tracker().failureCount("B_Kernighan@mail.com"));
        } finally { fx(() -> { second.hide(); return null; }); }
    }

    private static void capture(Scene scene, String name) throws Exception {
        String destination = System.getProperty("lab3.screenshots");
        if (destination == null) return;
        scene.getRoot().applyCss(); scene.getRoot().layout();
        WritableImage image = scene.snapshot(null);
        int width = (int) image.getWidth(), height = (int) image.getHeight();
        int[] pixels = new int[width * height];
        image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        result.setRGB(0, 0, width, height, pixels, 0, width);
        ImageIO.write(result, "png", Files.createDirectories(Path.of(destination)).resolve(name).toFile());
    }
}
