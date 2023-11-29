package com.example.moneymanager;

import com.example.moneymanager.DBConnection.DBHandler;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXRippler;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.time.Month;
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
    private float[] monthExpensesList;
    private float[] yearExpensesList;
    private float[] dayCategoriesList;
    private float[] monthCategoriesList;
    private float[] yearCategoriesList;
    private float spentToday = 0;

    XYChart.Series series;
    XYChart.Series maxMonthExpenseSeries;
    XYChart.Series barChartSeries;
    ObservableList<PieChart.Data> pieChartData;

    @FXML
    private LineChart<?, ?> lineChart;

    @FXML
    private BarChart<?, ?> barChart;

    @FXML
    private PieChart pieChart;

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

    @FXML
    private ChoiceBox<String> deleteField;

    @FXML
    private RadioButton byDaysRadioButton;

    @FXML
    private RadioButton byMonthsRadioButton;

    @FXML
    private RadioButton byYearsRadioButton;

    @FXML
    private ToggleGroup expensesByTime;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        vboxContainer.setVisible(false);
        lineChart.getStylesheets().add(getClass().getResource("chart.css").toExternalForm());
        categoryField.getStylesheets().add(getClass().getResource("inputFields.css").toExternalForm());
        dateField.getStylesheets().add(getClass().getResource("inputFields.css").toExternalForm());
        valueField.getStylesheets().add(getClass().getResource("inputFields.css").toExternalForm());
        deleteField.getStylesheets().add(getClass().getResource("inputFields.css").toExternalForm());
        newLimitField.getStylesheets().add(getClass().getResource("inputFields.css").toExternalForm());

        byDaysRadioButton.setStyle("-fx-selected-color: #f1f4d5;");
        byMonthsRadioButton.setStyle("-fx-selected-color: #f1f4d5;");
        byYearsRadioButton.setStyle("-fx-selected-color: #f1f4d5;");

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
            this.monthExpensesList = new float[12];
            this.yearExpensesList = new float [10];
            this.dayCategoriesList = new float[6];
            this.monthCategoriesList = new float[6];
            this.yearCategoriesList = new float[6];

            while(rs2.next()) {
                String buffCategoryNum = rs2.getString(3);
                System.out.println("buffCategoryNum = " + buffCategoryNum);
                java.sql.Date sqlDate = rs2.getDate(4);
                LocalDate buffExpenseDate = sqlDate.toLocalDate();
                String buffExpenseAmount = rs2.getString(5);
                yearExpensesList[buffExpenseDate.getYear() - 2020] += Float.parseFloat(buffExpenseAmount);
                yearCategoriesList[Integer.parseInt(buffCategoryNum) - 1] += Float.parseFloat(buffExpenseAmount);
                if(currentDate.getYear() == buffExpenseDate.getYear()){
                    monthExpensesList[buffExpenseDate.getMonthValue() - 1] += Float.parseFloat(buffExpenseAmount);
                    monthCategoriesList[Integer.parseInt(buffCategoryNum) - 1] += Float.parseFloat(buffExpenseAmount);
                    if(currentDate.getMonth() == buffExpenseDate.getMonth()){
                        monthExpenses += Float.parseFloat(buffExpenseAmount);
                        dateExpensesList[buffExpenseDate.getDayOfMonth() - 1] += Float.parseFloat(buffExpenseAmount);
                        dayCategoriesList[Integer.parseInt(buffCategoryNum) - 1] += Float.parseFloat(buffExpenseAmount);
                        if (currentDate.getDayOfMonth() == buffExpenseDate.getDayOfMonth()) {
                            spentToday += Float.parseFloat(buffExpenseAmount);
                        }
                    }
                }

                String categoriesRequest3 = "SELECT * from categories where category_id = ?";
                PreparedStatement pst3 = connection.prepareStatement(categoriesRequest3);
                pst3.setInt(1, Integer.parseInt(buffCategoryNum));
                ResultSet rs = pst3.executeQuery();
                String buffCategoryName = "Other";
                if(rs.next()) {
                    buffCategoryName = rs.getString(3);
                    System.out.println("buffCategoryName = " + buffCategoryName);
                }
                String expenseInfo = buffExpenseDate.toString() + " | " + buffCategoryName + " | " + buffExpenseAmount + "$";
                deleteField.getItems().add(expenseInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //linechart
        float buffSum = 0;
        for(int i = 0; i < dateExpensesList.length; i++){
            buffSum += dateExpensesList[i];
            series.getData().add(new XYChart.Data("" + (i + 1), buffSum));
        }

        //barchart
        barChartSeries = new XYChart.Series();
        barChartSeries.getData().clear();
        pieChartData = FXCollections.observableArrayList();
        pieChartData.clear();

        if(byDaysRadioButton.isSelected()){
            for(int i = 0; i < dateExpensesList.length; i++){
                barChartSeries.getData().add(new XYChart.Data("" + (i + 1), dateExpensesList[i]));
            }
            for(int i = 0; i < dayCategoriesList.length; i++){
                String buffCategoryString = switch (i){
                    case 0 -> "Products";
                    case 1 -> "Travel";
                    case 2 -> "Entertainment";
                    case 3 -> "Transport";
                    case 4 -> "Technologies";
                    default -> "Other";
                };
                pieChartData.add(new PieChart.Data(buffCategoryString, dayCategoriesList[i]));
            }
        }else if(byMonthsRadioButton.isSelected()){
            for(int i = 0; i < monthExpensesList.length; i++){
                barChartSeries.getData().add(new XYChart.Data(Month.of(i + 1).name().substring(0, 3) , monthExpensesList[i]));
            }
            for(int i = 0; i < monthCategoriesList.length; i++){
                String buffCategoryString = switch (i){
                    case 0 -> "Products";
                    case 1 -> "Travel";
                    case 2 -> "Entertainment";
                    case 3 -> "Transport";
                    case 4 -> "Technologies";
                    default -> "Other";
                };
                pieChartData.add(new PieChart.Data(buffCategoryString, monthCategoriesList[i]));
            }
        }else if(byYearsRadioButton.isSelected()){
            for(int i = 0; i<yearExpensesList.length; i++){
                barChartSeries.getData().add(new XYChart.Data("" + (2020 + i), yearExpensesList[i]));
            }
            for(int i = 0; i < yearCategoriesList.length; i++){
                String buffCategoryString = switch (i){
                    case 0 -> "Products";
                    case 1 -> "Travel";
                    case 2 -> "Entertainment";
                    case 3 -> "Transport";
                    case 4 -> "Technologies";
                    default -> "Other";
                };
                pieChartData.add(new PieChart.Data(buffCategoryString, yearCategoriesList[i]));
            }
        }
        barChart.getData().add(barChartSeries);
        pieChart.setData(pieChartData);

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
        DeleteFieldEvent();
        changeRadioButtonEvent();
    }

    private void categoryFieldEvent(){
        categoryField.setOnAction((event) -> {
            int selectedIndex = categoryField.getSelectionModel().getSelectedIndex();
            String selectedItem = categoryField.getSelectionModel().getSelectedItem();

            System.out.println("Selection made: [" + selectedIndex + "] " + selectedItem);
            System.out.println("ChoiceBox.getValue(): " + categoryField.getValue());
        });
    }

    private void DeleteFieldEvent(){
        deleteField.setOnAction((event) -> {
            int selectedIndex = deleteField.getSelectionModel().getSelectedIndex();
            String selectedItem = deleteField.getSelectionModel().getSelectedItem();

            System.out.println("Selection made: [" + selectedIndex + "] " + selectedItem);
            System.out.println("ChoiceBox.getValue(): " + deleteField.getValue());
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

        if(selectedDate.getYear() > 2029 || selectedDate.getYear() < 2020){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Input error");
            alert.setHeaderText(null);
            alert.setContentText("Invalid value entered. Please enter year from 2020 to 2029.");
            alert.getDialogPane().getStylesheets().add(Main.class.getResource("alert.css").toExternalForm());
            alert.showAndWait();
        }else {
            if (buffValue.matches("^[0-9\\.]*$") && buffValue.length() != 0) {
                System.out.println("buffValue = " + buffValue);

                String insert = "INSERT INTO expenses(user_id,category_id,expense_date,expense_amount)" + "VALUES (?,?,?,?)";
                try {
                    connection = handler.getConnection();
                    pst = connection.prepareStatement(insert);
                    pst.setString(1, "" + userId);
                    int catIdBuff = switch (buffCategory) {
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

                    for (int i = 0; i < dateExpensesList.length; i++) {
                        dateExpensesList[i] = 0;
                    }
                    for (int i = 0; i < monthExpensesList.length; i++) {
                        monthExpensesList[i] = 0;
                    }
                    for (int i = 0; i < yearExpensesList.length; i++) {
                        yearExpensesList[i] = 0;
                    }
                    for (int i = 0; i < dayCategoriesList.length; i++) {
                        dayCategoriesList[i] = 0;
                    }
                    for (int i = 0; i < monthCategoriesList.length; i++) {
                        monthCategoriesList[i] = 0;
                    }
                    for (int i = 0; i < yearCategoriesList.length; i++) {
                        yearCategoriesList[i] = 0;
                    }
                    spentToday = 0;
                    monthExpenses = 0;
                    deleteField.getItems().clear();

                    while (rs.next()) {
                        String buffCategoryNum = rs.getString(3);
                        System.out.println("buffCategoryNum = " + buffCategoryNum);
                        java.sql.Date sqlDate = rs.getDate(4);
                        LocalDate buffExpenseDate = sqlDate.toLocalDate();
                        String buffExpenseAmount = rs.getString(5);

                        yearExpensesList[buffExpenseDate.getYear() - 2020] += Float.parseFloat(buffExpenseAmount);
                        yearCategoriesList[Integer.parseInt(buffCategoryNum) - 1] += Float.parseFloat(buffExpenseAmount);
                        if (currentDate.getYear() == buffExpenseDate.getYear()) {
                            monthExpensesList[buffExpenseDate.getMonthValue() - 1] += Float.parseFloat(buffExpenseAmount);
                            monthCategoriesList[Integer.parseInt(buffCategoryNum) - 1] += Float.parseFloat(buffExpenseAmount);
                            if (currentDate.getMonth() == buffExpenseDate.getMonth()) {
                                monthExpenses += Float.parseFloat(buffExpenseAmount);
                                dateExpensesList[buffExpenseDate.getDayOfMonth() - 1] += Float.parseFloat(buffExpenseAmount);
                                dayCategoriesList[Integer.parseInt(buffCategoryNum) - 1] += Float.parseFloat(buffExpenseAmount);
                                if (currentDate.getDayOfMonth() == buffExpenseDate.getDayOfMonth()) {
                                    spentToday += Float.parseFloat(buffExpenseAmount);
                                }
                            }
                        }
//                    yearExpensesList[buffExpenseDate.getYear() - 2020] += Float.parseFloat(buffExpenseAmount);
//                    if(currentDate.getYear() == buffExpenseDate.getYear()){
//                        monthExpensesList[buffExpenseDate.getMonthValue() - 1] += Float.parseFloat(buffExpenseAmount);
//                        if(currentDate.getMonth() == buffExpenseDate.getMonth()){
//                            monthExpenses += Float.parseFloat(buffExpenseAmount);
//                            dateExpensesList[buffExpenseDate.getDayOfMonth() - 1] += Float.parseFloat(buffExpenseAmount);
//                            if (currentDate.getDayOfMonth() == buffExpenseDate.getDayOfMonth()) {
//                                spentToday += Float.parseFloat(buffExpenseAmount);
//                            }
//                        }
//                    }
                        //////////////////////////////////////////////////////////////////////////////
                        String categoriesRequest3 = "SELECT * from categories where category_id = ?";
                        PreparedStatement pst3 = connection.prepareStatement(categoriesRequest3);
                        pst3.setInt(1, Integer.parseInt(buffCategoryNum));
                        ResultSet rs2 = pst3.executeQuery();
                        String buffCategoryName = "Other";
                        if (rs2.next()) {
                            buffCategoryName = rs2.getString(3);
                            System.out.println("buffCategoryName = " + buffCategoryName);
                        }
                        String expenseInfo = buffExpenseDate.toString() + " | " + buffCategoryName + " | " + buffExpenseAmount + "$";
                        deleteField.getItems().add(expenseInfo);
                    }
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                float buffSum = 0;
                for (int i = 0; i < dateExpensesList.length; i++) {
                    buffSum += dateExpensesList[i];
                    series.getData().add(new XYChart.Data("" + (i + 1), buffSum));
                }

                //barchart piechart
                barChartSeries.getData().clear();
                pieChartData.clear();
                if (byDaysRadioButton.isSelected()) {
                    for (int i = 0; i < dateExpensesList.length; i++) {
                        barChartSeries.getData().add(new XYChart.Data("" + (i + 1), dateExpensesList[i]));
                    }
                    for (int i = 0; i < dayCategoriesList.length; i++) {
                        String buffCategoryString = switch (i) {
                            case 0 -> "Products";
                            case 1 -> "Travel";
                            case 2 -> "Entertainment";
                            case 3 -> "Transport";
                            case 4 -> "Technologies";
                            default -> "Other";
                        };
                        pieChartData.add(new PieChart.Data(buffCategoryString, dayCategoriesList[i]));
                    }
                } else if (byMonthsRadioButton.isSelected()) {
                    for (int i = 0; i < monthExpensesList.length; i++) {
                        barChartSeries.getData().add(new XYChart.Data(Month.of(i + 1).name().substring(0, 3), monthExpensesList[i]));
                    }
                    for (int i = 0; i < monthCategoriesList.length; i++) {
                        String buffCategoryString = switch (i) {
                            case 0 -> "Products";
                            case 1 -> "Travel";
                            case 2 -> "Entertainment";
                            case 3 -> "Transport";
                            case 4 -> "Technologies";
                            default -> "Other";
                        };
                        pieChartData.add(new PieChart.Data(buffCategoryString, monthCategoriesList[i]));
                    }
                } else if (byYearsRadioButton.isSelected()) {
                    for (int i = 0; i < yearExpensesList.length; i++) {
                        barChartSeries.getData().add(new XYChart.Data("" + (2020 + i), yearExpensesList[i]));
                    }
                    for (int i = 0; i < yearCategoriesList.length; i++) {
                        String buffCategoryString = switch (i) {
                            case 0 -> "Products";
                            case 1 -> "Travel";
                            case 2 -> "Entertainment";
                            case 3 -> "Transport";
                            case 4 -> "Technologies";
                            default -> "Other";
                        };
                        pieChartData.add(new PieChart.Data(buffCategoryString, yearCategoriesList[i]));
                    }
                }

                spentTodayText.setText("Spent today " + spentToday + "$");
                spentTodayWarnText.setText("Spent today " + spentToday + "$");
                if (monthExpenses > maxMonthExpense) {
                    spentTodayText.setVisible(false);
                    spentTodayWarnText.setVisible(true);
                    monthLimitWarnText.setVisible(true);
                } else {
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
        String buffCategory = deleteField.getValue();
        if (buffCategory != null) {

            String[] parts = buffCategory.split("\\|");
            String dateStr = parts[0].trim();
            LocalDate localDateStr = LocalDate.parse(dateStr);
            String categoriesStr = parts[1].trim();
            String valueStr = parts[2].trim().replace("$", "");
            float value = Float.parseFloat(valueStr);
            int catIdBuff = switch (categoriesStr) {
                case "Products" -> 1;
                case "Travel" -> 2;
                case "Entertainment" -> 3;
                case "Transport" -> 4;
                case "Technologies" -> 5;
                default -> 6;
            };

            String deleteExpenseRequest = "SELECT * FROM expenses WHERE expense_date = ? AND expense_amount = ? AND category_id = ?";
            try {
                connection = handler.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(deleteExpenseRequest);
                pstmt.setString(1, localDateStr.toString());
                pstmt.setString(2, valueStr);
                pstmt.setInt(3, catIdBuff);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    int idToDelete = rs.getInt(1);
                    String deleteRequest = "DELETE FROM expenses WHERE expense_id = ?";
                    PreparedStatement deleteStmt = connection.prepareStatement(deleteRequest);
                    deleteStmt.setInt(1, idToDelete);
                    deleteStmt.executeUpdate();
                }


                String expensesRequest = "SELECT * from expenses where user_id=?";
                PreparedStatement pst;
                pst = connection.prepareStatement(expensesRequest);
                pst.setInt(1, userId);
                ResultSet rs2 = pst.executeQuery();

                for (int i = 0; i < dateExpensesList.length; i++) {
                    dateExpensesList[i] = 0;
                }
                for (int i = 0; i < monthExpensesList.length; i++) {
                    monthExpensesList[i] = 0;
                }
                for (int i = 0; i < yearExpensesList.length; i++) {
                    yearExpensesList[i] = 0;
                }
                for (int i = 0; i < dayCategoriesList.length; i++) {
                    dayCategoriesList[i] = 0;
                }
                for (int i = 0; i < monthCategoriesList.length; i++) {
                    monthCategoriesList[i] = 0;
                }
                for (int i = 0; i < yearCategoriesList.length; i++) {
                    yearCategoriesList[i] = 0;
                }

                spentToday = 0;
                monthExpenses = 0;
                deleteField.getItems().clear();
                while (rs2.next()) {
                    String buffCategoryNum = rs2.getString(3);
                    System.out.println("buffCategoryNum = " + buffCategoryNum);
                    java.sql.Date sqlDate = rs2.getDate(4);
                    LocalDate buffExpenseDate = sqlDate.toLocalDate();
                    String buffExpenseAmount = rs2.getString(5);

                    yearExpensesList[buffExpenseDate.getYear() - 2020] += Float.parseFloat(buffExpenseAmount);
                    yearCategoriesList[Integer.parseInt(buffCategoryNum) - 1] += Float.parseFloat(buffExpenseAmount);
                    if(currentDate.getYear() == buffExpenseDate.getYear()){
                        monthExpensesList[buffExpenseDate.getMonthValue() - 1] += Float.parseFloat(buffExpenseAmount);
                        monthCategoriesList[Integer.parseInt(buffCategoryNum) - 1] += Float.parseFloat(buffExpenseAmount);
                        if(currentDate.getMonth() == buffExpenseDate.getMonth()){
                            monthExpenses += Float.parseFloat(buffExpenseAmount);
                            dateExpensesList[buffExpenseDate.getDayOfMonth() - 1] += Float.parseFloat(buffExpenseAmount);
                            dayCategoriesList[Integer.parseInt(buffCategoryNum) - 1] += Float.parseFloat(buffExpenseAmount);
                            if (currentDate.getDayOfMonth() == buffExpenseDate.getDayOfMonth()) {
                                spentToday += Float.parseFloat(buffExpenseAmount);
                            }
                        }
                    }
//                    yearExpensesList[buffExpenseDate.getYear() - 2020] += Float.parseFloat(buffExpenseAmount);
//                    if(currentDate.getYear() == buffExpenseDate.getYear()){
//                        monthExpensesList[buffExpenseDate.getMonthValue() - 1] += Float.parseFloat(buffExpenseAmount);
//                        if(currentDate.getMonth() == buffExpenseDate.getMonth()){
//                            monthExpenses += Float.parseFloat(buffExpenseAmount);
//                            dateExpensesList[buffExpenseDate.getDayOfMonth() - 1] += Float.parseFloat(buffExpenseAmount);
//                            if (currentDate.getDayOfMonth() == buffExpenseDate.getDayOfMonth()) {
//                                spentToday += Float.parseFloat(buffExpenseAmount);
//                            }
//                        }
//                    }
                    String categoriesRequest3 = "SELECT * from categories where category_id = ?";
                    PreparedStatement pst3 = connection.prepareStatement(categoriesRequest3);
                    pst3.setInt(1, Integer.parseInt(buffCategoryNum));
                    ResultSet rs3 = pst3.executeQuery();
                    String buffCategoryName = "Other";
                    if (rs3.next()) {
                        buffCategoryName = rs3.getString(3);
                        System.out.println("buffCategoryName = " + buffCategoryName);
                    }
                    String expenseInfo = buffExpenseDate.toString() + " | " + buffCategoryName + " | " + buffExpenseAmount + "$";
                    deleteField.getItems().add(expenseInfo);
                }
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //linechart
            series.getData().clear();
            float buffSum = 0;
            for (int i = 0; i < dateExpensesList.length; i++) {
                buffSum += dateExpensesList[i];
                series.getData().add(new XYChart.Data("" + (i + 1), buffSum));
            }

            //barchart piechart
            barChartSeries.getData().clear();
            pieChartData.clear();
            if(byDaysRadioButton.isSelected()){
                for(int i = 0; i < dateExpensesList.length; i++){
                    barChartSeries.getData().add(new XYChart.Data("" + (i + 1), dateExpensesList[i]));
                }
                for(int i = 0; i < dayCategoriesList.length; i++){
                    String buffCategoryString = switch (i){
                        case 0 -> "Products";
                        case 1 -> "Travel";
                        case 2 -> "Entertainment";
                        case 3 -> "Transport";
                        case 4 -> "Technologies";
                        default -> "Other";
                    };
                    pieChartData.add(new PieChart.Data(buffCategoryString, dayCategoriesList[i]));
                }
            }else if(byMonthsRadioButton.isSelected()){
                for(int i = 0; i < monthExpensesList.length; i++){
                    barChartSeries.getData().add(new XYChart.Data(Month.of(i + 1).name().substring(0, 3) , monthExpensesList[i]));
                }
                for(int i = 0; i < monthCategoriesList.length; i++){
                    String buffCategoryString = switch (i){
                        case 0 -> "Products";
                        case 1 -> "Travel";
                        case 2 -> "Entertainment";
                        case 3 -> "Transport";
                        case 4 -> "Technologies";
                        default -> "Other";
                    };
                    pieChartData.add(new PieChart.Data(buffCategoryString, monthCategoriesList[i]));
                }
            }else if(byYearsRadioButton.isSelected()){
                for(int i = 0; i<yearExpensesList.length; i++){
                    barChartSeries.getData().add(new XYChart.Data("" + (2020 + i), yearExpensesList[i]));
                }
                for(int i = 0; i < monthCategoriesList.length; i++){
                    String buffCategoryString = switch (i){
                        case 0 -> "Products";
                        case 1 -> "Travel";
                        case 2 -> "Entertainment";
                        case 3 -> "Transport";
                        case 4 -> "Technologies";
                        default -> "Other";
                    };
                    pieChartData.add(new PieChart.Data(buffCategoryString, monthCategoriesList[i]));
                }
            }
            //barChart.getData().add(barChartSeries);

            spentTodayText.setText("Spent today " + spentToday + "$");
            spentTodayWarnText.setText("Spent today " + spentToday + "$");
            if (monthExpenses > maxMonthExpense) {
                spentTodayText.setVisible(false);
                spentTodayWarnText.setVisible(true);
                monthLimitWarnText.setVisible(true);
            } else {
                spentTodayText.setVisible(true);
                spentTodayWarnText.setVisible(false);
                monthLimitWarnText.setVisible(false);
            }
        }else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Input error");
            alert.setHeaderText(null);
            alert.setContentText("Invalid value selected. Select one of the suggested values.");
            alert.getDialogPane().getStylesheets().add(Main.class.getResource("alert.css").toExternalForm());
            alert.showAndWait();
        }
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

    private void changeRadioButtonEvent() {
        expensesByTime.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                if (expensesByTime.getSelectedToggle() != null) {
                    System.out.println("State of other radio buttons:");
                    System.out.println("byDaysRadioButton: " + byDaysRadioButton.isSelected());
                    System.out.println("byMonthsRadioButton: " + byMonthsRadioButton.isSelected());
                    System.out.println("byYearsRadioButton: " + byYearsRadioButton.isSelected());

                    //barchart piechart
                    barChartSeries.getData().clear();
                    pieChartData.clear();
                    if(byDaysRadioButton.isSelected()){
                        for(int i = 0; i < dateExpensesList.length; i++){
                            barChartSeries.getData().add(new XYChart.Data("" + (i + 1), dateExpensesList[i]));
                        }
                        for(int i = 0; i < dayCategoriesList.length; i++){
                            String buffCategoryString = switch (i){
                                case 0 -> "Products";
                                case 1 -> "Travel";
                                case 2 -> "Entertainment";
                                case 3 -> "Transport";
                                case 4 -> "Technologies";
                                default -> "Other";
                            };
                            pieChartData.add(new PieChart.Data(buffCategoryString, dayCategoriesList[i]));
                        }
                    }else if(byMonthsRadioButton.isSelected()){
                        for(int i = 0; i < monthExpensesList.length; i++){
                            barChartSeries.getData().add(new XYChart.Data(Month.of(i + 1).name().substring(0, 3) , monthExpensesList[i]));
                        }
                        for(int i = 0; i < monthCategoriesList.length; i++){
                            String buffCategoryString = switch (i){
                                case 0 -> "Products";
                                case 1 -> "Travel";
                                case 2 -> "Entertainment";
                                case 3 -> "Transport";
                                case 4 -> "Technologies";
                                default -> "Other";
                            };
                            pieChartData.add(new PieChart.Data(buffCategoryString, monthCategoriesList[i]));
                        }
                    }else if(byYearsRadioButton.isSelected()){
                        for(int i = 0; i<yearExpensesList.length; i++){
                            barChartSeries.getData().add(new XYChart.Data("" + (2020 + i), yearExpensesList[i]));
                        }
                        for(int i = 0; i < yearCategoriesList.length; i++){
                            String buffCategoryString = switch (i){
                                case 0 -> "Products";
                                case 1 -> "Travel";
                                case 2 -> "Entertainment";
                                case 3 -> "Transport";
                                case 4 -> "Technologies";
                                default -> "Other";
                            };
                            pieChartData.add(new PieChart.Data(buffCategoryString, yearCategoriesList[i]));
                        }
                    }
                }
            }
        });
    }
}
