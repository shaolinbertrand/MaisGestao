module com.gestao.agro {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.apache.poi.ooxml;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires org.json;
    requires io.github.cdimascio.dotenv.java;
    requires org.apache.poi.poi;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires javafx.swing;
    requires java.desktop;

    opens com.gestao.agro to javafx.fxml;
    opens com.gestao.agro.controller to javafx.fxml;
    opens com.gestao.agro.model to javafx.base;

    exports com.gestao.agro;
    exports com.gestao.agro.model;
    exports com.gestao.agro.repository;
    exports com.gestao.agro.controller;
    exports com.gestao.agro.util;
}