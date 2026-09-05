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
        // Inicializa as tabelas do SQLite na abertura
        DatabaseConnection.initializeDatabase();

        scene = new Scene(loadFXML("main"), 1050, 700);
        stage.setTitle("Sistema de Gestão e Diagnóstico - Cooperativas");
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
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