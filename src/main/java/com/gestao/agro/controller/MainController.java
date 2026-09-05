package com.gestao.agro.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Label lblTituloModulo;

    @FXML
    private Label lblStatusSync;

    @FXML
    public void initialize() {
        lblStatusSync.setText("● Modo Offline (Pronto)");
    }

    @FXML
    private void abrirOrganizacao(ActionEvent event) {
        lblTituloModulo.setText("Módulo 1: Cadastro da Organização e Produtores Rurais");
    }

    @FXML
    private void abrirDiagnostico(ActionEvent event) {
        lblTituloModulo.setText("Módulo 2: Diagnóstico das 10 Dimensões");
    }

    @FXML
    private void abrirPestel(ActionEvent event) {
        lblTituloModulo.setText("Módulo 3: Análise PESTEL (Ambiente Externo)");
    }

    @FXML
    private void abrirFofa(ActionEvent event) {
        lblTituloModulo.setText("Módulo 4: Matriz FOFA / SWOT com Cruzamentos");
    }

    @FXML
    private void abrirGut(ActionEvent event) {
        lblTituloModulo.setText("Módulo 5: Matriz GUT e Priorização de Ações");
    }

    @FXML
    private void abrirPlanoAcao(ActionEvent event) {
        lblTituloModulo.setText("Módulo 6: Plano de Ação 5W2H");
    }

    @FXML
    private void abrirRelatorios(ActionEvent event) {
        lblTituloModulo.setText("Módulo 7: Geração de Relatórios e Planilhas");
    }
}