package com.example.moneymanager;

import com.example.moneymanager.DBConnection.DBHandler;
import javafx.animation.PauseTransition;
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
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Scanner;

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

    @FXML
    private ImageView progress;

    @FXML
    private Text successAlert;

    @FXML
    private Text notCorrectAlert;

    @FXML
    private CheckBox rememberMeBox;

    private Connection connection;
    private DBHandler handler;
    private PreparedStatement pst;
    private boolean rememberMe;
    private static String saveEmail;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        progress.setVisible(false);
        notCorrectAlert.setVisible(false);
        successAlert.setVisible(false);
        email.setStyle("-fx-background-color: #555555; -fx-text-fill: #b9b8b8");
        password.setStyle("-fx-background-color: #555555; -fx-text-fill: #b9b8b8");
        handler = new DBHandler();

        try {
            email.requestFocus();
            String path = new File(".").getCanonicalPath();
            File file = new File(path + File.separator + "RememberMe.txt");
            Scanner scanner = new Scanner(file);
            String buffEmail = "", buffPassword = "";
            if(scanner.hasNextLine()) {
                buffEmail = scanner.nextLine();
            }
            if(scanner.hasNextLine()) {
                buffPassword = scanner.nextLine();
            }
            scanner.close();
            if(buffEmail != "" && buffPassword != ""){
                email.requestFocus();
                email.requestFocus();
                email.setText(buffEmail);
                email.requestFocus();
                password.setText(buffPassword);;
                rememberMeBox.setSelected(true);
                rememberMe = true;

                ActionEvent e = null;
                loginAction(e);
            }else{
                rememberMeBox.setSelected(false);
                rememberMe = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        rememberMeBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                System.out.println("Yes");
                rememberMe = true;
            }else{
                System.out.println("No");
                rememberMe = false;
            }
        });
    }

    @FXML
    public void loginAction(ActionEvent e){
        notCorrectAlert.setVisible(false);
        successAlert.setVisible(false);
        progress.setVisible(true);
        PauseTransition pt = new PauseTransition();
        pt.setDuration(Duration.seconds(2));
        pt.setOnFinished(ev ->{

            try {
                String path = new File(".").getCanonicalPath();
                connection = handler.getConnection();
                String request = "SELECT * from users where email=? and password=?";
                pst = connection.prepareStatement(request);
                String buffEmail, buffPassword;
                buffEmail = email.getText();
                buffPassword = password.getText();
                pst.setString(1, buffEmail);
                pst.setString(2, buffPassword);
                ResultSet rs = pst.executeQuery();
                if(rs.next()){
                    System.out.println("Login successful");
                    progress.setVisible(false);
                    successAlert.setVisible(true);
                    saveEmail = buffEmail;
                    try {
                        FileWriter writer;
                        if(rememberMe == true){
                            writer = new FileWriter(path + File.separator + "RememberMe.txt");
                            writer.write(buffEmail + "\n");
                            writer.write(buffPassword + "\n");
                        }else{
                            writer = new FileWriter(path + File.separator + "RememberMe.txt", false);
                        }
                        writer.close();
                    }catch (IOException e1){
                        e1.printStackTrace();
                    }
                    PauseTransition pt1 = new PauseTransition();
                    pt1.setDuration(Duration.seconds(1));
                    pt1.setOnFinished(ev1 -> {
                        login.getScene().getWindow().hide();
                        Stage homeStage = new Stage();
                        try{
                            Parent root = FXMLLoader.load(getClass().getResource("MainWindowNew.fxml"));
                            Scene scene = new Scene(root, 1000, 600);

                            //homeStage.setMaximized(true);
                            homeStage.setScene(scene);
                            homeStage.setTitle("MoneyManager v1.5");
                            homeStage.setResizable(false);
                            homeStage.show();
                        }catch(IOException e1){
                            e1.printStackTrace();
                        }
                    });
                    pt1.play();
                }else{
                    System.out.println("Username or Password is not correct");
                    progress.setVisible(false);
                    notCorrectAlert.setVisible(true);
                }
                connection.close();
            }catch (SQLException e1){
                e1.printStackTrace();
            } catch (FileNotFoundException e2) {
                e2.printStackTrace();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
        });
        pt.play();
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

    public static String getSaveEmail(){
        return saveEmail;
    }
}