package lab3;

import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

/** UI only: captures input, starts tasks, and presents their immutable results. */
public final class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Button loginButton;
    @FXML private Button checkButton;
    @FXML private Label checkResult;
    private LoginService service;
    private String welcomeXml;

    void configure(LoginService service, String welcomeXml) {
        this.service = service; this.welcomeXml = welcomeXml;
    }

    void showFileError() {
        messageLabel.setText("לא ניתן לקרוא את קובץ המשתמשים");
        setBusy(true); checkButton.setDisable(true);
    }

    private void setBusy(boolean busy) {
        usernameField.setDisable(busy); passwordField.setDisable(busy); loginButton.setDisable(busy);
    }

    @FXML private void login() {
        String email = usernameField.getText(), password = passwordField.getText();
        setBusy(true);
        messageLabel.setText("");
        service.submit(email, password, result -> showResult(email, result));
    }

    private void showResult(String email, LoginResult result) {
        switch (result.status()) {
            case SUCCESS -> {
                try {
                    Parent welcome = new FXMLLoader().load(LoginApp.viewInput(welcomeXml));
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.setScene(new Scene(welcome)); stage.setTitle("GCM - Welcome");
                } catch (IOException error) {
                    messageLabel.setText("לא ניתן לפתוח את מסך Welcome"); setBusy(false);
                }
            }
            case ERROR -> {
                messageLabel.setText(result.attemptsLeft() < 0 ? "פרטים שגויים"
                        : "פרטים שגויים. נותרו " + result.attemptsLeft() + " ניסיונות");
                setBusy(false);
            }
            case BLOCKED -> service.start(new CountdownTask(service, email, seconds -> {
                messageLabel.setText(seconds == 0 ? "" : "החשבון חסום, נותרו " + seconds + " שניות");
                setBusy(seconds > 0);
            }), "CountdownTask");
        }
    }

    @FXML private void checkSynchronization() {
        checkButton.setDisable(true); checkResult.setText("Running 20 threads...");
        String email = service.tracker().contains(usernameField.getText())
                ? usernameField.getText() : "stress@example.com";
        service.start(new SynchronizationCheck(service, email, result -> Platform.runLater(() -> {
            if (!service.isClosed()) { checkResult.setText(result); checkButton.setDisable(false); }
        })), "SynchronizationCheck");
    }
}
