package com.example.moneymanager;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SignUpController implements Initializable {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField email;

    @FXML
    private Button login;

    @FXML
    private PasswordField password;

    @FXML
    private Button signUp;

    @FXML
    private TextField username;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        username.setStyle("-fx-background-color: #555555; -fx-text-fill: #b9b8b8");
        email.setStyle("-fx-background-color: #555555; -fx-text-fill: #b9b8b8");
        password.setStyle("-fx-background-color: #555555; -fx-text-fill: #b9b8b8");
    }

    @FXML
    public void signUpAction(ActionEvent e){
        System.out.println("SignUP successful");
    }

    @FXML
    public void loginAction(ActionEvent e1) throws IOException {
        System.out.println("Open login window");
        signUp.getScene().getWindow().hide();
        Stage login = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("LoginWindow.fxml"));
        Scene scene = new Scene(root);
        login.setTitle("Authorization");
        login.setScene(scene);
        login.show();
        login.setResizable(false);
    }
}
