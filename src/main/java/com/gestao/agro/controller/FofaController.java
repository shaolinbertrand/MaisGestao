package com.gestao.agro.controller;

import com.gestao.agro.model.CruzamentoSwot;
import com.gestao.agro.model.FatorSwot;
import com.gestao.agro.model.Organizacao;
import com.gestao.agro.model.Produtor;
import com.gestao.agro.repository.OrganizacaoRepository;
import com.gestao.agro.repository.ProdutorRepository;
import com.gestao.agro.repository.SwotRepository;
import com.gestao.agro.util.SessaoUsuario;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.util.List;

public class FofaController {

    // --- Seleção de Entidade ---
    @FXML private RadioButton rbTipoOrg;
    @FXML private RadioButton rbTipoProd;
    @FXML private ToggleGroup grpTipoAlvo;
    @FXML private Label lblEntidadeAlvo;
    @FXML private ComboBox<Object> cbEntidadeAlvo;
    @FXML private TextField txtConsultor;

    // --- Formulário Fator SWOT (Aba 1) ---
    @FXML private ComboBox<String> cbTipoFator;
    @FXML private Slider sldIntensidade;
    @FXML private Label lblValorIntensidade;
    @FXML private TextField txtDescricaoFator;

    // --- Contadores da Matriz 2x2 ---
    @FXML private Label lblCountForcas;
    @FXML private Label lblCountFraquezas;
    @FXML private Label lblCountOportunidades;
    @FXML private Label lblCountAmeacas;

    // --- Tabelas dos Quadrantes ---
    @FXML private TableView<FatorSwot> tblForcas;
    @FXML private TableColumn<FatorSwot, String> colForcaDesc;
    @FXML private TableColumn<FatorSwot, Integer> colForcaInt;

    @FXML private TableView<FatorSwot> tblFraquezas;
    @FXML private TableColumn<FatorSwot, String> colFraquezaDesc;
    @FXML private TableColumn<FatorSwot, Integer> colFraquezaInt;

    @FXML private TableView<FatorSwot> tblOportunidades;
    @FXML private TableColumn<FatorSwot, String> colOportunidadeDesc;
    @FXML private TableColumn<FatorSwot, Integer> colOportunidadeInt;

    @FXML private TableView<FatorSwot> tblAmeacas;
    @FXML private TableColumn<FatorSwot, String> colAmeacaDesc;
    @FXML private TableColumn<FatorSwot, Integer> colAmeacaInt;

    // --- Formulário Cruzamentos TOWS (Aba 2) ---
    @FXML private ComboBox<String> cbTipoCruzamento;
    @FXML private ComboBox<FatorSwot> cbFatorInterno;
    @FXML private ComboBox<FatorSwot> cbFatorExterno;
    @FXML private TextField txtEstrategiaProposta;

    // --- Tabela Cruzamentos TOWS ---
    @FXML private TableView<CruzamentoSwot> tblCruzamentos;
    @FXML private TableColumn<CruzamentoSwot, String> colCruzTipo;
    @FXML private TableColumn<CruzamentoSwot, String> colCruzInterno;
    @FXML private TableColumn<CruzamentoSwot, String> colCruzExterno;
    @FXML private TableColumn<CruzamentoSwot, String> colCruzEstrategia;

    @FXML private Label lblStatusMensagem;

    private final SwotRepository swotRepo = new SwotRepository();
    private final OrganizacaoRepository orgRepo = new OrganizacaoRepository();
    private final ProdutorRepository prodRepo = new ProdutorRepository();

    // Coleções observáveis vinculadas permanentemente às tabelas
    private final ObservableList<FatorSwot> listaForcas = FXCollections.observableArrayList();
    private final ObservableList<FatorSwot> listaFraquezas = FXCollections.observableArrayList();
    private final ObservableList<FatorSwot> listaOportunidades = FXCollections.observableArrayList();
    private final ObservableList<FatorSwot> listaAmeacas = FXCollections.observableArrayList();
    private final ObservableList<CruzamentoSwot> listaCruzamentos = FXCollections.observableArrayList();

    private FatorSwot fatorEmEdicao = null;
    private boolean ignorarListener = false;

    @FXML
    public void initialize() {
        txtConsultor.setText(SessaoUsuario.getNomeConsultor());

        configurarFormularioFator();
        configurarSeletoresEntidade();
        configurarTabelasQuadrantes();
        configurarAbaCruzamentos();

        alternarTipoAlvo();
    }

    private void configurarFormularioFator() {
        cbTipoFator.setItems(FXCollections.observableArrayList(
            "FORÇA", "FRAQUEZA", "OPORTUNIDADE", "AMEAÇA"
        ));
        cbTipoFator.getSelectionModel().selectFirst();

        sldIntensidade.valueProperty().addListener((obs, antigo, novo) -> 
            lblValorIntensidade.setText(String.valueOf(novo.intValue()))
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

        cbEntidadeAlvo.getSelectionModel().selectedItemProperty().addListener((obs, antigo, selecionado) -> {
            if (!ignorarListener && selecionado != null) {
                carregarDadosEntidade(selecionado);
            }
        });
    }

    private void configurarTabelasQuadrantes() {
        // Usa PropertyValueFactory nativo do JavaFX para evitar falha de renderização
        colForcaDesc.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colForcaInt.setCellValueFactory(new PropertyValueFactory<>("intensidade"));
        tblForcas.setItems(listaForcas);

        colFraquezaDesc.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colFraquezaInt.setCellValueFactory(new PropertyValueFactory<>("intensidade"));
        tblFraquezas.setItems(listaFraquezas);

        colOportunidadeDesc.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colOportunidadeInt.setCellValueFactory(new PropertyValueFactory<>("intensidade"));
        tblOportunidades.setItems(listaOportunidades);

        colAmeacaDesc.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colAmeacaInt.setCellValueFactory(new PropertyValueFactory<>("intensidade"));
        tblAmeacas.setItems(listaAmeacas);
    }

    private void configurarAbaCruzamentos() {
        cbTipoCruzamento.setItems(FXCollections.observableArrayList(
            "FO - Alavancagem (Força x Oportunidade)",
            "FA - Proteção (Força x Ameaça)",
            "WO - Recuperação (Fraqueza x Oportunidade)",
            "WA - Sobrevivência (Fraqueza x Ameaça)"
        ));
        cbTipoCruzamento.getSelectionModel().selectFirst();

        cbTipoCruzamento.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> 
            atualizarSeletoresCruzamento(novo)
        );

        StringConverter<FatorSwot> conversorFator = new StringConverter<>() {
            @Override
            public String toString(FatorSwot f) {
                return (f != null) ? f.getDescricao() : "";
            }

            @Override
            public FatorSwot fromString(String string) {
                return null;
            }
        };

        cbFatorInterno.setConverter(conversorFator);
        cbFatorExterno.setConverter(conversorFator);

        colCruzTipo.setCellValueFactory(new PropertyValueFactory<>("tipoEstrategia"));
        colCruzInterno.setCellValueFactory(new PropertyValueFactory<>("descricaoFatorInterno"));
        colCruzExterno.setCellValueFactory(new PropertyValueFactory<>("descricaoFatorExterno"));
        colCruzEstrategia.setCellValueFactory(new PropertyValueFactory<>("estrategiaProposta"));
        tblCruzamentos.setItems(listaCruzamentos);
    }

    private void alternarTipoAlvo() {
        ignorarListener = true;
        try {
            cbEntidadeAlvo.getItems().clear();
            limparTudo();

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
        } finally {
            ignorarListener = false;
        }

        if (cbEntidadeAlvo.getValue() != null) {
            carregarDadosEntidade(cbEntidadeAlvo.getValue());
        }
    }

    private void carregarDadosEntidade(Object entidade) {
        if (entidade == null) {
            limparTudo();
            return;
        }

        try {
            List<FatorSwot> fatores;
            List<CruzamentoSwot> cruzamentos;

            if (entidade instanceof Organizacao org) {
                fatores = swotRepo.listarFatoresPorOrganizacao(org.getId());
                cruzamentos = swotRepo.listarCruzamentosPorOrganizacao(org.getId());
            } else {
                Produtor prod = (Produtor) entidade;
                fatores = swotRepo.listarFatoresPorProdutor(prod.getId());
                cruzamentos = swotRepo.listarCruzamentosPorProdutor(prod.getId());
            }

            listaForcas.clear();
            listaFraquezas.clear();
            listaOportunidades.clear();
            listaAmeacas.clear();
            listaCruzamentos.clear();

            for (FatorSwot f : fatores) {
                if (f.getTipo() == null) continue;
                String t = f.getTipo().trim().toUpperCase();

                if (t.contains("FORC") || t.contains("FORÇ")) {
                    listaForcas.add(f);
                } else if (t.contains("FRAQ")) {
                    listaFraquezas.add(f);
                } else if (t.contains("OPORT")) {
                    listaOportunidades.add(f);
                } else if (t.contains("AMEA")) {
                    listaAmeacas.add(f);
                }
            }

            listaCruzamentos.addAll(cruzamentos);
            atualizarContadores();
            atualizarSeletoresCruzamento(cbTipoCruzamento.getValue());

            lblStatusMensagem.setStyle("-fx-text-fill: #2e7d32;");
            lblStatusMensagem.setText("Dados da Matriz FOFA carregados com sucesso.");
        } catch (Exception e) {
            exibirMensagemErro("Erro ao carregar dados SWOT: " + e.getMessage());
        }
    }

    private void atualizarSeletoresCruzamento(String tipoSelecionado) {
        cbFatorInterno.getItems().clear();
        cbFatorExterno.getItems().clear();

        if (tipoSelecionado == null) return;

        if (tipoSelecionado.startsWith("FO")) {
            cbFatorInterno.setItems(FXCollections.observableArrayList(listaForcas));
            cbFatorExterno.setItems(FXCollections.observableArrayList(listaOportunidades));
        } else if (tipoSelecionado.startsWith("FA")) {
            cbFatorInterno.setItems(FXCollections.observableArrayList(listaForcas));
            cbFatorExterno.setItems(FXCollections.observableArrayList(listaAmeacas));
        } else if (tipoSelecionado.startsWith("WO")) {
            cbFatorInterno.setItems(FXCollections.observableArrayList(listaFraquezas));
            cbFatorExterno.setItems(FXCollections.observableArrayList(listaOportunidades));
        } else if (tipoSelecionado.startsWith("WA")) {
            cbFatorInterno.setItems(FXCollections.observableArrayList(listaFraquezas));
            cbFatorExterno.setItems(FXCollections.observableArrayList(listaAmeacas));
        }

        if (!cbFatorInterno.getItems().isEmpty()) cbFatorInterno.getSelectionModel().selectFirst();
        if (!cbFatorExterno.getItems().isEmpty()) cbFatorExterno.getSelectionModel().selectFirst();
    }

    // ==========================================
    //            OPERAÇÕES: FATORES
    // ==========================================

    @FXML
    public void salvarFator() {
        Object entidade = cbEntidadeAlvo.getValue();
        if (entidade == null) {
            exibirMensagemErro("Selecione uma Cooperativa ou Produtor Rural antes de salvar!");
            return;
        }

        String descricao = txtDescricaoFator.getText();
        if (descricao == null || descricao.trim().isEmpty()) {
            exibirMensagemErro("A descrição do fator SWOT é obrigatória.");
            return;
        }

        try {
            boolean isNovo = (fatorEmEdicao == null || fatorEmEdicao.getId() == null);
            if (isNovo) {
                fatorEmEdicao = new FatorSwot();
                if (entidade instanceof Organizacao org) {
                    fatorEmEdicao.setOrganizacaoId(org.getId());
                    fatorEmEdicao.setProdutorId(null);
                } else {
                    fatorEmEdicao.setProdutorId(((Produtor) entidade).getId());
                    fatorEmEdicao.setOrganizacaoId(null);
                }
            }

            fatorEmEdicao.setTipo(cbTipoFator.getValue());
            fatorEmEdicao.setDescricao(descricao.trim());
            fatorEmEdicao.setIntensidade((int) sldIntensidade.getValue());

            if (isNovo) {
                fatorEmEdicao = swotRepo.salvarFator(fatorEmEdicao);
                
                // Insere diretamente na lista observável (sem recarregar o banco inteiro)
                String t = fatorEmEdicao.getTipo().toUpperCase();
                if (t.contains("FORC") || t.contains("FORÇ")) listaForcas.add(fatorEmEdicao);
                else if (t.contains("FRAQ")) listaFraquezas.add(fatorEmEdicao);
                else if (t.contains("OPORT")) listaOportunidades.add(fatorEmEdicao);
                else if (t.contains("AMEA")) listaAmeacas.add(fatorEmEdicao);

                lblStatusMensagem.setStyle("-fx-text-fill: #2e7d32;");
                lblStatusMensagem.setText("Fator adicionado com sucesso.");
            } else {
                swotRepo.atualizarFator(fatorEmEdicao);
                lblStatusMensagem.setStyle("-fx-text-fill: #2e7d32;");
                lblStatusMensagem.setText("Fator atualizado com sucesso.");
                // Em caso de edição, recarrega
                carregarDadosEntidade(entidade);
            }

            atualizarContadores();
            limparFormularioFator();
        } catch (Exception e) {
            exibirMensagemErro("Erro ao salvar fator: " + e.getMessage());
        }
    }

    @FXML
    public void importarDadosPestel() {
        Object entidade = cbEntidadeAlvo.getValue();
        if (entidade == null) {
            exibirMensagemErro("Selecione uma Cooperativa ou Produtor Rural antes de importar!");
            return;
        }

        try {
            Integer orgId = (entidade instanceof Organizacao org) ? org.getId() : null;
            Integer prodId = (entidade instanceof Produtor prod) ? prod.getId() : null;

            int total = swotRepo.importarFatoresDaPestel(orgId, prodId);
            lblStatusMensagem.setStyle("-fx-text-fill: #2e7d32;");
            lblStatusMensagem.setText("Importação concluída: " + total + " novo(s) fator(es) importado(s) da PESTEL.");
            carregarDadosEntidade(entidade);
        } catch (Exception e) {
            exibirMensagemErro("Erro ao importar dados da PESTEL: " + e.getMessage());
        }
    }

    @FXML
    public void editarForcaSelecionada() {
        carregarParaEdicao(tblForcas.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void editarFraquezaSelecionada() {
        carregarParaEdicao(tblFraquezas.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void editarOportunidadeSelecionada() {
        carregarParaEdicao(tblOportunidades.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void editarAmeacaSelecionada() {
        carregarParaEdicao(tblAmeacas.getSelectionModel().getSelectedItem());
    }

    private void carregarParaEdicao(FatorSwot f) {
        if (f == null) {
            exibirMensagemErro("Selecione um fator na tabela para editar.");
            return;
        }
        this.fatorEmEdicao = f;
        cbTipoFator.setValue(f.getTipo().toUpperCase());
        sldIntensidade.setValue(f.getIntensidade());
        txtDescricaoFator.setText(f.getDescricao());
        lblStatusMensagem.setStyle("-fx-text-fill: #1976D2;");
        lblStatusMensagem.setText("Editando fator #" + f.getId());
    }

    @FXML
    public void excluirForcaSelecionada() {
        excluirFator(tblForcas.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void excluirFraquezaSelecionada() {
        excluirFator(tblFraquezas.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void excluirOportunidadeSelecionada() {
        excluirFator(tblOportunidades.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void excluirAmeacaSelecionada() {
        excluirFator(tblAmeacas.getSelectionModel().getSelectedItem());
    }

    private void excluirFator(FatorSwot f) {
        if (f == null || f.getId() == null) {
            exibirMensagemErro("Selecione um fator para excluir.");
            return;
        }

        try {
            int idParaRemover = f.getId();
            
            // 1. Remove do banco SQLite
            swotRepo.excluirFator(idParaRemover);

            // 2. Remove da lista pelo ID garantindo que não falhe por referência de ponteiro
            listaForcas.removeIf(item -> item.getId() != null && item.getId().equals(idParaRemover));
            listaFraquezas.removeIf(item -> item.getId() != null && item.getId().equals(idParaRemover));
            listaOportunidades.removeIf(item -> item.getId() != null && item.getId().equals(idParaRemover));
            listaAmeacas.removeIf(item -> item.getId() != null && item.getId().equals(idParaRemover));

            // 3. Atualiza os contadores
            atualizarContadores();

            // 4. Limpa e atualiza seletores secundários
            atualizarSeletoresCruzamento(cbTipoCruzamento.getValue());

            // 5. Garante a atualização visual na thread gráfica do JavaFX
            Platform.runLater(() -> {
                tblForcas.refresh();
                tblFraquezas.refresh();
                tblOportunidades.refresh();
                tblAmeacas.refresh();
            });

            lblStatusMensagem.setStyle("-fx-text-fill: #2e7d32;");
            lblStatusMensagem.setText("Fator removido com sucesso.");
        } catch (Exception e) {
            exibirMensagemErro("Erro ao excluir fator: " + e.getMessage());
        }
    }

    // ==========================================
    //          OPERAÇÕES: CRUZAMENTOS
    // ==========================================

    @FXML
    public void salvarCruzamento() {
        Object entidade = cbEntidadeAlvo.getValue();
        if (entidade == null) {
            exibirMensagemErro("Selecione uma entidade antes de registrar a estratégia!");
            return;
        }

        FatorSwot interno = cbFatorInterno.getValue();
        FatorSwot externo = cbFatorExterno.getValue();
        String acao = txtEstrategiaProposta.getText();

        if (interno == null || externo == null || acao == null || acao.trim().isEmpty()) {
            exibirMensagemErro("Preencha todos os campos do cruzamento estratégico.");
            return;
        }

        try {
            CruzamentoSwot c = new CruzamentoSwot();
            if (entidade instanceof Organizacao org) {
                c.setOrganizacaoId(org.getId());
            } else {
                c.setProdutorId(((Produtor) entidade).getId());
            }

            String tipoAbrev = cbTipoCruzamento.getValue().substring(0, 2);
            c.setTipoEstrategia(tipoAbrev);
            c.setFatorInternoId(interno.getId());
            c.setFatorExternoId(externo.getId());
            c.setDescricaoFatorInterno(interno.getDescricao());
            c.setDescricaoFatorExterno(externo.getDescricao());
            c.setEstrategiaProposta(acao.trim());

            c = swotRepo.salvarCruzamento(c);
            listaCruzamentos.add(0, c);

            lblStatusMensagem.setStyle("-fx-text-fill: #2e7d32;");
            lblStatusMensagem.setText("Estratégia cruzada registrada com sucesso.");

            limparFormularioCruzamento();
        } catch (Exception e) {
            exibirMensagemErro("Erro ao registrar cruzamento: " + e.getMessage());
        }
    }

    @FXML
    public void excluirCruzamentoSelecionado() {
        CruzamentoSwot selecionado = tblCruzamentos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            exibirMensagemErro("Selecione uma estratégia na tabela para excluir.");
            return;
        }

        try {
            swotRepo.excluirCruzamento(selecionado.getId());
            listaCruzamentos.remove(selecionado);
            lblStatusMensagem.setStyle("-fx-text-fill: #2e7d32;");
            lblStatusMensagem.setText("Estratégia cruzada removida com sucesso.");
        } catch (Exception e) {
            exibirMensagemErro("Erro ao excluir estratégia: " + e.getMessage());
        }
    }

    @FXML
    public void limparFormularioFator() {
        fatorEmEdicao = null;
        cbTipoFator.getSelectionModel().selectFirst();
        sldIntensidade.setValue(3.0);
        txtDescricaoFator.clear();
    }

    @FXML
    public void limparFormularioCruzamento() {
        txtEstrategiaProposta.clear();
    }

    private void limparTudo() {
        listaForcas.clear();
        listaFraquezas.clear();
        listaOportunidades.clear();
        listaAmeacas.clear();
        listaCruzamentos.clear();
        limparFormularioFator();
        limparFormularioCruzamento();
        atualizarContadores();
    }

    private void atualizarContadores() {
        lblCountForcas.setText(String.valueOf(listaForcas.size()));
        lblCountFraquezas.setText(String.valueOf(listaFraquezas.size()));
        lblCountOportunidades.setText(String.valueOf(listaOportunidades.size()));
        lblCountAmeacas.setText(String.valueOf(listaAmeacas.size()));
    }

    private void exibirMensagemErro(String msg) {
        lblStatusMensagem.setStyle("-fx-text-fill: #d32f2f;");
        lblStatusMensagem.setText(msg);
    }
}