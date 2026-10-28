package lab2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/** Handles login events; validation errors stay inside the existing window. */
public final class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Button loginButton;
    private List<User> users = List.of();

    public void setUsers(List<User> users) {
        this.users = new ArrayList<>(users);
    }

    public void showFileError() {
        messageLabel.setText("Could not read users file.");
        loginButton.setDisable(true);
    }

    @FXML
    private void login() {
        if (!UserRepository.authenticate(users, usernameField.getText(), passwordField.getText())) {
            messageLabel.setText("Invalid username or password.");
            return;
        }
        try {
            Parent welcome = FXMLLoader.load(LoginController.class.getResource("welcome.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(welcome));
            stage.setTitle("GCM - Welcome");
        } catch (IOException error) {
            System.err.println("Could not open Welcome screen: " + error.getMessage());
            messageLabel.setText("Could not open Welcome screen.");
        }
    }
}
