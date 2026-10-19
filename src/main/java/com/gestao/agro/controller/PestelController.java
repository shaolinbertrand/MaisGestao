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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @FXML private ComboBox<String> cbFatorSugerido;
    @FXML private TextField txtFatorManual;
    
    @FXML private RadioButton rbOportunidade;
    @FXML private RadioButton rbAmeaca;
    @FXML private ToggleGroup grpTipoFator;
    @FXML private Slider sldImpacto;
    @FXML private Label lblValorImpacto;
    @FXML private Slider sldProbabilidade;
    @FXML private Label lblValorProbabilidade;
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

    private final Map<String, List<String>> sugestoesPestel = new HashMap<>();
    private FatorPestel fatorEmEdicao = null;

    @FXML
    public void initialize() {
        txtConsultor.setText(SessaoUsuario.getNomeConsultor());

        carregarSugestoesDoCliente();
        configurarDimensoesPestel();
        configurarSliders();
        configurarSeletoresEntidade();
        configurarTabelas();

        alternarTipoAlvo();
    }

    private void carregarSugestoesDoCliente() {
        sugestoesPestel.put("POLÍTICO", Arrays.asList(
            "Pouco acesso a políticas públicas de apoio à agricultura familiar",
            "Descontinuidade de programas e ações governamentais",
            "Dificuldade de acesso ao PAA e ao PNAE",
            "Pouco apoio dos órgãos públicos às demandas das organizações",
            "Falta de articulação entre instituições que atendem à agricultura familiar",
            "Mudanças nas prioridades governamentais que prejudicam o setor",
            "Não identifica influência política relevante no momento",
            "Não sabe avaliar",
            "Outro fator"
        ));

        sugestoesPestel.put("ECONÔMICO", Arrays.asList(
            "Alto custo de insumos e materiais de produção",
            "Frete caro e dificuldade de transporte da produção",
            "Poucas opções de compradores e canais de comercialização",
            "Preços baixos pagos aos agricultores",
            "Dificuldade de acesso a crédito e financiamento",
            "Demanda insuficiente ou irregular pelos produtos",
            "Concorrência com produtos de outras regiões",
            "Não identifica influência econômica relevante no momento",
            "Não sabe avaliar",
            "Outro fator"
        ));

        sugestoesPestel.put("SOCIAL", Arrays.asList(
            "Pouca participação dos jovens na agricultura familiar",
            "Saída de jovens das comunidades rurais",
            "Dificuldade de encontrar trabalhadores para a produção",
            "Baixo reconhecimento dos produtos e saberes locais",
            "Desvalorização do trabalho das mulheres agricultoras",
            "Mudanças nos hábitos de consumo que reduzem a procura por produtos locais",
            "Dificuldade de acesso a oportunidades de formação no meio rural",
            "Não identifica influência sociocultural relevante no momento",
            "Não sabe avaliar",
            "Outro fator"
        ));

        sugestoesPestel.put("TECNOLÓGICO", Arrays.asList(
            "Falta ou instabilidade de internet e sinal de telefone nas áreas rurais",
            "Pouca oferta de assistência técnica e extensão rural",
            "Dificuldade de acesso a máquinas e equipamentos adequados",
            "Pouca disponibilidade de tecnologias de beneficiamento e conservação",
            "Dificuldade de acesso a sementes, mudas e insumos adequados",
            "Pouca oferta de tecnologias adaptadas às condições locais",
            "Dificuldade de acesso a ferramentas digitais para comercialização",
            "Não identifica limitação tecnológica externa relevante no momento",
            "Não sabe avaliar",
            "Outro fator"
        ));

        sugestoesPestel.put("ECOLÓGICO / AMBIENTAL", Arrays.asList(
            "Chuvas irregulares e períodos de seca",
            "Enchentes, alagamentos ou dificuldades de acesso no inverno",
            "Queimadas e fumaça que prejudicam a produção",
            "Dificuldade de acesso à água para produção",
            "Pragas e doenças que afetam as plantações ou criações",
            "Degradação do solo e perda de recursos naturais",
            "Mudanças no clima que alteram o calendário de plantio e colheita",
            "Não identifica influência ambiental relevante no momento",
            "Não sabe avaliar",
            "Outro fator"
        ));

        sugestoesPestel.put("LEGAL / REGULATÓRIO", Arrays.asList(
            "Dificuldade de obter ou atualizar o CAF",
            "Exigências sanitárias difíceis de atender",
            "Dificuldade de adequar produtos às regras de rotulagem e embalagem",
            "Exigências documentais para vender ao poder público",
            "Dificuldades relacionadas à regularização fundiária ou ambiental",
            "Exigências legais que dificultam a formalização da organização",
            "Falta de orientação acessível sobre normas e exigências aplicáveis",
            "Não identifica dificuldade legal externa relevante no momento",
            "Não sabe avaliar",
            "Outro fator"
        ));
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

        // Atualiza as sugestões sempre que a dimensão PESTEL mudar
        cbDimensaoPestel.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                List<String> sugestoes = sugestoesPestel.get(novo.toUpperCase());
                cbFatorSugerido.setItems(FXCollections.observableArrayList(sugestoes));
                cbFatorSugerido.getSelectionModel().selectFirst();
            }
        });

        // Oculta inicialmente o campo manual
        txtFatorManual.setVisible(false);
        txtFatorManual.setManaged(false);

        // Controla a visibilidade do campo manual se escolher "Outro fator"
        cbFatorSugerido.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if ("Outro fator".equals(novo)) {
                txtFatorManual.setVisible(true);
                txtFatorManual.setManaged(true);
                txtFatorManual.requestFocus();
            } else {
                txtFatorManual.setVisible(false);
                txtFatorManual.setManaged(false);
                txtFatorManual.clear();
            }
        });

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
        colOpDimensao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDimensao()));
        colOpDescricao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescricao()));
        colOpCriticidade.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCriticidade()).asObject());
        colOpAcao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAcaoSugerida()));
        tblOportunidades.setItems(listaOportunidades);

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

        // Pega a descrição do ComboBox ou do campo manual
        String descricao = "";
        if ("Outro fator".equals(cbFatorSugerido.getValue())) {
            descricao = txtFatorManual.getText();
        } else {
            descricao = cbFatorSugerido.getValue();
        }

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
        
        // Define a dimensão (isto irá disparar o listener que preenche o cbFatorSugerido)
        cbDimensaoPestel.setValue(f.getDimensao());
        
        // Verifica se a descrição do banco está na lista de sugestões gerada
        if (cbFatorSugerido.getItems().contains(f.getDescricao())) {
            cbFatorSugerido.setValue(f.getDescricao());
        } else {
            // Se foi algo digitado manualmente antes, seleciona "Outro fator" e preenche o manual
            cbFatorSugerido.setValue("Outro fator");
            txtFatorManual.setText(f.getDescricao());
        }

        if ("OPORTUNIDADE".equalsIgnoreCase(f.getTipo())) {
            rbOportunidade.setSelected(true);
        } else {
            rbAmeaca.setSelected(true);
        }
        sldImpacto.setValue(f.getImpacto());
        sldProbabilidade.setValue(f.getProbabilidade());
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
        if (!cbFatorSugerido.getItems().isEmpty()) {
            cbFatorSugerido.getSelectionModel().selectFirst();
        }
        txtFatorManual.clear();
        txtFatorManual.setVisible(false);
        txtFatorManual.setManaged(false);
        
        rbOportunidade.setSelected(true);
        sldImpacto.setValue(3.0);
        sldProbabilidade.setValue(3.0);
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