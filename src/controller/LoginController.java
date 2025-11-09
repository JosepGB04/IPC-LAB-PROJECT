/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.NavDAOException;
import model.Navigation;
import model.User;
import model.UserSession;

/**
 * FXML Controller class
 *
 * @author MSIMONI
 */
public class LoginController implements Initializable {

    @FXML
    private TextField usernameField;
    @FXML
    private Label loginError;
    @FXML
    private Button loginButton;
    @FXML
    private TextField passwordField;
    public boolean LoginSuccessfull;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        LoginSuccessfull = false;
        // enabling closing the window pressing the escape button and login with enter

        usernameField.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        Stage stage = (Stage) usernameField.getScene().getWindow();
                        stage.close();
                    }
                    else if(event.getCode() == KeyCode.ENTER){
                        try{
                            loginButtonClicked(new ActionEvent());
                        }catch(NavDAOException ex){
                            System.out.println("error");
                        }
                    }
                });
            }
        });

    }    

    @FXML
public void loginButtonClicked(ActionEvent event) throws NavDAOException {
    String username = usernameField.getText();
    String password = passwordField.getText();
    
    if (checkLoginCredentials(username, password)) {
        // Get the authenticated user
        User authenticatedUser = Navigation.getInstance().authenticate(username, password);
        if (authenticatedUser == null) {
            loginError.setVisible(true);
            return;
        }
        
        // Set the user in the session
        UserSession.getInstance().setCurrentUser(authenticatedUser);
        
        LoginSuccessfull = true;
        
        try {
            // Load the MainView FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
            Parent mainView = loader.load();
            
            // Create a new scene with the MainView
            Scene scene = new Scene(mainView);
            
            // Get the current stage from the event
            Stage stage = (Stage) loginButton.getScene().getWindow();
            
            // Set the new scene to the existing stage
            stage.setScene(scene);
            stage.setTitle("Nautical Navigation System");
            stage.setMaximized(true);
            
            // Display the main view
            stage.show();
            
        } catch (IOException e) {
            System.err.println("Error loading MainView: " + e.getMessage());
            e.printStackTrace();
        }
    } else {
        loginError.setVisible(true);
        LoginSuccessfull = false;
    }
}

private boolean checkLoginCredentials(String username, String password) throws NavDAOException {
    // 1. admin user
    if (username.equals("admin") && password.equals("admin")) {
        return true;
    }

    // 2. search rest of domains
    else{
        Navigation nav = Navigation.getInstance();
        User located = nav.authenticate(username, password);
        if(located!=null){return true;}
    
    }

    return false; // user was not found..
}


    @FXML
    private void registerLink(ActionEvent event) throws IOException {
        // Close the current window
        // Mattia: I commented the following two lines because otherwise after clicking exit from login
        //          it redirects you to the mainView. So I think is easier to do the register in a pop up window,
        //          saving the data and then let the user redo the login.
        //Stage currentStage = (Stage) usernameField.getScene().getWindow();
        //currentStage.close();
        
        // Open register window
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RegisterView.fxml"));
        Parent root = loader.load();
        //RegisterViewController controller = loader.getController();
        
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("register");
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.showAndWait();
    }
    
}
