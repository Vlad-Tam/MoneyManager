module com.example.moneymanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.moneymanager to javafx.fxml;
    exports com.example.moneymanager;
}