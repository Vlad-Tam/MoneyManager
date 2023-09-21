package com.example.moneymanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("LoginWindow.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 550, 350);
        stage.setTitle("Authorization");
        stage.setScene(scene);
        stage.show();

//        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("SignUpWindow.fxml"));
//        Scene scene = new Scene(fxmlLoader.load(), 550, 350);
//        stage.setTitle("Authorization");
//        stage.setScene(scene);
//        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}