package com.gestao.agro.controller;

import com.gestao.agro.model.Organizacao;
import com.gestao.agro.model.PlanoAcao5W2H;
import com.gestao.agro.model.Produtor;
import com.gestao.agro.repository.OrganizacaoRepository;
import com.gestao.agro.repository.PlanoAcaoRepository;
import com.gestao.agro.repository.ProdutorRepository;
import com.gestao.agro.util.SessaoUsuario;
import com.gestao.agro.util.GeminiService;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PlanoAcaoController {

    // --- Seleção de Entidade ---
    @FXML private RadioButton rbTipoOrg;
    @FXML private RadioButton rbTipoProd;
    @FXML private ToggleGroup grpTipoAlvo;
    @FXML private Label lblEntidadeAlvo;
    @FXML private ComboBox<Object> cbEntidadeAlvo;
    @FXML private TextField txtConsultor;

    // --- Formulário 5W2H ---
    @FXML private TextField txtWhat;
    @FXML private TextField txtWhy;
    @FXML private TextField txtWhere;
    @FXML private TextField txtWhen;
    @FXML private TextField txtWho;
    @FXML private TextField txtHow;
    @FXML private TextField txtHowMuch;
    @FXML private ComboBox<String> cbStatus;
    
    // --- Botão e Spinner da IA ---
    @FXML private Button btnGerarComoIA;
    @FXML private ProgressIndicator spinnerIA;

    // --- Tabela de Planos de Ação ---
    @FXML private Label lblTotalCusto;
    @FXML private TableView<PlanoAcao5W2H> tblPlanoAcao;
    @FXML private TableColumn<PlanoAcao5W2H, String> colWhat;
    @FXML private TableColumn<PlanoAcao5W2H, String> colWhy;
    @FXML private TableColumn<PlanoAcao5W2H, String> colWhere;
    @FXML private TableColumn<PlanoAcao5W2H, String> colWhen;
    @FXML private TableColumn<PlanoAcao5W2H, String> colWho;
    @FXML private TableColumn<PlanoAcao5W2H, String> colHow;
    @FXML private TableColumn<PlanoAcao5W2H, Double> colHowMuch;
    @FXML private TableColumn<PlanoAcao5W2H, String> colStatus;

    @FXML private Label lblStatusMensagem;

    private final PlanoAcaoRepository planoRepo = new PlanoAcaoRepository();
    private final OrganizacaoRepository orgRepo = new OrganizacaoRepository();
    private final ProdutorRepository prodRepo = new ProdutorRepository();

    private final ObservableList<PlanoAcao5W2H> listaPlanos = FXCollections.observableArrayList();
    private PlanoAcao5W2H planoEmEdicao = null;
    private boolean ignorarListener = false;

    // Formatador de Moeda Brasileira
    private final NumberFormat formatadorMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @FXML
    public void initialize() {
        txtConsultor.setText(SessaoUsuario.getNomeConsultor());

        configurarFormulario();
        configurarSeletoresEntidade();
        configurarTabela();

        // Oculta o spinner de carregamento da IA ao iniciar
        if (spinnerIA != null) {
            spinnerIA.setVisible(false);
            spinnerIA.setManaged(false);
        }

        alternarTipoAlvo();
    }

    private void configurarFormulario() {
        cbStatus.setItems(FXCollections.observableArrayList(
            "NÃO INICIADO", "EM ANDAMENTO", "CONCLUÍDO", "CANCELADO"
        ));
        cbStatus.getSelectionModel().selectFirst();
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

    private void configurarTabela() {
        colWhat.setCellValueFactory(new PropertyValueFactory<>("whatAcao"));
        colWhy.setCellValueFactory(new PropertyValueFactory<>("whyMotivo"));
        colWhere.setCellValueFactory(new PropertyValueFactory<>("whereLocal"));
        colWhen.setCellValueFactory(new PropertyValueFactory<>("whenPrazo"));
        colWho.setCellValueFactory(new PropertyValueFactory<>("whoResponsavel"));
        colHow.setCellValueFactory(new PropertyValueFactory<>("howMetodo"));
        
        // Formata a coluna How Much para Moeda (R$)
        colHowMuch.setCellValueFactory(new PropertyValueFactory<>("howMuchCusto"));
        colHowMuch.setCellFactory(tc -> new TableCell<PlanoAcao5W2H, Double>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) {
                    setText(null);
                } else {
                    setText(formatadorMoeda.format(valor));
                }
            }
        });

        // Estiliza a coluna Status com cores
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(tc -> new TableCell<PlanoAcao5W2H, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status.toUpperCase()) {
                        case "CONCLUÍDO" -> setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                        case "EM ANDAMENTO" -> setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold;");
                        case "CANCELADO" -> setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
                        default -> setStyle("-fx-text-fill: #757575; -fx-font-weight: bold;");
                    }
                }
            }
        });

        tblPlanoAcao.setItems(listaPlanos);
    }

    private void alternarTipoAlvo() {
        ignorarListener = true;
        try {
            cbEntidadeAlvo.getItems().clear();
            limparFormulario();
            listaPlanos.clear();
            calcularTotalInvestimento();

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
            listaPlanos.clear();
            calcularTotalInvestimento();
            return;
        }

        try {
            List<PlanoAcao5W2H> planos;
            if (entidade instanceof Organizacao org) {
                planos = planoRepo.listarPorOrganizacao(org.getId());
            } else {
                planos = planoRepo.listarPorProdutor(((Produtor) entidade).getId());
            }

            listaPlanos.setAll(planos);
            calcularTotalInvestimento();

            Platform.runLater(() -> tblPlanoAcao.refresh());

            lblStatusMensagem.setStyle("-fx-text-fill: #2e7d32;");
            lblStatusMensagem.setText("Plano de Ação 5W2H carregado com sucesso.");
        } catch (Exception e) {
            exibirMensagemErro("Erro ao carregar Planos de Ação: " + e.getMessage());
        }
    }

    private void calcularTotalInvestimento() {
        double total = listaPlanos.stream()
                .filter(p -> p.getHowMuchCusto() != null && !p.getStatus().equalsIgnoreCase("CANCELADO"))
                .mapToDouble(PlanoAcao5W2H::getHowMuchCusto)
                .sum();
        lblTotalCusto.setText(formatadorMoeda.format(total));
    }

    // ==========================================
    //            OPERAÇÕES: CRUD
    // ==========================================

    @FXML
    public void salvarPlano() {
        Object entidade = cbEntidadeAlvo.getValue();
        if (entidade == null) {
            exibirMensagemErro("Selecione uma Cooperativa ou Produtor Rural antes de salvar!");
            return;
        }

        String what = txtWhat.getText();
        if (what == null || what.trim().isEmpty()) {
            exibirMensagemErro("O campo 'O quê? (What)' é obrigatório.");
            return;
        }

        double custo = 0.0;
        try {
            String valorTexto = txtHowMuch.getText().replace(",", ".");
            if (!valorTexto.trim().isEmpty()) {
                custo = Double.parseDouble(valorTexto);
            }
        } catch (NumberFormatException e) {
            exibirMensagemErro("Valor inválido no campo 'Quanto?'. Digite apenas números.");
            return;
        }

        try {
            boolean isNovo = (planoEmEdicao == null || planoEmEdicao.getId() == null);
            if (isNovo) {
                planoEmEdicao = new PlanoAcao5W2H();
                if (entidade instanceof Organizacao org) {
                    planoEmEdicao.setOrganizacaoId(org.getId());
                } else {
                    planoEmEdicao.setProdutorId(((Produtor) entidade).getId());
                }
            }

            planoEmEdicao.setWhatAcao(what.trim());
            planoEmEdicao.setWhyMotivo(txtWhy.getText() != null ? txtWhy.getText().trim() : "");
            planoEmEdicao.setWhereLocal(txtWhere.getText() != null ? txtWhere.getText().trim() : "");
            planoEmEdicao.setWhenPrazo(txtWhen.getText() != null ? txtWhen.getText().trim() : "");
            planoEmEdicao.setWhoResponsavel(txtWho.getText() != null ? txtWho.getText().trim() : "");
            planoEmEdicao.setHowMetodo(txtHow.getText() != null ? txtHow.getText().trim() : "");
            planoEmEdicao.setHowMuchCusto(custo);
            planoEmEdicao.setStatus(cbStatus.getValue() != null ? cbStatus.getValue() : "NÃO INICIADO");

            if (isNovo) {
                planoEmEdicao = planoRepo.salvar(planoEmEdicao);
                listaPlanos.add(0, planoEmEdicao);
                lblStatusMensagem.setStyle("-fx-text-fill: #2e7d32;");
                lblStatusMensagem.setText("Ação registrada com sucesso no plano.");
            } else {
                planoRepo.atualizar(planoEmEdicao);
                lblStatusMensagem.setStyle("-fx-text-fill: #2e7d32;");
                lblStatusMensagem.setText("Ação do plano atualizada.");
            }

            calcularTotalInvestimento();
            limparFormulario();
            Platform.runLater(() -> tblPlanoAcao.refresh());
        } catch (Exception e) {
            exibirMensagemErro("Erro ao salvar ação no Plano: " + e.getMessage());
        }
    }

    @FXML
    public void editarItemSelecionado() {
        PlanoAcao5W2H selecionado = tblPlanoAcao.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            exibirMensagemErro("Selecione uma ação na tabela para editar.");
            return;
        }

        this.planoEmEdicao = selecionado;
        txtWhat.setText(selecionado.getWhatAcao());
        txtWhy.setText(selecionado.getWhyMotivo());
        txtWhere.setText(selecionado.getWhereLocal());
        txtWhen.setText(selecionado.getWhenPrazo());
        txtWho.setText(selecionado.getWhoResponsavel());
        txtHow.setText(selecionado.getHowMetodo());
        txtHowMuch.setText(selecionado.getHowMuchCusto() != null ? String.valueOf(selecionado.getHowMuchCusto()) : "");
        cbStatus.setValue(selecionado.getStatus());

        lblStatusMensagem.setStyle("-fx-text-fill: #1976D2;");
        lblStatusMensagem.setText("A editar ação #" + selecionado.getId());
    }

    @FXML
    public void excluirItemSelecionado() {
        PlanoAcao5W2H selecionado = tblPlanoAcao.getSelectionModel().getSelectedItem();
        if (selecionado == null || selecionado.getId() == null) {
            exibirMensagemErro("Selecione uma ação na tabela para excluir.");
            return;
        }

        try {
            int idParaRemover = selecionado.getId();
            planoRepo.excluir(idParaRemover);
            listaPlanos.removeIf(i -> i.getId() != null && i.getId().equals(idParaRemover));

            calcularTotalInvestimento();
            Platform.runLater(() -> tblPlanoAcao.refresh());

            lblStatusMensagem.setStyle("-fx-text-fill: #2e7d32;");
            lblStatusMensagem.setText("Ação removida com sucesso.");
        } catch (Exception e) {
            exibirMensagemErro("Erro ao excluir ação: " + e.getMessage());
        }
    }

    // ==========================================
    //       IMPORTAÇÃO AUTOMATIZADA: GUT
    // ==========================================

    @FXML
    public void importarPrioridadesGut() {
        Object entidade = cbEntidadeAlvo.getValue();
        if (entidade == null) {
            exibirMensagemErro("Selecione uma entidade antes de importar prioridades!");
            return;
        }

        try {
            Integer orgId = (entidade instanceof Organizacao org) ? org.getId() : null;
            Integer prodId = (entidade instanceof Produtor prod) ? prod.getId() : null;

            int total = planoRepo.importarItensPrioritariosDoGut(orgId, prodId);
            lblStatusMensagem.setStyle("-fx-text-fill: #2e7d32;");
            lblStatusMensagem.setText("Importação concluída: " + total + " itens prioritários trazidos da Matriz GUT.");
            
            carregarDadosEntidade(entidade);
        } catch (Exception e) {
            exibirMensagemErro("Erro ao importar dados da Matriz GUT: " + e.getMessage());
        }
    }

    @FXML
    public void limparFormulario() {
        this.planoEmEdicao = null;
        txtWhat.clear();
        txtWhy.clear();
        txtWhere.clear();
        txtWhen.clear();
        txtWho.clear();
        txtHow.clear();
        txtHowMuch.clear();
        cbStatus.getSelectionModel().selectFirst();
    }

    private void exibirMensagemErro(String msg) {
        lblStatusMensagem.setStyle("-fx-text-fill: #d32f2f;");
        lblStatusMensagem.setText(msg);
    }

    @FXML
    public void gerarComoComIA() {
        String what = txtWhat.getText();
        String why = txtWhy.getText();

        if (what == null || what.trim().isEmpty()) {
            exibirMensagemErro("Preencha primeiro o campo 'O quê? (What)' para a IA saber o que vai sugerir.");
            return;
        }

        // 1. Desativa o campo, o botão e exibe o spinner de progresso
        txtHow.setDisable(true);
        txtHow.setText("✨ A gerar metodologia com o Gemini... aguarde.");
        if (btnGerarComoIA != null) btnGerarComoIA.setDisable(true);
        if (spinnerIA != null) {
            spinnerIA.setVisible(true);
            spinnerIA.setManaged(true);
        }

        lblStatusMensagem.setStyle("-fx-text-fill: #1976D2;");
        lblStatusMensagem.setText("A contactar a Inteligência Artificial para o 5W2H...");

        // 2. Chamada assíncrona para não travar a interface
        CompletableFuture.supplyAsync(() -> {
            try {
                return GeminiService.gerarComo5W2H(what, why);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenAccept(resultado -> {
            Platform.runLater(() -> {
                // 3. Restaura os elementos de UI após sucesso
                txtHow.setDisable(false);
                txtHow.setText(resultado);
                if (btnGerarComoIA != null) btnGerarComoIA.setDisable(false);
                if (spinnerIA != null) {
                    spinnerIA.setVisible(false);
                    spinnerIA.setManaged(false);
                }
                lblStatusMensagem.setStyle("-fx-text-fill: #2e7d32;");
                lblStatusMensagem.setText("Metodologia gerada com sucesso pela IA!");
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                // 4. Restaura os elementos de UI após erro
                txtHow.setDisable(false);
                txtHow.clear();
                if (btnGerarComoIA != null) btnGerarComoIA.setDisable(false);
                if (spinnerIA != null) {
                    spinnerIA.setVisible(false);
                    spinnerIA.setManaged(false);
                }
                exibirMensagemErro("Falha ao usar IA: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()));
            });
            return null;
        });
    }
}