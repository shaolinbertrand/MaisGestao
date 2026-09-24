package com.gestao.agro.controller;

import com.gestao.agro.App;
import com.gestao.agro.util.SessaoUsuario;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    @FXML private TextField txtNomeConsultor;
    @FXML private TextField txtMatricula;
    @FXML private Label lblErro;

    @FXML
    private void entrar() {
        String nome = txtNomeConsultor.getText().trim();
        String matricula = txtMatricula.getText().trim();

        if (nome.isEmpty()) {
            lblErro.setText("Informe seu nome para continuar.");
            return;
        }

        SessaoUsuario.login(nome, matricula);

        try {
            App.setRoot("main");
        } catch (IOException e) {
            lblErro.setText("Erro ao abrir tela principal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}