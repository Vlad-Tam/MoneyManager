package com.example.moneymanager;

import com.example.moneymanager.DBConnection.DBHandler;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXRippler;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.event.ActionEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable{

    private String userEmail;
    private int userId;
    private BigDecimal userBalance;
    private Connection connection;
    private DBHandler handler;
    private PreparedStatement pst;
    private float maxMonthExpense;
    private LocalDate currentDate;
    private int daysInMonth;
    private float monthExpenses;
    private float[] dateExpensesList;
    private float spentToday = 0;

    XYChart.Series series;
    XYChart.Series maxMonthExpenseSeries;

    @FXML
    private LineChart<?, ?> lineChart;

    @FXML
    private BarChart<?, ?> barChartMonth;

    @FXML
    AnchorPane home;

    @FXML
    private Button settingsButton;

    @FXML
    private VBox vboxContainer;

    @FXML
    private Button changeLimitButton;

    @FXML
    private Button deleteExpenseButton;

    @FXML
    private Button btnAppInfo;

    @FXML
    private Button btnLogOut;

    @FXML
    private Button btnExit;

    @FXML
    private DatePicker dateField;

    @FXML
    private TextField valueField;

    @FXML
    private ChoiceBox<String> categoryField;

    @FXML
    private Text monthLimitWarnText;

    @FXML
    private Text spentTodayText;

    @FXML
    private Text spentTodayWarnText;

    @FXML
    private TextField newLimitField;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        vboxContainer.setVisible(false);
        lineChart.getStylesheets().add(getClass().getResource("chart.css").toExternalForm());
        categoryField.getStylesheets().add(getClass().getResource("inputFields.css").toExternalForm());
        dateField.getStylesheets().add(getClass().getResource("inputFields.css").toExternalForm());
        valueField.getStylesheets().add(getClass().getResource("inputFields.css").toExternalForm());

        dateField.setValue(LocalDate.now());

        userEmail = LoginController.getSaveEmail();
        handler = new DBHandler();
        connection = handler.getConnection();
        String usersRequest = "SELECT * from users where email=?";
        try {
            pst = connection.prepareStatement(usersRequest);
            pst.setString(1, userEmail);
            ResultSet rs = pst.executeQuery();
            if(rs.next()) {
                String buffUserId = rs.getString(1);
                String buffUserBalance = rs.getString(5);
                String buffMonthExpense = rs.getString(6);
                System.out.println("buffUserId = " + buffUserId);
                System.out.println("buffUserBalance = " + buffUserBalance);
                System.out.println("buffMonthExpense = " + buffMonthExpense);
                maxMonthExpense = Float.parseFloat(buffMonthExpense);
                userId = Integer.parseInt(buffUserId);

                series = new XYChart.Series();
                series.setName("Your month expenses");
                //series.getData().add(new XYChart.Data("0", 0));
                lineChart.getData().add(series);
                maxMonthExpenseSeries = new XYChart.Series();
                maxMonthExpenseSeries.setName("Expenses limit");
                maxMonthExpenseSeries.getData().add(new XYChart.Data("1", maxMonthExpense));

                currentDate = LocalDate.now();
                dateField.setValue(LocalDate.now());
                daysInMonth = currentDate.lengthOfMonth();
                System.out.println("DAYSINMONTH = " + daysInMonth);
                maxMonthExpenseSeries.getData().add(new XYChart.Data("" + daysInMonth, maxMonthExpense));
                lineChart.getData().add(maxMonthExpenseSeries);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String categoriesRequest = "SELECT * from categories where user_id = ? OR user_id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(categoriesRequest);
            pstmt.setInt(1, 1);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                String buffCategory = rs.getString(3);
                System.out.println("buffCategory = " + buffCategory);
                categoryField.getItems().add(buffCategory);
                categoryField.setValue(buffCategory);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        String expensesRequest = "SELECT * from expenses where user_id=?";
        try {
            PreparedStatement pst2;
            pst2 = connection.prepareStatement(expensesRequest);
            pst2.setInt(1, userId);
            ResultSet rs2 = pst2.executeQuery();

            this.dateExpensesList = new float[daysInMonth];
            while(rs2.next()) {
                String buffCategory = rs2.getString(3);
                System.out.println("buffCategoryNum = " + buffCategory);
                java.sql.Date sqlDate = rs2.getDate(4);
                LocalDate buffExpenseDate = sqlDate.toLocalDate();
                String buffExpenseAmount = rs2.getString(5);
                if(currentDate.getYear() == buffExpenseDate.getYear() && currentDate.getMonth() == buffExpenseDate.getMonth()){
                    monthExpenses += Float.parseFloat(buffExpenseAmount);
                    dateExpensesList[buffExpenseDate.getDayOfMonth() - 1] += Float.parseFloat(buffExpenseAmount);
                    if(currentDate.getDayOfMonth() == buffExpenseDate.getDayOfMonth()){
                        spentToday += Float.parseFloat(buffExpenseAmount);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        float buffSum = 0;
        for(int i = 0; i < dateExpensesList.length; i++){
            buffSum += dateExpensesList[i];
            series.getData().add(new XYChart.Data("" + (i + 1), buffSum));
        }
        spentTodayText.setText("Spent today " + spentToday + "$");
        spentTodayWarnText.setText("Spent today " + spentToday + "$");
        if(monthExpenses > maxMonthExpense){
            spentTodayText.setVisible(false);
            spentTodayWarnText.setVisible(true);
            monthLimitWarnText.setVisible(true);
        }else{
            spentTodayText.setVisible(true);
            spentTodayWarnText.setVisible(false);
            monthLimitWarnText.setVisible(false);
        }

        openMenu();
        categoryFieldEvent();
    }

    private void categoryFieldEvent(){
        categoryField.setOnAction((event) -> {
            int selectedIndex = categoryField.getSelectionModel().getSelectedIndex();
            String selectedItem = categoryField.getSelectionModel().getSelectedItem();

            System.out.println("Selection made: [" + selectedIndex + "] " + selectedItem);
            System.out.println("ChoiceBox.getValue(): " + categoryField.getValue());
        });
    }

    private void openMenu(){
        JFXPopup pop = new JFXPopup();
        pop.setPopupContent(vboxContainer);
        settingsButton.setOnMouseClicked(e ->{
            if (pop.isShowing()) {
                pop.hide();
                vboxContainer.setVisible(false);
            } else {
                vboxContainer.setVisible(true);
                pop.show(settingsButton, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.RIGHT);
            }
        });
    }

    @FXML
    void addExpenseAction(ActionEvent event) {
        System.out.println("expenseAdding");
        String buffCategory = categoryField.getValue();
        String buffValue = valueField.getText();
        LocalDate selectedDate = dateField.getValue();

        if (buffValue.matches("^[0-9\\.]*$") && buffValue.length() != 0) {
            System.out.println("buffValue = " + buffValue);

            String insert = "INSERT INTO expenses(user_id,category_id,expense_date,expense_amount)" + "VALUES (?,?,?,?)";
            try {
                connection = handler.getConnection();
                pst = connection.prepareStatement(insert);
                pst.setString(1, "" + userId);
                int catIdBuff = switch (buffCategory){
                    case "Products" -> 1;
                    case "Travel" -> 2;
                    case "Entertainment" -> 3;
                    case "Transport" -> 4;
                    case "Technologies" -> 5;
                    default -> 6;
                };
                pst.setString(2, "" + catIdBuff);
                pst.setString(3, selectedDate.toString());
                pst.setString(4, buffValue);
                pst.executeUpdate();

                series.getData().clear();

                String expensesRequest = "SELECT * from expenses where user_id=?";
                PreparedStatement pst;
                pst = connection.prepareStatement(expensesRequest);
                pst.setInt(1, userId);
                ResultSet rs = pst.executeQuery();

                for(int i = 0; i < dateExpensesList.length; i++){
                    dateExpensesList[i] = 0;
                }
                spentToday = 0;
                monthExpenses = 0;

                while(rs.next()) {
                    String buffCategoryNum = rs.getString(3);
                    System.out.println("buffCategoryNum = " + buffCategoryNum);
                    java.sql.Date sqlDate = rs.getDate(4);
                    LocalDate buffExpenseDate = sqlDate.toLocalDate();
                    String buffExpenseAmount = rs.getString(5);
                    if(currentDate.getYear() == buffExpenseDate.getYear() && currentDate.getMonth() == buffExpenseDate.getMonth()){
                        monthExpenses += Float.parseFloat(buffExpenseAmount);
                        dateExpensesList[buffExpenseDate.getDayOfMonth() - 1] += Float.parseFloat(buffExpenseAmount);
                        if(currentDate.getDayOfMonth() == buffExpenseDate.getDayOfMonth()){
                            spentToday += Float.parseFloat(buffExpenseAmount);
                        }
                    }
                }
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            float buffSum = 0;
            for(int i = 0; i < dateExpensesList.length; i++){
                buffSum += dateExpensesList[i];
                series.getData().add(new XYChart.Data("" + (i + 1), buffSum));
            }
            spentTodayText.setText("Spent today " + spentToday + "$");
            spentTodayWarnText.setText("Spent today " + spentToday + "$");
            if(monthExpenses > maxMonthExpense){
                spentTodayText.setVisible(false);
                spentTodayWarnText.setVisible(true);
                monthLimitWarnText.setVisible(true);
            }else{
                spentTodayText.setVisible(true);
                spentTodayWarnText.setVisible(false);
                monthLimitWarnText.setVisible(false);
            }

        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Input error");
            alert.setHeaderText(null);
            alert.setContentText("Invalid value entered. Please enter a number.");
            alert.getDialogPane().getStylesheets().add(Main.class.getResource("alert.css").toExternalForm());
            alert.showAndWait();
        }
        valueField.clear();
    }


    @FXML
    void changeLimitAction(ActionEvent event) throws IOException {
        String buffValue = newLimitField.getText();
        if (buffValue.matches("^[0-9\\.]*$") && buffValue.length() != 0) {
            maxMonthExpense = Float.parseFloat(buffValue);
            maxMonthExpenseSeries.getData().clear();
            maxMonthExpenseSeries.getData().add(new XYChart.Data("1", maxMonthExpense));
            maxMonthExpenseSeries.getData().add(new XYChart.Data("" + daysInMonth, maxMonthExpense));
            if(monthExpenses > maxMonthExpense){
                spentTodayText.setVisible(false);
                spentTodayWarnText.setVisible(true);
                monthLimitWarnText.setVisible(true);
            }else{
                spentTodayText.setVisible(true);
                spentTodayWarnText.setVisible(false);
                monthLimitWarnText.setVisible(false);
            }

            String update = "UPDATE users SET monthLimit = ? WHERE user_id=?";
            try {
                connection = handler.getConnection();
                pst = connection.prepareStatement(update);
                pst.setInt(2, userId);
                pst.setFloat(1, maxMonthExpense);
                pst.executeUpdate();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

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

    @FXML
    void deleteExpenseAction(ActionEvent event) {

    }

    @FXML
    void exit(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    void logOut(ActionEvent event) throws IOException {
        String path = new File(".").getCanonicalPath();
        FileWriter writer;
        writer = new FileWriter(path + File.separator + "RememberMe.txt", false);
        writer.close();

        settingsButton.getScene().getWindow().hide();
        Stage login = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("LoginWindow.fxml"));
        Scene scene = new Scene(root);
        login.setTitle("Authorization");
        login.setScene(scene);
        login.show();
        login.setResizable(false);
    }

    @FXML
    void openAppInfo(ActionEvent event) throws IOException {
        Stage appInfo = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("InfoWindow.fxml"));
        Scene scene = new Scene(root);
        appInfo.setTitle("Application Information");
        appInfo.setScene(scene);
        appInfo.show();
        appInfo.setResizable(false);
    }

}
