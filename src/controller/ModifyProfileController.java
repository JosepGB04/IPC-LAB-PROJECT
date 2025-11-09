package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Navigation;
import model.User;
import model.UserSession;
import javafx.scene.shape.Circle;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;
import javafx.scene.paint.Color;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ModifyProfileController implements Initializable {

    @FXML
    private Label usernameLabel;

    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField emailField;

    @FXML
    private DatePicker dobPicker;

    @FXML
    private ImageView profilePicture;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;

    @FXML
    private Label profileModStat;

    private String username; // Store the username

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Get the currently authenticated user from the session
        User currentUser = UserSession.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Set the username from the authenticated user
            if (usernameLabel != null) {
                usernameLabel.setText(currentUser.getNickName());
            }
            
            // Pre-fill the fields with user data
            if (emailField != null && currentUser.getEmail() != null) {
                emailField.setText(currentUser.getEmail());
            }
            
            if (dobPicker != null && currentUser.getBirthdate() != null) {
                dobPicker.setValue(currentUser.getBirthdate());
            }

            if (profilePicture != null && currentUser.getAvatar() != null) {
                profilePicture.setImage(currentUser.getAvatar());
            }
        } else {
            // Handle case where no user is logged in
            if (usernameLabel != null) {
                usernameLabel.setText("User");
            }
        }
        
        // Set up circular clipping for profile image
        Circle clip = new Circle(50, 50, 50);
        profilePicture.setClip(clip);
        
        // Clear status message initially
        if (profileModStat != null) {
            profileModStat.setVisible(false);
        }
        
        // Add enter key handler for saving profile
        if (confirmPasswordField != null) {
            confirmPasswordField.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER")) {
                    saveProfile();
                }
            });
        }
    }

    public void setUsername(String username) {
        this.username = username;
        if (usernameLabel != null) {
            usernameLabel.setText(username); // Set the username in the header
        }
    }

    @FXML
    private void saveProfile() {
        // Get the current authenticated user
        User currentUser = UserSession.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            profileModStat.setText("Error: No user logged in");
            profileModStat.setStyle("-fx-text-fill: red;");
            profileModStat.setVisible(true);
            return;
        }
        
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String email = emailField.getText();
        LocalDate dob = dobPicker.getValue();
        
        boolean hasError = false;

        // Check if password is provided and validate it
        if (password != null && !password.isEmpty()) {
            if (!isValidPassword(password)) {
                profileModStat.setText("Password must be at least 8 characters!");
                profileModStat.setStyle("-fx-text-fill: red;");
                hasError = true;
            } else if (!password.equals(confirmPassword)) {
                profileModStat.setText("Passwords do not match!");
                profileModStat.setStyle("-fx-text-fill: red;");
                hasError = true;
            }
        }

        // Check if email is provided and validate it
        if (!hasError && email != null && !email.isEmpty() && !isValidEmail(email)) {
            profileModStat.setText("Invalid email format!");
            profileModStat.setStyle("-fx-text-fill: red;");
            hasError = true;
        }

        // Check if date of birth is provided and validate it
        if (!hasError && dob != null && !isValidAge(dob)) {
            profileModStat.setText("You must be at least 12 years old!");
            profileModStat.setStyle("-fx-text-fill: red;");
            hasError = true;
        }

        // If all validations pass, update the user object
        if (!hasError) {
            try {
                // Update user data
                if (password != null && !password.isEmpty()) {
                    currentUser.setPassword(password);
                }
                
                if (email != null && !email.isEmpty()) {
                    currentUser.setEmail(email);
                }
                
                if (dob != null) {
                    currentUser.setBirthdate(dob);
                }
                
                // Show success message
                profileModStat.setText("Profile saved successfully!");
                profileModStat.setStyle("-fx-text-fill: green;");
            } catch (Exception e) {
                profileModStat.setText("Error saving profile: " + e.getMessage());
                profileModStat.setStyle("-fx-text-fill: red;");
            }
        }
        
        profileModStat.setVisible(true);
    }

    @FXML
    private void cancel() {
        // Close the current window
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void uploadProfilePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File selectedFile = fileChooser.showOpenDialog(profilePicture.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Load the original image
                Image originalImage = new Image(selectedFile.toURI().toString());
                
                // Create a square cropped version
                Image croppedImage = cropImageToSquare(originalImage);
                
                // Set the cropped image to the ImageView
                profilePicture.setImage(croppedImage);
                
                // Store the image in the current user
                User currentUser = UserSession.getInstance().getCurrentUser();
                if (currentUser != null) {
                    currentUser.setAvatar(croppedImage);
                }
                
                System.out.println("Profile picture updated: " + selectedFile.getName());
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Crops an image to a square shape by taking the center portion.
     * 
     * @param original The original image
     * @return A new Image that is square, containing the center portion of the original
     */
    private Image cropImageToSquare(Image original) {
        // Determine the side length of the square (use the smaller dimension)
        double size = Math.min(original.getWidth(), original.getHeight());
        
        // Calculate coordinates for the center crop
        double x = (original.getWidth() - size) / 2;
        double y = (original.getHeight() - size) / 2;
        
        // Create a WritableImage to hold the cropped result
        WritableImage croppedImage = new WritableImage((int)size, (int)size);
        
        // Create a canvas for drawing
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Draw the portion of the original image that we want to keep
        gc.drawImage(original, x, y, size, size, 0, 0, size, size);
        
        // Render the canvas to our WritableImage
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        canvas.snapshot(params, croppedImage);
        
        return croppedImage;
    }

    private boolean isValidPassword(String password) {
        // Password must be at least 8 characters
        return password.length() >= 8;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(emailRegex, email);
    }

    private boolean isValidAge(LocalDate dob) {
        return Period.between(dob, LocalDate.now()).getYears() >= 12;
    }
}