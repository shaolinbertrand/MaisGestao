package com.gestao.agro.controller;

import com.gestao.agro.model.Organizacao;
import com.gestao.agro.model.Produtor;
import com.gestao.agro.repository.OrganizacaoRepository;
import com.gestao.agro.repository.ProdutorRepository;
import com.gestao.agro.util.SessaoUsuario;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrganizacaoFormController {

    // --- Controles Aba 1: Cooperativa / Organização ---
    @FXML private ComboBox<Organizacao> cbOrgEdicao;
    @FXML private Button btnSalvarOrg;
    @FXML private Button btnExcluirOrg;
    @FXML private Label lblOrgMsg;

    @FXML private TextField txtOrgNome;
    @FXML private TextField txtOrgCnpj;
    @FXML private TextField txtOrgMunicipio;
    @FXML private TextField txtOrgAssociados;
    @FXML private TextField txtOrgFaturamento;
    @FXML private TextField txtOrgProdutos;
    @FXML private TextField txtOrgCanais;
    @FXML private CheckBox chkOrgCaf;
    @FXML private CheckBox chkOrgPaaPnae;
    @FXML private TextArea txtOrgDocumentos;
    @FXML private TextArea txtOrgInfra;
    @FXML private TextArea txtOrgDificuldades;

    // --- Controles Aba 2: Produtor Rural ---
    @FXML private ComboBox<Organizacao> cbOrganizacaoAtiva;
    @FXML private CheckBox chkMeusBloqueios;
    @FXML private TextField txtProdNome;
    @FXML private TextField txtProdCpf;
    @FXML private DatePicker dpDataNascimento;
    @FXML private ComboBox<String> cbGenero;
    @FXML private TextField txtProdComunidade;
    @FXML private TextField txtProdArea;
    @FXML private TextField txtProdCulturas;
    @FXML private CheckBox chkDestinoCoop;
    @FXML private CheckBox chkDestinoVendaDireta;
    @FXML private CheckBox chkDestinoConsumo;
    @FXML private TextField txtProdConsultor;
    @FXML private CheckBox chkProdCaf;
    @FXML private Label lblProdMsg;

    @FXML private Button btnSalvarProd;
    @FXML private Button btnExcluirProd;
    @FXML private Button btnAlternarBloqueio;

    // --- Tabela de Produtores ---
    @FXML private TableView<Produtor> tblProdutores;
    @FXML private TableColumn<Produtor, String> colProdNome;
    @FXML private TableColumn<Produtor, Integer> colProdIdade;
    @FXML private TableColumn<Produtor, String> colProdGenero;
    @FXML private TableColumn<Produtor, String> colProdCpf;
    @FXML private TableColumn<Produtor, String> colProdComunidade;
    @FXML private TableColumn<Produtor, String> colProdDestino;
    @FXML private TableColumn<Produtor, String> colProdConsultor;

    // --- Controles de Busca / Filtro ---
    @FXML private TextField txtBuscaOrg;
    @FXML private TextField txtBuscaProd;

    // Listas filtradas com suporte a busca dinâmica
    private FilteredList<Organizacao> listaOrgFiltrada;
    private FilteredList<Produtor> listaProdFiltrada;

    private final OrganizacaoRepository orgRepo = new OrganizacaoRepository();
    private final ProdutorRepository prodRepo = new ProdutorRepository();
    private final ObservableList<Produtor> listaProdutores = FXCollections.observableArrayList();
    private final ObservableList<Organizacao> listaOrganizacoes = FXCollections.observableArrayList();

    private Organizacao organizacaoEmEdicao = null;
    private Produtor produtorEmEdicao = null;

    @FXML
    public void initialize() {
        configurarOpcoesCampos();
        configurarMascaraDatePicker();
        configurarMascaraCpf();
        configurarTabelaProdutores();
        configurarFiltroOrganizacao();
        configurarFiltroProdutores();
        configurarSeletoresOrganizacao();

        recarregarOrganizacoes();
        novoFormularioOrg();
    }

    private void configurarOpcoesCampos() {
        cbGenero.setItems(FXCollections.observableArrayList(
                "MASCULINO", "FEMININO", "OUTRO", "NÃO DECLARADO"
        ));

        txtProdConsultor.setText(SessaoUsuario.getNomeConsultor());
    }

    private void configurarTabelaProdutores() {
        colProdNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colProdIdade.setCellValueFactory(new PropertyValueFactory<>("idade"));
        colProdGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));
        colProdCpf.setCellValueFactory(new PropertyValueFactory<>("cpf"));
        colProdComunidade.setCellValueFactory(new PropertyValueFactory<>("comunidade"));
        colProdDestino.setCellValueFactory(new PropertyValueFactory<>("destinoProducao"));
        colProdConsultor.setCellValueFactory(new PropertyValueFactory<>("consultorBloqueio"));

        // 1. Cria a FilteredList vinculada à lista mestre de produtores
        listaProdFiltrada = new FilteredList<>(listaProdutores, p -> true);

        // 2. Envolve em SortedList vinculada ao comparador da tabela para habilitar ordenação clicável
        SortedList<Produtor> listaProdOrdenada = new SortedList<>(listaProdFiltrada);
        listaProdOrdenada.comparatorProperty().bind(tblProdutores.comparatorProperty());

        // 3. Define a SortedList na tabela
        tblProdutores.setItems(listaProdOrdenada);

        tblProdutores.getSelectionModel().selectedItemProperty().addListener((obs, antigo, selecionado) -> {
            if (selecionado != null) {
                carregarProdutorParaEdicao(selecionado);
            }
        });
    }

    private void configurarFiltroOrganizacao() {
        listaOrgFiltrada = new FilteredList<>(listaOrganizacoes, p -> true);

        if (txtBuscaOrg != null) {
            txtBuscaOrg.textProperty().addListener((obs, antigo, termo) -> {
                listaOrgFiltrada.setPredicate(org -> {
                    if (termo == null || termo.isBlank()) {
                        return true;
                    }
                    String termoLower = termo.toLowerCase().trim();
                    boolean bateNome = org.getNome() != null && org.getNome().toLowerCase().contains(termoLower);
                    boolean bateCnpj = org.getCnpj() != null && org.getCnpj().contains(termoLower);
                    boolean bateMun = org.getMunicipioComunidade() != null && org.getMunicipioComunidade().toLowerCase().contains(termoLower);

                    return bateNome || bateCnpj || bateMun;
                });

                if (!listaOrgFiltrada.isEmpty()) {
                    cbOrgEdicao.getSelectionModel().selectFirst();
                }
            });
        }
    }

    private void configurarFiltroProdutores() {
        if (txtBuscaProd != null) {
            txtBuscaProd.textProperty().addListener((obs, antigo, termo) -> {
                listaProdFiltrada.setPredicate(prod -> {
                    if (termo == null || termo.isBlank()) {
                        return true;
                    }
                    String termoLower = termo.toLowerCase().trim();
                    String termoDigitos = termo.replaceAll("\\D", "");

                    boolean bateNome = prod.getNome() != null && prod.getNome().toLowerCase().contains(termoLower);
                    boolean bateComunidade = prod.getComunidade() != null && prod.getComunidade().toLowerCase().contains(termoLower);

                    // Busca de CPF flexível (com pontuação ou apenas números)
                    boolean bateCpf = false;
                    if (prod.getCpf() != null) {
                        bateCpf = prod.getCpf().contains(termoLower)
                                || (!termoDigitos.isEmpty() && prod.getCpf().replaceAll("\\D", "").contains(termoDigitos));
                    }

                    return bateNome || bateComunidade || bateCpf;
                });
            });
        }
    }

    private void configurarSeletoresOrganizacao() {
        StringConverter<Organizacao> orgConverter = new StringConverter<>() {
            @Override
            public String toString(Organizacao org) {
                return (org != null) ? (org.getId() + " - " + org.getNome()) : "";
            }

            @Override
            public Organizacao fromString(String string) {
                return null;
            }
        };

        cbOrgEdicao.setConverter(orgConverter);
        cbOrgEdicao.setItems(listaOrgFiltrada);
        cbOrgEdicao.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selecionada) -> {
            if (selecionada != null) {
                carregarOrganizacaoParaEdicao(selecionada);
            }
        });

        cbOrganizacaoAtiva.setConverter(orgConverter);
        cbOrganizacaoAtiva.setItems(listaOrganizacoes);
        cbOrganizacaoAtiva.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selecionada) -> {
            if (!chkMeusBloqueios.isSelected()) {
                if (selecionada != null) {
                    carregarProdutores(selecionada.getId());
                } else {
                    listaProdutores.clear();
                }
            }
            limparCamposProdutor();
        });
    }

    private void configurarMascaraDatePicker() {
        DateTimeFormatter formatoBr = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dpDataNascimento.setPromptText("DD/MM/AAAA");

        dpDataNascimento.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? formatoBr.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.trim().isEmpty()) {
                    try {
                        return LocalDate.parse(string.trim(), formatoBr);
                    } catch (Exception e) {
                        return null;
                    }
                }
                return null;
            }
        });

        dpDataNascimento.getEditor().textProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) {
                return;
            }
            if (antigo != null && novo.length() < antigo.length()) {
                return;
            }

            String apenasDigitos = novo.replaceAll("\\D", "");
            if (apenasDigitos.length() > 8) {
                apenasDigitos = apenasDigitos.substring(0, 8);
            }

            StringBuilder formatado = new StringBuilder();
            for (int i = 0; i < apenasDigitos.length(); i++) {
                if (i == 2 || i == 4) {
                    formatado.append('/');
                }
                formatado.append(apenasDigitos.charAt(i));
            }

            String resultado = formatado.toString();
            if (!novo.equals(resultado)) {
                dpDataNascimento.getEditor().setText(resultado);
                dpDataNascimento.getEditor().positionCaret(resultado.length());
            }
        });

        dpDataNascimento.getEditor().focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String texto = dpDataNascimento.getEditor().getText();
                if (texto != null && texto.length() == 10) {
                    try {
                        dpDataNascimento.setValue(LocalDate.parse(texto, formatoBr));
                    } catch (Exception e) {
                        dpDataNascimento.setValue(null);
                    }
                } else if (texto == null || texto.isBlank()) {
                    dpDataNascimento.setValue(null);
                }
            }
        });
    }

    private void configurarMascaraCpf() {
        txtProdCpf.setPromptText("000.000.000-00");
        txtProdCpf.textProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) {
                return;
            }
            if (antigo != null && novo.length() < antigo.length()) {
                return; // Permite apagar livremente
            }
            String apenasDigitos = novo.replaceAll("\\D", "");
            if (apenasDigitos.length() > 11) {
                apenasDigitos = apenasDigitos.substring(0, 11);
            }

            StringBuilder formatado = new StringBuilder();
            for (int i = 0; i < apenasDigitos.length(); i++) {
                if (i == 3 || i == 6) {
                    formatado.append('.');
                } else if (i == 9) {
                    formatado.append('-');
                }
                formatado.append(apenasDigitos.charAt(i));
            }

            String resultado = formatado.toString();
            if (!novo.equals(resultado)) {
                Platform.runLater(() -> {
                    txtProdCpf.setText(resultado);
                    txtProdCpf.positionCaret(resultado.length());
                });
            }
        });
    }

    @FXML
    public void recarregarOrganizacoes() {
        try {
            Organizacao ativa = cbOrganizacaoAtiva.getValue();
            listaOrganizacoes.clear();
            List<Organizacao> todas = orgRepo.listarTodas();
            listaOrganizacoes.addAll(todas);

            if (ativa != null) {
                for (Organizacao o : todas) {
                    if (o.getId().equals(ativa.getId())) {
                        cbOrganizacaoAtiva.getSelectionModel().select(o);
                        break;
                    }
                }
            } else if (!todas.isEmpty()) {
                cbOrganizacaoAtiva.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar organizações: " + e.getMessage());
        }
    }

    // ==========================================
    //           CRUD ORGANIZAÇÃO
    // ==========================================
    @FXML
    public void novoFormularioOrg() {
        organizacaoEmEdicao = null;
        limparCamposOrg();
        btnSalvarOrg.setText("Salvar Cooperativa");
        btnExcluirOrg.setDisable(true);
        lblOrgMsg.setText("");
    }

    private void carregarOrganizacaoParaEdicao(Organizacao org) {
        organizacaoEmEdicao = org;
        txtOrgNome.setText(org.getNome());
        txtOrgCnpj.setText(org.getCnpj());
        txtOrgMunicipio.setText(org.getMunicipioComunidade());
        txtOrgAssociados.setText(org.getNumeroAssociados() != null ? String.valueOf(org.getNumeroAssociados()) : "");
        txtOrgFaturamento.setText(org.getFaturamentoEstimado() != null ? String.valueOf(org.getFaturamentoEstimado()) : "");
        txtOrgProdutos.setText(org.getPrincipaisProdutos());
        txtOrgCanais.setText(org.getCanaisComercializacao());
        chkOrgCaf.setSelected(org.isPossuiCaf());
        chkOrgPaaPnae.setSelected(org.isParticipacaoPaaPnae());
        txtOrgDocumentos.setText(org.getSituacaoDocumental());
        txtOrgInfra.setText(org.getInfraestrutura());
        txtOrgDificuldades.setText(org.getDificuldades());

        btnSalvarOrg.setText("Atualizar Cooperativa");
        btnExcluirOrg.setDisable(false);
        lblOrgMsg.setText("");
    }

    @FXML
    public void salvarOuAtualizarOrganizacao() {
        if (txtOrgNome.getText().trim().isEmpty() || txtOrgMunicipio.getText().trim().isEmpty()) {
            lblOrgMsg.setStyle("-fx-text-fill: #d32f2f;");
            lblOrgMsg.setText("Preencha Nome e Município!");
            return;
        }

        try {
            Organizacao org = (organizacaoEmEdicao != null) ? organizacaoEmEdicao : new Organizacao();
            org.setNome(txtOrgNome.getText().trim());
            org.setCnpj(txtOrgCnpj.getText().trim());
            org.setMunicipioComunidade(txtOrgMunicipio.getText().trim());

            if (!txtOrgAssociados.getText().trim().isEmpty()) {
                org.setNumeroAssociados(Integer.parseInt(txtOrgAssociados.getText().trim()));
            } else {
                org.setNumeroAssociados(null);
            }

            if (!txtOrgFaturamento.getText().trim().isEmpty()) {
                org.setFaturamentoEstimado(Double.parseDouble(txtOrgFaturamento.getText().trim()));
            } else {
                org.setFaturamentoEstimado(null);
            }

            org.setPrincipaisProdutos(txtOrgProdutos.getText().trim());
            org.setCanaisComercializacao(txtOrgCanais.getText().trim());
            org.setPossuiCaf(chkOrgCaf.isSelected());
            org.setParticipacaoPaaPnae(chkOrgPaaPnae.isSelected());
            org.setSituacaoDocumental(txtOrgDocumentos.getText().trim());
            org.setInfraestrutura(txtOrgInfra.getText().trim());
            org.setDificuldades(txtOrgDificuldades.getText().trim());

            if (organizacaoEmEdicao == null) {
                Organizacao salva = orgRepo.salvar(org);
                lblOrgMsg.setStyle("-fx-text-fill: #2e7d32;");
                lblOrgMsg.setText("Cooperativa salva com sucesso!");
                recarregarOrganizacoes();
                cbOrganizacaoAtiva.getSelectionModel().select(salva);
                novoFormularioOrg();
            } else {
                orgRepo.atualizar(org);
                lblOrgMsg.setStyle("-fx-text-fill: #2e7d32;");
                lblOrgMsg.setText("Cooperativa atualizada com sucesso!");
                recarregarOrganizacoes();
            }
        } catch (Exception e) {
            lblOrgMsg.setStyle("-fx-text-fill: #d32f2f;");
            lblOrgMsg.setText("Erro ao salvar: " + e.getMessage());
        }
    }

    @FXML
    public void excluirOrganizacao() {
        if (organizacaoEmEdicao == null || organizacaoEmEdicao.getId() == null) {
            return;
        }

        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar Exclusão");
        alerta.setHeaderText("Excluir: " + organizacaoEmEdicao.getNome());
        alerta.setContentText("Atenção: Os produtores e diagnósticos vinculados a esta cooperativa também serão excluídos. Deseja continuar?");

        Optional<ButtonType> resultado = alerta.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                orgRepo.excluir(organizacaoEmEdicao.getId());
                recarregarOrganizacoes();
                novoFormularioOrg();
                lblOrgMsg.setStyle("-fx-text-fill: #2e7d32;");
                lblOrgMsg.setText("Organização excluída com sucesso!");
            } catch (Exception e) {
                lblOrgMsg.setStyle("-fx-text-fill: #d32f2f;");
                lblOrgMsg.setText("Erro ao excluir: " + e.getMessage());
            }
        }
    }

    private void limparCamposOrg() {
        txtOrgNome.clear();
        txtOrgCnpj.clear();
        txtOrgMunicipio.clear();
        txtOrgAssociados.clear();
        txtOrgFaturamento.clear();
        txtOrgProdutos.clear();
        txtOrgCanais.clear();
        chkOrgCaf.setSelected(false);
        chkOrgPaaPnae.setSelected(false);
        txtOrgDocumentos.clear();
        txtOrgInfra.clear();
        txtOrgDificuldades.clear();
    }

    // ==========================================
    //           CRUD PRODUTOR RURAL
    // ==========================================
    private void carregarProdutorParaEdicao(Produtor p) {
        produtorEmEdicao = p;
        txtProdNome.setText(p.getNome());
        txtProdCpf.setText(p.getCpf());
        dpDataNascimento.setValue(p.getDataNascimento());
        cbGenero.setValue(p.getGenero());
        txtProdComunidade.setText(p.getComunidade());
        txtProdArea.setText(p.getAreaPropriedade() != null ? String.valueOf(p.getAreaPropriedade()) : "");
        txtProdCulturas.setText(p.getPrincipaisCulturas());

        String destino = p.getDestinoProducao() != null ? p.getDestinoProducao() : "";
        if (chkDestinoCoop != null) chkDestinoCoop.setSelected(destino.contains("COOPERATIVA"));
        if (chkDestinoVendaDireta != null) chkDestinoVendaDireta.setSelected(destino.contains("VENDA_DIRETA"));
        if (chkDestinoConsumo != null) chkDestinoConsumo.setSelected(destino.contains("CONSUMO_PROPRIO"));

        txtProdConsultor.setText(p.getConsultorBloqueio() != null ? p.getConsultorBloqueio() : "LIVRE");
        chkProdCaf.setSelected(p.isPossuiCaf());

        btnSalvarProd.setText("Atualizar Produtor");

        String consultorLogado = SessaoUsuario.getNomeConsultor();
        String bloqueador = p.getConsultorBloqueio();

        if (bloqueador == null || bloqueador.isBlank()) {
            btnAlternarBloqueio.setText("Bloquear Registro");
            btnAlternarBloqueio.setDisable(false);
            btnSalvarProd.setDisable(false);
            btnExcluirProd.setDisable(false);
            lblProdMsg.setStyle("-fx-text-fill: #2e7d32;");
            lblProdMsg.setText("Registro livre para edição.");
        } else if (bloqueador.equalsIgnoreCase(consultorLogado)) {
            btnAlternarBloqueio.setText("Desbloquear Registro");
            btnAlternarBloqueio.setDisable(false);
            btnSalvarProd.setDisable(false);
            btnExcluirProd.setDisable(false);
            lblProdMsg.setStyle("-fx-text-fill: #1976D2;");
            lblProdMsg.setText("Você possui o bloqueio exclusivo deste produtor.");
        } else {
            btnAlternarBloqueio.setText("Bloqueado");
            btnAlternarBloqueio.setDisable(true);
            btnSalvarProd.setDisable(true);
            btnExcluirProd.setDisable(true);
            lblProdMsg.setStyle("-fx-text-fill: #d32f2f;");
            lblProdMsg.setText("Acesso restrito: Em atendimento por " + bloqueador);
        }
    }

    @FXML
    public void alternarBloqueioProdutor() {
        if (produtorEmEdicao == null || produtorEmEdicao.getId() == null) {
            return;
        }

        String consultorLogado = SessaoUsuario.getNomeConsultor();
        String bloqueador = produtorEmEdicao.getConsultorBloqueio();

        try {
            if (bloqueador == null || bloqueador.isBlank()) {
                prodRepo.definirBloqueio(produtorEmEdicao.getId(), consultorLogado);
                produtorEmEdicao.setConsultorBloqueio(consultorLogado);
                lblProdMsg.setStyle("-fx-text-fill: #1976D2;");
                lblProdMsg.setText("Produtor bloqueado com sucesso para você.");
            } else if (bloqueador.equalsIgnoreCase(consultorLogado)) {
                prodRepo.definirBloqueio(produtorEmEdicao.getId(), null);
                produtorEmEdicao.setConsultorBloqueio(null);
                lblProdMsg.setStyle("-fx-text-fill: #2e7d32;");
                lblProdMsg.setText("Produtor desbloqueado. Agora está livre para a equipe.");
            } else {
                Alert alerta = new Alert(Alert.AlertType.WARNING);
                alerta.setTitle("Acesso Restrito");
                alerta.setHeaderText("Operação não permitida");
                alerta.setContentText("Somente o consultor " + bloqueador + " pode liberar este registro.");
                alerta.showAndWait();
                return;
            }

            carregarProdutorParaEdicao(produtorEmEdicao);
            recarregarListaProdutoresTabela();
        } catch (Exception e) {
            lblProdMsg.setStyle("-fx-text-fill: #d32f2f;");
            lblProdMsg.setText("Erro ao alterar bloqueio: " + e.getMessage());
        }
    }

    @FXML
    public void filtrarMeusBloqueios() {
        if (chkMeusBloqueios.isSelected()) {
            try {
                listaProdutores.clear();
                listaProdutores.addAll(prodRepo.listarBloqueadosPorConsultor(SessaoUsuario.getNomeConsultor()));
                lblProdMsg.setStyle("-fx-text-fill: #1976D2;");
                lblProdMsg.setText("Exibindo apenas os produtores bloqueados por você.");
            } catch (Exception e) {
                System.err.println("Erro ao listar bloqueados: " + e.getMessage());
            }
        } else {
            Organizacao org = cbOrganizacaoAtiva.getValue();
            if (org != null) {
                carregarProdutores(org.getId());
            } else {
                listaProdutores.clear();
            }
            lblProdMsg.setText("");
        }
    }

    private void recarregarListaProdutoresTabela() {
        if (chkMeusBloqueios.isSelected()) {
            filtrarMeusBloqueios();
        } else {
            Organizacao org = cbOrganizacaoAtiva.getValue();
            if (org != null) {
                carregarProdutores(org.getId());
            }
        }
    }

    @FXML
    public void salvarOuAtualizarProdutor() {
        Organizacao orgSelecionada = cbOrganizacaoAtiva.getValue();
        if (orgSelecionada == null || orgSelecionada.getId() == null) {
            lblProdMsg.setStyle("-fx-text-fill: #d32f2f;");
            lblProdMsg.setText("Selecione uma Cooperativa antes de salvar o produtor!");
            return;
        }

        if (txtProdNome.getText().trim().isEmpty()) {
            lblProdMsg.setStyle("-fx-text-fill: #d32f2f;");
            lblProdMsg.setText("Informe o nome do produtor!");
            return;
        }

        String cpfInformado = txtProdCpf.getText().trim();

        try {
            // Validação de unicidade do CPF dentro desta mesma organização
            if (!cpfInformado.isEmpty()) {
                Integer idAtual = (produtorEmEdicao != null) ? produtorEmEdicao.getId() : null;
                if (prodRepo.existeCpfNaOrganizacao(cpfInformado, orgSelecionada.getId(), idAtual)) {
                    lblProdMsg.setStyle("-fx-text-fill: #d32f2f;");
                    lblProdMsg.setText("Aviso: Já existe um produtor com este CPF nesta cooperativa!");

                    Alert alerta = new Alert(Alert.AlertType.WARNING);
                    alerta.setTitle("CPF Duplicado na Cooperativa");
                    alerta.setHeaderText("Produtor já cadastrado");
                    alerta.setContentText("O CPF informado (" + cpfInformado + ") já está registrado nesta organização. O mesmo CPF só pode ser cadastrado em cooperativas diferentes.");
                    alerta.showAndWait();
                    return;
                }
            }

            Produtor p = (produtorEmEdicao != null) ? produtorEmEdicao : new Produtor();
            p.setOrganizacaoId(orgSelecionada.getId());
            p.setNome(txtProdNome.getText().trim());
            p.setCpf(cpfInformado);
            p.setDataNascimento(dpDataNascimento.getValue());
            p.setGenero(cbGenero.getValue());
            p.setComunidade(txtProdComunidade.getText().trim());
            p.setPossuiCaf(chkProdCaf.isSelected());
            p.setPrincipaisCulturas(txtProdCulturas.getText().trim());

            // Validação: deve selecionar pelo menos um destino
            List<String> destinosSelecionados = new ArrayList<>();
            if (chkDestinoCoop != null && chkDestinoCoop.isSelected()) {
                destinosSelecionados.add("COOPERATIVA");
            }
            if (chkDestinoVendaDireta != null && chkDestinoVendaDireta.isSelected()) {
                destinosSelecionados.add("VENDA_DIRETA");
            }
            if (chkDestinoConsumo != null && chkDestinoConsumo.isSelected()) {
                destinosSelecionados.add("CONSUMO_PROPRIO");
            }

            if (destinosSelecionados.isEmpty()) {
                lblProdMsg.setStyle("-fx-text-fill: #d32f2f;");
                lblProdMsg.setText("Selecione ao menos um Destino da Produção!");
                return;
            }

            String destinoFinal = String.join(", ", destinosSelecionados);
            p.setDestinoProducao(destinoFinal);

            if (!txtProdArea.getText().trim().isEmpty()) {
                p.setAreaPropriedade(Double.parseDouble(txtProdArea.getText().trim()));
            } else {
                p.setAreaPropriedade(null);
            }

            if (produtorEmEdicao == null) {
                // Novos produtores nascem livres para a equipe
                p.setConsultorBloqueio(null);
                prodRepo.salvar(p);
                lblProdMsg.setStyle("-fx-text-fill: #2e7d32;");
                lblProdMsg.setText("Produtor cadastrado com sucesso e livre para edição!");
            } else {
                prodRepo.atualizar(p);
                lblProdMsg.setStyle("-fx-text-fill: #2e7d32;");
                lblProdMsg.setText("Produtor atualizado com sucesso!");
            }

            limparCamposProdutor();
            recarregarListaProdutoresTabela();
        } catch (Exception e) {
            lblProdMsg.setStyle("-fx-text-fill: #d32f2f;");
            lblProdMsg.setText("Erro ao salvar produtor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void excluirProdutor() {
        if (produtorEmEdicao == null || produtorEmEdicao.getId() == null) {
            return;
        }

        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar Exclusão");
        alerta.setHeaderText("Excluir Produtor: " + produtorEmEdicao.getNome());
        alerta.setContentText("Deseja realmente excluir este produtor?");

        Optional<ButtonType> resultado = alerta.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                prodRepo.excluir(produtorEmEdicao.getId());
                limparCamposProdutor();
                recarregarListaProdutoresTabela();
                lblProdMsg.setStyle("-fx-text-fill: #2e7d32;");
                lblProdMsg.setText("Produtor excluído com sucesso!");
            } catch (Exception e) {
                lblProdMsg.setStyle("-fx-text-fill: #d32f2f;");
                lblProdMsg.setText("Erro ao excluir produtor: " + e.getMessage());
            }
        }
    }

    @FXML
    public void limparCamposProdutor() {
        produtorEmEdicao = null;
        tblProdutores.getSelectionModel().clearSelection();

        txtProdNome.clear();
        txtProdCpf.clear();
        dpDataNascimento.setValue(null);
        cbGenero.getSelectionModel().clearSelection();
        txtProdComunidade.clear();
        txtProdArea.clear();
        txtProdCulturas.clear();
        chkProdCaf.setSelected(false);
        txtProdConsultor.setText("LIVRE");

        if (chkDestinoCoop != null) chkDestinoCoop.setSelected(true);
        if (chkDestinoVendaDireta != null) chkDestinoVendaDireta.setSelected(false);
        if (chkDestinoConsumo != null) chkDestinoConsumo.setSelected(false);

        btnSalvarProd.setText("+ Adicionar Produtor");
        btnSalvarProd.setDisable(false);
        btnExcluirProd.setDisable(true);
        btnAlternarBloqueio.setText("Bloquear Registro");
        btnAlternarBloqueio.setDisable(true);
    }

    private void carregarProdutores(int organizacaoId) {
        try {
            listaProdutores.clear();
            listaProdutores.addAll(prodRepo.listarPorOrganizacao(organizacaoId));
        } catch (Exception e) {
            System.err.println("Erro ao listar produtores da organização: " + e.getMessage());
        }
    }
}