package com.example.moneymanager;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class ChangeLimitController implements Initializable {
    private static float newLimit;

    @FXML
    private TextField newLimitField;

    @FXML
    private ImageView progress;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        progress.setVisible(false);

    }

    @FXML
    void applyButtonAction(ActionEvent event) {
        String buffValue = newLimitField.getText();
        if (buffValue.matches("^[0-9\\.]*$") && buffValue.length() != 0) {
            newLimit = Float.parseFloat(buffValue);
            progress.setVisible(true);
            PauseTransition pt = new PauseTransition();
            pt.setDuration(Duration.seconds(1));
            pt.setOnFinished(ev ->{
                this.progress.getScene().getWindow().hide();
            });
            pt.play();

        }else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Input error");
            alert.setHeaderText(null);
            alert.setContentText("Invalid value entered. Please enter a number.");
            alert.getDialogPane().getStylesheets().add(Main.class.getResource("alert.css").toExternalForm());
            alert.showAndWait();
        }
        newLimitField.clear();
    }

    public static float getNewLimit(){
        return newLimit;
    }
}
