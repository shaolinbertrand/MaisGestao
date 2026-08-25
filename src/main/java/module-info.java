module com.gestao.agro {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens com.gestao.agro to javafx.fxml;
    opens com.gestao.agro.controller to javafx.fxml;
    opens com.gestao.agro.model to javafx.base;

    exports com.gestao.agro;
    exports com.gestao.agro.model;
    exports com.gestao.agro.controller;
    exports com.gestao.agro.util;
}