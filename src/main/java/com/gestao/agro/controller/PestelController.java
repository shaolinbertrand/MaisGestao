package com.gestao.agro.controller;

import com.gestao.agro.model.FatorPestel;
import com.gestao.agro.model.Organizacao;
import com.gestao.agro.model.Produtor;
import com.gestao.agro.repository.OrganizacaoRepository;
import com.gestao.agro.repository.PestelRepository;
import com.gestao.agro.repository.ProdutorRepository;
import com.gestao.agro.util.SessaoUsuario;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.util.List;

public class PestelController {

    // --- Seleção de Entidade ---
    @FXML private RadioButton rbTipoOrg;
    @FXML private RadioButton rbTipoProd;
    @FXML private ToggleGroup grpTipoAlvo;
    @FXML private Label lblEntidadeAlvo;
    @FXML private ComboBox<Object> cbEntidadeAlvo;
    @FXML private TextField txtConsultor;
    @FXML private Label lblResumoOportunidades;
    @FXML private Label lblResumoAmeacas;

    // --- Formulário PESTEL ---
    @FXML private ComboBox<String> cbDimensaoPestel;
    @FXML private RadioButton rbOportunidade;
    @FXML private RadioButton rbAmeaca;
    @FXML private ToggleGroup grpTipoFator;
    @FXML private Slider sldImpacto;
    @FXML private Label lblValorImpacto;
    @FXML private Slider sldProbabilidade;
    @FXML private Label lblValorProbabilidade;
    @FXML private TextField txtDescricao;
    @FXML private TextField txtAcaoSugerida;

    // --- Tabela Oportunidades ---
    @FXML private TableView<FatorPestel> tblOportunidades;
    @FXML private TableColumn<FatorPestel, String> colOpDimensao;
    @FXML private TableColumn<FatorPestel, String> colOpDescricao;
    @FXML private TableColumn<FatorPestel, Integer> colOpCriticidade;
    @FXML private TableColumn<FatorPestel, String> colOpAcao;

    // --- Tabela Ameaças ---
    @FXML private TableView<FatorPestel> tblAmeacas;
    @FXML private TableColumn<FatorPestel, String> colAmDimensao;
    @FXML private TableColumn<FatorPestel, String> colAmDescricao;
    @FXML private TableColumn<FatorPestel, Integer> colAmCriticidade;
    @FXML private TableColumn<FatorPestel, String> colAmAcao;

    @FXML private Label lblStatusMensagem;

    private final PestelRepository pestelRepo = new PestelRepository();
    private final OrganizacaoRepository orgRepo = new OrganizacaoRepository();
    private final ProdutorRepository prodRepo = new ProdutorRepository();

    private final ObservableList<FatorPestel> listaOportunidades = FXCollections.observableArrayList();
    private final ObservableList<FatorPestel> listaAmeacas = FXCollections.observableArrayList();

    private FatorPestel fatorEmEdicao = null;

    @FXML
    public void initialize() {
        txtConsultor.setText(SessaoUsuario.getNomeConsultor());

        configurarDimensoesPestel();
        configurarSliders();
        configurarSeletoresEntidade();
        configurarTabelas();

        alternarTipoAlvo();
    }

    private void configurarDimensoesPestel() {
        cbDimensaoPestel.setItems(FXCollections.observableArrayList(
            "Político",
            "Econômico",
            "Social",
            "Tecnológico",
            "Ecológico / Ambiental",
            "Legal / Regulatório"
        ));
        cbDimensaoPestel.getSelectionModel().selectFirst();
    }

    private void configurarSliders() {
        sldImpacto.valueProperty().addListener((obs, antigo, novo) -> 
            lblValorImpacto.setText(String.valueOf(novo.intValue()))
        );
        sldProbabilidade.valueProperty().addListener((obs, antigo, novo) -> 
            lblValorProbabilidade.setText(String.valueOf(novo.intValue()))
        );
    }

    private void configurarSeletoresEntidade() {
        grpTipoAlvo.selectedToggleProperty().addListener((obs, antigo, novo) -> alternarTipoAlvo());

        cbEntidadeAlvo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Object obj) {
                if (obj instanceof Organizacao org) {
                    return org.getId() + " - " + org.getNome();
                } else if (obj instanceof Produtor prod) {
                    return prod.getId() + " - " + prod.getNome() + " (" + prod.getComunidade() + ")";
                }
                return "";
            }

            @Override
            public Object fromString(String string) {
                return null;
            }
        });

        cbEntidadeAlvo.getSelectionModel().selectedItemProperty().addListener((obs, antigo, selecionado) -> 
            carregarFatoresEntidade(selecionado)
        );
    }

    private void configurarTabelas() {
        // Tabela Oportunidades
        colOpDimensao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDimensao()));
        colOpDescricao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescricao()));
        colOpCriticidade.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCriticidade()).asObject());
        colOpAcao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAcaoSugerida()));
        tblOportunidades.setItems(listaOportunidades);

        // Tabela Ameaças
        colAmDimensao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDimensao()));
        colAmDescricao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescricao()));
        colAmCriticidade.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCriticidade()).asObject());
        colAmAcao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAcaoSugerida()));
        tblAmeacas.setItems(listaAmeacas);
    }

    private void alternarTipoAlvo() {
        cbEntidadeAlvo.getItems().clear();
        limparFormulario();
        listaOportunidades.clear();
        listaAmeacas.clear();
        atualizarContadores();

        try {
            if (rbTipoOrg.isSelected()) {
                lblEntidadeAlvo.setText("Organização:*");
                List<Organizacao> orgs = orgRepo.listarTodas();
                cbEntidadeAlvo.getItems().addAll(orgs);
            } else {
                lblEntidadeAlvo.setText("Produtor Rural:*");
                List<Produtor> prods = prodRepo.listarTodos();
                cbEntidadeAlvo.getItems().addAll(prods);
            }

            if (!cbEntidadeAlvo.getItems().isEmpty()) {
                cbEntidadeAlvo.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            exibirMensagemErro("Erro ao listar entidades: " + e.getMessage());
        }
    }

    private void carregarFatoresEntidade(Object entidade) {
        listaOportunidades.clear();
        listaAmeacas.clear();
        limparFormulario();

        if (entidade == null) {
            atualizarContadores();
            return;
        }

        try {
            List<FatorPestel> fatores;
            if (entidade instanceof Organizacao org) {
                fatores = pestelRepo.listarPorOrganizacao(org.getId());
            } else {
                fatores = pestelRepo.listarPorProdutor(((Produtor) entidade).getId());
            }

            for (FatorPestel f : fatores) {
                if ("OPORTUNIDADE".equalsIgnoreCase(f.getTipo())) {
                    listaOportunidades.add(f);
                } else {
                    listaAmeacas.add(f);
                }
            }
            atualizarContadores();
            lblStatusMensagem.setStyle("-fx-text-fill: #2e7d32;");
            lblStatusMensagem.setText("Fatores PESTEL carregados com sucesso.");
        } catch (Exception e) {
            exibirMensagemErro("Erro ao carregar fatores PESTEL: " + e.getMessage());
        }
    }

    @FXML
    public void salvarFator() {
        Object entidade = cbEntidadeAlvo.getValue();
        if (entidade == null) {
            exibirMensagemErro("Selecione uma Cooperativa ou Produtor Rural antes de salvar!");
            return;
        }

        String descricao = txtDescricao.getText();
        if (descricao == null || descricao.trim().isEmpty()) {
            exibirMensagemErro("A descrição do fator externo é obrigatória.");
            return;
        }

        try {
            if (fatorEmEdicao == null) {
                fatorEmEdicao = new FatorPestel();
                if (entidade instanceof Organizacao org) {
                    fatorEmEdicao.setOrganizacaoId(org.getId());
                    fatorEmEdicao.setProdutorId(null);
                } else {
                    fatorEmEdicao.setProdutorId(((Produtor) entidade).getId());
                    fatorEmEdicao.setOrganizacaoId(null);
                }
            }

            fatorEmEdicao.setDimensao(cbDimensaoPestel.getValue());
            fatorEmEdicao.setTipo(rbOportunidade.isSelected() ? "OPORTUNIDADE" : "AMEACA");
            fatorEmEdicao.setDescricao(descricao.trim());
            fatorEmEdicao.setImpacto((int) sldImpacto.getValue());
            fatorEmEdicao.setProbabilidade((int) sldProbabilidade.getValue());
            fatorEmEdicao.setAcaoSugerida(txtAcaoSugerida.getText() != null ? txtAcaoSugerida.getText().trim() : "");

            if (fatorEmEdicao.getId() == null || fatorEmEdicao.getId() == 0) {
                pestelRepo.salvar(fatorEmEdicao);
                lblStatusMensagem.setStyle("-fx-text-fill: #2e7d32;");
                lblStatusMensagem.setText("Novo fator PESTEL registrado com sucesso.");
            } else {
                pestelRepo.atualizar(fatorEmEdicao);
                lblStatusMensagem.setStyle("-fx-text-fill: #2e7d32;");
                lblStatusMensagem.setText("Fator PESTEL atualizado com sucesso.");
            }

            limparFormulario();
            carregarFatoresEntidade(entidade);
        } catch (Exception e) {
            exibirMensagemErro("Erro ao salvar fator PESTEL: " + e.getMessage());
        }
    }

    @FXML
    public void editarOportunidadeSelecionada() {
        FatorPestel selecionado = tblOportunidades.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            carregarParaEdicao(selecionado);
        } else {
            exibirMensagemErro("Selecione uma oportunidade na tabela para editar.");
        }
    }

    @FXML
    public void editarAmeacaSelecionada() {
        FatorPestel selecionado = tblAmeacas.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            carregarParaEdicao(selecionado);
        } else {
            exibirMensagemErro("Selecione uma ameaça na tabela para editar.");
        }
    }

    private void carregarParaEdicao(FatorPestel f) {
        this.fatorEmEdicao = f;
        cbDimensaoPestel.setValue(f.getDimensao());
        if ("OPORTUNIDADE".equalsIgnoreCase(f.getTipo())) {
            rbOportunidade.setSelected(true);
        } else {
            rbAmeaca.setSelected(true);
        }
        sldImpacto.setValue(f.getImpacto());
        sldProbabilidade.setValue(f.getProbabilidade());
        txtDescricao.setText(f.getDescricao());
        txtAcaoSugerida.setText(f.getAcaoSugerida() != null ? f.getAcaoSugerida() : "");
        lblStatusMensagem.setStyle("-fx-text-fill: #1976D2;");
        lblStatusMensagem.setText("Modo de edição: Fator #" + f.getId());
    }

    @FXML
    public void excluirOportunidadeSelecionada() {
        FatorPestel selecionado = tblOportunidades.getSelectionModel().getSelectedItem();
        excluirFator(selecionado);
    }

    @FXML
    public void excluirAmeacaSelecionada() {
        FatorPestel selecionado = tblAmeacas.getSelectionModel().getSelectedItem();
        excluirFator(selecionado);
    }

    private void excluirFator(FatorPestel f) {
        if (f == null) {
            exibirMensagemErro("Selecione um item para excluir.");
            return;
        }

        try {
            pestelRepo.excluir(f.getId());
            lblStatusMensagem.setStyle("-fx-text-fill: #2e7d32;");
            lblStatusMensagem.setText("Fator PESTEL removido com sucesso.");
            carregarFatoresEntidade(cbEntidadeAlvo.getValue());
        } catch (Exception e) {
            exibirMensagemErro("Erro ao excluir fator: " + e.getMessage());
        }
    }

    @FXML
    public void limparFormulario() {
        fatorEmEdicao = null;
        if (!cbDimensaoPestel.getItems().isEmpty()) {
            cbDimensaoPestel.getSelectionModel().selectFirst();
        }
        rbOportunidade.setSelected(true);
        sldImpacto.setValue(3.0);
        sldProbabilidade.setValue(3.0);
        txtDescricao.clear();
        txtAcaoSugerida.clear();
    }

    private void atualizarContadores() {
        lblResumoOportunidades.setText(listaOportunidades.size() + " Oportunidades");
        lblResumoAmeacas.setText(listaAmeacas.size() + " Ameaças");
    }

    private void exibirMensagemErro(String msg) {
        lblStatusMensagem.setStyle("-fx-text-fill: #d32f2f;");
        lblStatusMensagem.setText(msg);
    }
}