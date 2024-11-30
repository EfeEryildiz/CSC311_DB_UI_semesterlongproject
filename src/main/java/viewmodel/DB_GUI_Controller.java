package viewmodel;

import dao.DbConnectivityClass;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Person;
import service.MyLogger;
import service.UserSession;

import java.io.*;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class DB_GUI_Controller implements Initializable {

    @FXML
    private TextField first_name, last_name, department, email, imageURL;
    @FXML
    private ComboBox<Major> majorComboBox;
    @FXML
    private ImageView img_view;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Button editBtn, deleteBtn, addBtn;
    @FXML
    private MenuItem editMenuItem;
    @FXML
    private MenuItem deleteMenuItem;
    @FXML
    private Label statusLabel;
    @FXML
    private HBox statusBar;
    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;

    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();
    private final MyLogger logger = new MyLogger();

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(.+)$"
    );
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[A-Za-z\\s\\-']{2,50}$"
    );
    private static final Pattern DEPARTMENT_PATTERN = Pattern.compile(
            "^[A-Za-z\\s\\-&]{2,50}$"
    );

    public enum Major {
        COMPUTER_SCIENCE("Computer Science"),
        CPIS("Computer Information Systems"),
        ENGLISH("English"),
        MATHEMATICS("Mathematics"),
        BUSINESS("Business");

        private final String displayName;

        Major(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public static Major fromString(String text) {
            for (Major m : Major.values()) {
                if (m.displayName.equalsIgnoreCase(text)) {
                    return m;
                }
            }
            throw new IllegalArgumentException("No major with display name: " + text);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Initialize table columns
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);

            // Initialize major ComboBox
            majorComboBox.setItems(FXCollections.observableArrayList(Major.values()));
            majorComboBox.getSelectionModel().selectFirst();

            // Setup UI bindings
            setupUIBindings();

            // Initialize status bar
            statusLabel.setText("");

            // Add tooltips
            addBtn.setTooltip(new Tooltip("Add a new record"));
            editBtn.setTooltip(new Tooltip("Edit selected record"));
            deleteBtn.setTooltip(new Tooltip("Delete selected record"));

        } catch (Exception e) {
            showError("Initialization Error", e.getMessage());
            logger.makeLog("Initialization failed: " + e.getMessage());
        }
    }

    private void setupUIBindings() {
        // Bind button and menu item states to table selection
        editBtn.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
        deleteBtn.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
        editMenuItem.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
        deleteMenuItem.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());

        // Bind Add button state to form validation
        addBtn.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> !isValidInput(),
                        first_name.textProperty(),
                        last_name.textProperty(),
                        email.textProperty(),
                        department.textProperty(),
                        majorComboBox.valueProperty()
                )
        );
    }

    private boolean isValidInput() {
        return NAME_PATTERN.matcher(first_name.getText()).matches() &&
                NAME_PATTERN.matcher(last_name.getText()).matches() &&
                EMAIL_PATTERN.matcher(email.getText()).matches() &&
                DEPARTMENT_PATTERN.matcher(department.getText()).matches() &&
                majorComboBox.getValue() != null;
    }

    @FXML
    protected void addNewRecord() {
        try {
            if (!isValidInput()) {
                showError("Invalid Input", "Please check the form fields for errors.");
                return;
            }

            Person p = new Person(
                    first_name.getText(),
                    last_name.getText(),
                    department.getText(),
                    majorComboBox.getValue().toString(),
                    email.getText(),
                    imageURL.getText()
            );

            cnUtil.insertUser(p);
            p.setId(cnUtil.retrieveId(p));
            data.add(p);
            clearForm();
            showStatus("Record added successfully!");
            logger.makeLog("New record added: " + p);

        } catch (Exception e) {
            showError("Add Record Error", e.getMessage());
            logger.makeLog("Failed to add record: " + e.getMessage());
        }
    }

    @FXML
    protected void editRecord() {
        try {
            if (!isValidInput()) {
                showError("Invalid Input", "Please check the form fields for errors.");
                return;
            }

            Person selectedPerson = tv.getSelectionModel().getSelectedItem();
            if (selectedPerson == null) {
                showError("Selection Error", "Please select a record to edit.");
                return;
            }

            Person updatedPerson = new Person(
                    first_name.getText(),
                    last_name.getText(),
                    department.getText(),
                    majorComboBox.getValue().toString(),
                    email.getText(),
                    imageURL.getText()
            );

            cnUtil.editUser(selectedPerson.getId(), updatedPerson);

            int index = data.indexOf(selectedPerson);
            updatedPerson.setId(selectedPerson.getId());
            data.set(index, updatedPerson);

            showStatus("Record updated successfully!");
            logger.makeLog("Record updated: " + updatedPerson);

        } catch (Exception e) {
            showError("Edit Record Error", e.getMessage());
            logger.makeLog("Failed to edit record: " + e.getMessage());
        }
    }

    @FXML
    protected void deleteRecord() {
        try {
            Person selectedPerson = tv.getSelectionModel().getSelectedItem();
            if (selectedPerson == null) {
                showError("Selection Error", "Please select a record to delete.");
                return;
            }

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Delete Confirmation");
            confirmation.setHeaderText("Delete Record");
            confirmation.setContentText("Are you sure you want to delete this record?");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                cnUtil.deleteRecord(selectedPerson);
                data.remove(selectedPerson);
                clearForm();
                showStatus("Record deleted successfully!");
                logger.makeLog("Record deleted: " + selectedPerson);
            }
        } catch (Exception e) {
            showError("Delete Record Error", e.getMessage());
            logger.makeLog("Failed to delete record: " + e.getMessage());
        }
    }

    @FXML
    protected void importCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import CSV File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                int importCount = 0;
                // Skip header
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    if (fields.length >= 6) {
                        Person p = new Person(
                                fields[0].trim(), // firstName
                                fields[1].trim(), // lastName
                                fields[2].trim(), // department
                                fields[3].trim(), // major
                                fields[4].trim(), // email
                                fields[5].trim()  // imageURL
                        );
                        cnUtil.insertUser(p);
                        p.setId(cnUtil.retrieveId(p));
                        data.add(p);
                        importCount++;
                    }
                }
                showStatus(String.format("Successfully imported %d records!", importCount));
                logger.makeLog("CSV import completed: " + importCount + " records");
            } catch (IOException e) {
                showError("Import Error", "Failed to import CSV file: " + e.getMessage());
                logger.makeLog("CSV import failed: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void exportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export CSV File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(menuBar.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Write header
                writer.println("First Name,Last Name,Department,Major,Email,ImageURL");

                // Write data
                for (Person p : data) {
                    writer.println(String.format("%s,%s,%s,%s,%s,%s",
                            p.getFirstName(),
                            p.getLastName(),
                            p.getDepartment(),
                            p.getMajor(),
                            p.getEmail(),
                            p.getImageURL()
                    ));
                }
                showStatus("Data exported successfully!");
                logger.makeLog("CSV export completed: " + data.size() + " records");
            } catch (IOException e) {
                showError("Export Error", "Failed to export CSV file: " + e.getMessage());
                logger.makeLog("CSV export failed: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void clearForm() {
        first_name.clear();
        last_name.clear();
        department.clear();
        email.clear();
        imageURL.clear();
        majorComboBox.getSelectionModel().selectFirst();
        tv.getSelectionModel().clearSelection();
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            first_name.setText(p.getFirstName());
            last_name.setText(p.getLastName());
            department.setText(p.getDepartment());
            email.setText(p.getEmail());
            imageURL.setText(p.getImageURL());
            majorComboBox.setValue(Major.fromString(p.getMajor()));
        }
    }

    @FXML
    protected void showImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            try {
                Image image = new Image(file.toURI().toString());
                img_view.setImage(image);
                imageURL.setText(file.toURI().toString());
            } catch (Exception e) {
                showError("Image Error", "Failed to load image: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            UserSession.getInstance("", "").cleanUserSession();
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
            logger.makeLog("User logged out");
        } catch (Exception e) {
            showError("Logout Error", e.getMessage());
            logger.makeLog("Logout failed: " + e.getMessage());
        }
    }

    @FXML
    protected void closeApplication() {
        Platform.exit();
    }

    private void showStatus(String message) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            // Clear status after 3 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    Platform.runLater(() -> statusLabel.setText(""));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });
    }

    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    @FXML
    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            showStatus("Light theme applied");
        } catch (Exception e) {
            showError("Theme Error", "Failed to apply light theme: " + e.getMessage());
            logger.makeLog("Theme change failed: " + e.getMessage());
        }
    }

    @FXML
    public void darkTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
            showStatus("Dark theme applied");
        } catch (Exception e) {
            showError("Theme Error", "Failed to apply dark theme: " + e.getMessage());
            logger.makeLog("Theme change failed: " + e.getMessage());
        }
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 400);
            stage.setTitle("About");
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            showError("Display Error", "Failed to show About window: " + e.getMessage());
            logger.makeLog("Failed to show About window: " + e.getMessage());
        }
    }

    @FXML
    protected void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("Quick Add User");
        dialog.setHeaderText("Enter Basic Information");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Create and configure input fields
        TextField nameField = new TextField();
        nameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        ComboBox<Major> majorSelector = new ComboBox<>(FXCollections.observableArrayList(Major.values()));
        majorSelector.getSelectionModel().selectFirst();
        majorSelector.setPromptText("Select Major");

        // Layout the dialog
        dialogPane.setContent(new VBox(10,
                new Label("First Name:"), nameField,
                new Label("Last Name:"), lastNameField,
                new Label("Email:"), emailField,
                new Label("Major:"), majorSelector
        ));

        // Enable/Disable OK button based on input validation
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> !isQuickAddValid(nameField.getText(), lastNameField.getText(), emailField.getText()),
                        nameField.textProperty(),
                        lastNameField.textProperty(),
                        emailField.textProperty()
                )
        );

        // Convert dialog result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Results(
                        nameField.getText(),
                        lastNameField.getText(),
                        majorSelector.getValue()
                );
            }
            return null;
        });

        Optional<Results> result = dialog.showAndWait();
        result.ifPresent(this::processQuickAdd);
    }

    private boolean isQuickAddValid(String firstName, String lastName, String email) {
        return NAME_PATTERN.matcher(firstName).matches() &&
                NAME_PATTERN.matcher(lastName).matches() &&
                EMAIL_PATTERN.matcher(email).matches();
    }

    private void processQuickAdd(Results results) {
        try {
            Person p = new Person(
                    results.fname,
                    results.lname,
                    "TBD", // Default department
                    results.major.toString(),
                    results.fname.toLowerCase() + "." + results.lname.toLowerCase() + "@example.com",
                    "" // Default empty image URL
            );

            cnUtil.insertUser(p);
            p.setId(cnUtil.retrieveId(p));
            data.add(p);
            showStatus("Quick add completed successfully!");
            logger.makeLog("Quick add completed: " + p);

        } catch (Exception e) {
            showError("Quick Add Error", "Failed to add user: " + e.getMessage());
            logger.makeLog("Quick add failed: " + e.getMessage());
        }
    }

    private static class Results {
        String fname;
        String lname;
        Major major;

        public Results(String firstName, String lastName, Major major) {
            this.fname = firstName;
            this.lname = lastName;
            this.major = major;
        }
    }
    @FXML
    private void refreshTable() {
        try {
            data.clear();
            data.addAll(cnUtil.getData());
            showStatus("Table data refreshed successfully");
            logger.makeLog("Table refresh performed");
        } catch (Exception e) {
            showError("Refresh Error", "Unable to refresh table data: " + e.getMessage());
            logger.makeLog("Table refresh failed: " + e.getMessage());
        }
    }

    @FXML
    private void showUserGuide() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/userguide.fxml"));
            Parent root = loader.load();

            Stage guideStage = new Stage();
            guideStage.setTitle("Student Management System - User Guide");
            guideStage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());

            guideStage.setScene(scene);
            guideStage.show();

            logger.makeLog("User guide displayed");
        } catch (IOException e) {
            showError("Guide Error", "Unable to display user guide: " + e.getMessage());
            logger.makeLog("Failed to show user guide: " + e.getMessage());
        }
    }
    @FXML
    private void openSupportLink(ActionEvent event) {
        try {
            java.awt.Desktop.getDesktop().browse(
                    new java.net.URI("https://www.farmingdale.edu/information-technology/helpdesk.shtml")
            );
        } catch (Exception e) {
            showError("Link Error", "Unable to open support website: " + e.getMessage());
            logger.makeLog("Failed to open support link: " + e.getMessage());
        }
    }
}