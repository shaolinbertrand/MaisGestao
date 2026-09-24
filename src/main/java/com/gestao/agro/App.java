package com.gestao.agro;

import com.gestao.agro.util.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        DatabaseConnection.initializeDatabase();

        scene = new Scene(loadFXML("login"), 600, 500);
        stage.setTitle("Acesso - Gestão de Cooperativas");
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        Parent root = loadFXML(fxml);
        scene.setRoot(root);
        Stage stage = (Stage) scene.getWindow();
        if ("main".equals(fxml)) {
            stage.setTitle("Sistema de Gestão e Diagnóstico - Cooperativas");
            stage.setWidth(1080);
            stage.setHeight(720);
            stage.centerOnScreen();
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        URL resource = App.class.getResource("/fxml/" + fxml + ".fxml");
        if (resource == null) {
            resource = App.class.getClassLoader().getResource("fxml/" + fxml + ".fxml");
        }
        if (resource == null) {
            throw new IOException("Arquivo FXML não encontrado em /fxml/" + fxml + ".fxml");
        }
        return FXMLLoader.load(resource);
    }

    public static void main(String[] args) {
        launch();
    }
}