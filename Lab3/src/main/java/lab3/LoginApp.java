package lab3;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** Reads files in init() on the launcher thread, before any JavaFX screen work. */
public final class LoginApp extends Application {
    record Prepared(List<User> users, String loginXml, String welcomeXml, boolean fileError) { }
    private Prepared prepared;
    private LoginService service;

    @Override public void init() throws IOException { prepared = prepare(Path.of("users.txt")); }
    @Override public void start(Stage stage) throws IOException { open(stage, prepared, getParameters().getRaw()); }

    static Prepared prepare(Path file) throws IOException {
        if (Platform.isFxApplicationThread()) throw new IllegalStateException("Read files off the UI thread.");
        List<User> users;
        boolean error = false;
        try { users = UserRepository.load(file, System.err); }
        catch (IOException failure) {
            System.err.println("Could not read users file: " + failure.getMessage());
            users = List.of(); error = true;
        }
        return new Prepared(List.copyOf(users), resource("login.fxml"), resource("welcome.fxml"), error);
    }

    private static String resource(String name) throws IOException {
        try (InputStream input = LoginApp.class.getResourceAsStream(name)) {
            if (input == null) throw new IOException("Missing view: " + name);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    static ByteArrayInputStream viewInput(String xml) {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }

    void open(Stage stage, Prepared data, List<String> arguments) throws IOException {
        stage.setOnCloseRequest(event -> { stop(); Platform.exit(); });
        stage.setMinWidth(500); stage.setMinHeight(440);
        try { showLogin(stage, data, LoginConfig.parse(arguments)); }
        catch (IllegalArgumentException error) { showConfiguration(stage, data); }
        stage.show();
    }

    private void showConfiguration(Stage stage, Prepared data) {
        Label title = new Label("הגדרת מדיניות התחברות");
        title.setStyle("-fx-font-size: 23; -fx-font-weight: bold;");
        Label error = new Label("יש להזין שני מספרים שלמים חיוביים: n ו-t");
        error.setId("configError"); error.setStyle("-fx-text-fill: #b3261e;");
        TextField n = new TextField(); n.setId("nField"); n.setAccessibleText("מספר כישלונות מרבי");
        TextField t = new TextField(); t.setId("tField"); t.setAccessibleText("משך חסימה בשניות");
        Button apply = new Button("פתיחת מסך התחברות"); apply.setId("applyButton"); apply.setDefaultButton(true);
        VBox root = new VBox(13, title, error, new Label("n - מספר כישלונות מרבי"), n,
                new Label("t - משך חסימה בשניות"), t, apply);
        root.setPadding(new Insets(28)); root.setPrefSize(540, 440);
        root.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14; -fx-background-color: white;");
        apply.setOnAction(event -> {
            try { showLogin(stage, data, LoginConfig.parse(List.of(n.getText(), t.getText()))); }
            catch (IllegalArgumentException failure) { error.setText("n ו-t חייבים להיות מספרים שלמים הגדולים מ-0"); }
            catch (IOException failure) { error.setText("לא ניתן לפתוח את מסך ההתחברות"); }
        });
        stage.setTitle("GCM - Configuration"); stage.setScene(new Scene(root));
    }

    private void showLogin(Stage stage, Prepared data, LoginConfig config) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(viewInput(data.loginXml()));
        service = new LoginService(data.users(), config);
        LoginController controller = loader.getController();
        controller.configure(service, data.welcomeXml());
        if (data.fileError()) controller.showFileError();
        stage.setTitle("GCM - Login"); stage.setScene(new Scene(root));
    }

    LoginService service() { return service; }
    @Override public void stop() { if (service != null) service.close(); }
    public static void main(String[] args) { launch(args); }
}
