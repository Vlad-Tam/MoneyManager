module com.example.moneymanager {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.moneymanager to javafx.fxml;
    exports com.example.moneymanager;
}