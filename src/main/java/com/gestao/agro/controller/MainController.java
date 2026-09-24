package com.gestao.agro.controller;

import com.gestao.agro.util.SessaoUsuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
    private Label lblConsultorNome;

    @FXML
    public void initialize() {
        if (lblStatusSync != null) {
            lblStatusSync.setText("● Modo Offline (Pronto)");
        }
        if (lblConsultorNome != null) {
            lblConsultorNome.setText(SessaoUsuario.getNomeConsultor());
        }
    }

    @FXML
    private void abrirOrganizacao(ActionEvent event) {
        carregarView("/fxml/organizacao_form.fxml", "Módulo 1: Cadastro de Cooperativas e Produtores");
    }

    @FXML
    private void abrirConsultaGeral(ActionEvent event) {
        carregarView("/fxml/consulta_geral.fxml", "Consulta Geral - Cooperativas e Produtores");
    }

    @FXML
    private void abrirDiagnostico(ActionEvent event) {
        carregarView("/fxml/diagnostico_form.fxml", "Módulo 2: Diagnóstico das 10 Dimensões");
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

    private void carregarView(String caminhoFxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(caminhoFxml));
            Node viewNode = loader.load();
            contentArea.getChildren().setAll(viewNode);
            if (lblTituloModulo != null) {
                lblTituloModulo.setText(titulo);
            }
        } catch (Exception e) {
            if (lblTituloModulo != null) {
                lblTituloModulo.setText("Erro ao carregar tela: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }
}