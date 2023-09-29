package com.example.moneymanager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

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
    private CheckBox remember;

    @FXML
    private Button signUp;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        email.setStyle("-fx-background-color: #555555; -fx-text-fill: #b9b8b8");
        password.setStyle("-fx-background-color: #555555; -fx-text-fill: #b9b8b8");
    }

    @FXML
    public void loginAction(ActionEvent e){
        System.out.println("Login successful");
    }

    @FXML
    public void signUp(ActionEvent e1) throws IOException {
        System.out.println("Open signUP window");
        login.getScene().getWindow().hide();
        Stage signUp = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("SignUpWindow.fxml"));
        Scene scene = new Scene(root);
        signUp.setTitle("Authorization");
        signUp.setScene(scene);
        signUp.show();
        signUp.setResizable(false);
    }
}
