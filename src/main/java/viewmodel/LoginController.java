package viewmodel;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import service.UserSession;

public class LoginController {

    @FXML
    private GridPane rootpane;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordField;

    // Default admin credentials
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin123";

    public void initialize() {
        rootpane.setOpacity(0);
        FadeTransition fadeOut2 = new FadeTransition(Duration.seconds(10), rootpane);
        fadeOut2.setFromValue(0);
        fadeOut2.setToValue(1);
        fadeOut2.play();

        // Check for stored credentials
        if (UserSession.hasStoredCredentials()) {
            UserSession storedSession = UserSession.restoreSession();
            if (storedSession != null) {
                usernameTextField.setText(storedSession.getUserName());
                // Don't pre-fill password for security reasons
            }
        }
    }

    @FXML
    public void login(ActionEvent actionEvent) {
        String username = usernameTextField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        // Check credentials
        if (isValidCredentials(username, password)) {
            try {
                // Create user session
                UserSession.getInstance(username, password, "ADMIN");

                // Load main application window
                Parent root = FXMLLoader.load(getClass().getResource("/view/db_interface_gui.fxml"));
                Scene scene = new Scene(root, 900, 600);
                scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
                Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                window.setScene(scene);
                window.show();
            } catch (Exception e) {
                showError("Error loading application: " + e.getMessage());
            }
        } else {
            showError("Invalid username or password.");
        }
    }

    private boolean isValidCredentials(String username, String password) {
        // Check default admin credentials
        if (username.equals(DEFAULT_USERNAME) && password.equals(DEFAULT_PASSWORD)) {
            return true;
        }

        // For now, allow test/test123 as an additional login
        return username.equals("test") && password.equals("test123");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void signUp(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/signUp.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            showError("Error loading sign up page: " + e.getMessage());
        }
    }
}