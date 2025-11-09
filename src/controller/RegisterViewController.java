/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;
import java.io.File;
import javafx.scene.image.ImageView;
import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.NavDAOException;
import model.Navigation;
import model.User;

public class RegisterViewController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private Label usernameError;

    @FXML private PasswordField passwordField;
    @FXML private Label passwordError;

    @FXML private TextField emailField;
    @FXML private Label emailError;
    @FXML private PasswordField passwordField2;
    @FXML private Label passwordsdontmatch;

    @FXML private DatePicker datePicker;
    @FXML private Label birthdateError;
    
    @FXML private Button avatarButton;
    @FXML private ImageView avatarImageView;

    @FXML private Button registerButton;
    @FXML private Label succesfulRegistered;
    
    

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }

    @FXML
    private void handleChangeAvatar(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Avatar Image");

        // Filtros de archivo: solo imágenes
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(imageFilter);

        // Mostrar el diálogo
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
        // Cargar imagen en el ImageView
        Image image = new Image(selectedFile.toURI().toString());
        avatarImageView.setImage(image);
    }
}


    @FXML
    private void handleRegister(ActionEvent event) throws NavDAOException {
        // Clear all error labels first
        usernameError.setVisible(false);
        passwordError.setVisible(false);
        emailError.setVisible(false);
        birthdateError.setVisible(false);
        passwordsdontmatch.setVisible(false);

        boolean isValid = true;

        // Validate username
        String username = usernameField.getText().trim();
        if (!username.matches("^[a-zA-Z0-9_-]{6,15}$")) {
            usernameError.setVisible(true);
            isValid = false;
        }
   

        // Validate password
        String password = passwordField.getText();
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%&*()\\-+=])[A-Za-z\\d!@#$%&*()\\-+=]{8,20}$")) {
            passwordError.setVisible(true);
            isValid = false;
        }
        String password2 = passwordField2.getText();
        if(!password2.equals(password)){
            passwordsdontmatch.setVisible(true);
        isValid = false;
        }
        

        // Validate email
        String email = emailField.getText().trim();
        if (!Pattern.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", email)) {
            emailError.setVisible(true);
            isValid = false;
        }

        // Validate birthdate (must be at least 16 years old)
        LocalDate birthDate = datePicker.getValue();
        if (birthDate == null || Period.between(birthDate, LocalDate.now()).getYears() < 16) {
            birthdateError.setVisible(true);
            isValid = false;
        }

        // If all inputs are valid
        if (isValid) {
    try {
        // Obtener los datos
        username = usernameField.getText().trim();
        password = passwordField.getText();
        email = emailField.getText().trim();
        birthDate = datePicker.getValue();
        Image avatar = avatarImageView.getImage();

        // call nav to register user
        Navigation nav = Navigation.getInstance();
        
        if (nav.exitsNickName(username)) {
            usernameError.setText("Username already in use.");
            usernameError.setVisible(true);
            return;
        }

        User nuevoUsuario = nav.registerUser(username, email, password, avatar, birthDate);

        if (nuevoUsuario != null) {
            succesfulRegistered.setVisible(true);

            //give user time to read message of success
            PauseTransition delay = new PauseTransition(javafx.util.Duration.seconds(3));
            delay.setOnFinished(e -> {
                Stage stage = (Stage) registerButton.getScene().getWindow();
                stage.close();
            });
            delay.play();
        } else {
            
            succesfulRegistered.setText("There was an error.");
            succesfulRegistered.setVisible(true);
            
            }
    } catch (NavDAOException e) {
        e.printStackTrace();
        succesfulRegistered.setText("Error al acceder a la base de datos.");
        succesfulRegistered.setVisible(true);
    }
        }}}
    


    


