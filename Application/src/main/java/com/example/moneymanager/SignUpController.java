package com.example.moneymanager;

import com.example.moneymanager.DBConnection.DBHandler;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import static java.lang.Thread.sleep;


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

    @FXML
    private ImageView progress;

    @FXML
    private Text successAlert;

    @FXML
    private Text passwordLengthAlert;

    @FXML
    private Text passwordSymbolsAlert;

    @FXML
    private Text existEmailAlert;

    private Connection connection;
    private DBHandler handler;
    private PreparedStatement pst;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        progress.setVisible(false);
        successAlert.setVisible(false);
        passwordLengthAlert.setVisible(false);
        passwordSymbolsAlert.setVisible(false);
        existEmailAlert.setVisible(false);

        username.setStyle("-fx-background-color: #555555; -fx-text-fill: #b9b8b8");
        email.setStyle("-fx-background-color: #555555; -fx-text-fill: #b9b8b8");
        password.setStyle("-fx-background-color: #555555; -fx-text-fill: #b9b8b8");

        handler = new DBHandler();
    }

    @FXML
    public void signUpAction(ActionEvent e){
        passwordLengthAlert.setVisible(false);
        passwordSymbolsAlert.setVisible(false);
        existEmailAlert.setVisible(false);
        progress.setVisible(true);
        PauseTransition pt = new PauseTransition();
        pt.setDuration(Duration.seconds(2));
        pt.setOnFinished(ev ->{
            String insert = "INSERT INTO users(name,email,password)" + "VALUES (?,?,?)";
            connection = handler.getConnection();
            try {
                String buffEmail = email.getText();
                String buffPassword = password.getText();
                if(checkEmail(buffEmail) == false) {
                    return;
                }
                if(checkPassword(buffPassword) == false){
                    return;
                }
                pst = connection.prepareStatement(insert);
                pst.setString(1, username.getText());
                pst.setString(2, email.getText());
                pst.setString(3, password.getText());
                pst.executeUpdate();
                progress.setVisible(false);

                System.out.println("SignUP successful");
                successAlert.setVisible(true);
                PauseTransition pt1 = new PauseTransition();
                pt1.setDuration(Duration.seconds(1));
                pt1.setOnFinished(ev1 -> {
                    ActionEvent event = null;
                    try {
                        loginAction(event);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });
                pt1.play();
            }catch (SQLException e1){
                e1.printStackTrace();
            }
        });
        pt.play();
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

    public boolean checkEmail(String emailStr){
        String request = "SELECT * from users where email=?";
        try {
            PreparedStatement pst1 = connection.prepareStatement(request);
            pst1.setString(1, email.getText());
            ResultSet rs = pst1.executeQuery();
            int counter = 0;
            while (rs.next()) {
                counter++;
            }
            if(counter == 0){
                return true;
            }else{
                progress.setVisible(false);
                existEmailAlert.setVisible(true);
                return false;
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkPassword(String passwordStr){
        if(passwordStr.length() >= 8){
            if(passwordStr.matches("[a-zA-Z0-9]+")){
                return true;
            }else{
                progress.setVisible(false);
                passwordSymbolsAlert.setVisible(true);
                return false;
            }
        }else{
            progress.setVisible(false);
            passwordLengthAlert.setVisible(true);
            return false;
        }
    }
}
