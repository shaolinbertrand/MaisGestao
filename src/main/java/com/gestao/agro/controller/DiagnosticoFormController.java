package com.gestao.agro.controller;

import com.gestao.agro.model.*;
import com.gestao.agro.repository.DiagnosticoRepository;
import com.gestao.agro.repository.OrganizacaoRepository;
import com.gestao.agro.repository.ProdutorRepository;
import com.gestao.agro.util.SessaoUsuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.*;

public class DiagnosticoFormController {

    // --- Cabeçalho ---
    @FXML private RadioButton rbTipoOrg;
    @FXML private RadioButton rbTipoProd;
    @FXML private ToggleGroup grpTipoAlvo;
    @FXML private Label lblEntidadeAlvo;
    @FXML private ComboBox<Object> cbEntidadeAlvo;
    @FXML private ComboBox<String> cbVersaoCiclo;
    @FXML private DatePicker dpDataAplicacao;
    @FXML private TextField txtConsultor;

    // --- Navegação por Dimensão (Aba 1) ---
    @FXML private ComboBox<DimensaoDiagnostico> cbDimensaoAtiva;
    @FXML private VBox vbQuestoesContainer;
    @FXML private Label lblStatusMensagem;

    // --- Dashboard Analítico IMO-AF (Aba 2) ---
    @FXML private Label lblMediaGeral;
    @FXML private Label lblNivelMaturidade;
    @FXML private BarChart<String, Number> graficoMaturidade;

    private final OrganizacaoRepository orgRepo = new OrganizacaoRepository();
    private final ProdutorRepository prodRepo = new ProdutorRepository();
    private final DiagnosticoRepository diagRepo = new DiagnosticoRepository();

    // Armazenamento em memória das respostas (Chave: subdimensao/pergunta)
    private final Map<String, DiagnosticoResposta> respostasEmMemoria = new HashMap<>();

    private DiagnosticoVersao versaoAtual = null;

    @FXML
    public void initialize() {
        txtConsultor.setText(SessaoUsuario.getNomeConsultor());
        dpDataAplicacao.setValue(LocalDate.now());

        configurarNavegacaoDimensoes();
        configurarSeletoresAlvo();

        alternarTipoAlvo();
    }

    private void configurarNavegacaoDimensoes() {
        cbDimensaoAtiva.setItems(FXCollections.observableArrayList(DimensaoDiagnostico.values()));
        cbDimensaoAtiva.setConverter(new StringConverter<>() {
            @Override
            public String toString(DimensaoDiagnostico dim) {
                return (dim != null) ? dim.getTitulo() : "";
            }

            @Override
            public DimensaoDiagnostico fromString(String string) {
                return null;
            }
        });

        cbDimensaoAtiva.getSelectionModel().selectedItemProperty().addListener((obs, antigo, selecionada) -> {
            if (selecionada != null) {
                renderizarQuestoesDimensao(selecionada);
            }
        });

        cbDimensaoAtiva.getSelectionModel().selectFirst();
    }

    private void configurarSeletoresAlvo() {
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
            carregarVersoesEntidade(selecionado);
        });

        cbVersaoCiclo.getSelectionModel().selectedItemProperty().addListener((obs, antigo, selecionado) -> {
            carregarDiagnosticoPorCiclo(selecionado);
        });
    }

    private void alternarTipoAlvo() {
        cbEntidadeAlvo.getItems().clear();
        cbVersaoCiclo.getItems().clear();
        respostasEmMemoria.clear();
        versaoAtual = null;
        limparDashboardAnalitico();

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
            lblStatusMensagem.setStyle("-fx-text-fill: #d32f2f;");
            lblStatusMensagem.setText("Erro ao carregar lista de entidades: " + e.getMessage());
        }
    }

    private void carregarVersoesEntidade(Object entidade) {
        cbVersaoCiclo.getItems().clear();
        respostasEmMemoria.clear();
        versaoAtual = null;
        limparDashboardAnalitico();

        if (entidade == null) return;

        try {
            List<DiagnosticoVersao> versoes;
            if (entidade instanceof Organizacao org) {
                versoes = diagRepo.listarVersoesPorOrganizacao(org.getId());
            } else {
                Produtor prod = (Produtor) entidade;
                versoes = diagRepo.listarVersoesPorProdutor(prod.getId());
            }

            ObservableList<String> itensVersao = FXCollections.observableArrayList();
            int proximoCiclo = 1;

            if (!versoes.isEmpty()) {
                proximoCiclo = versoes.get(0).getNumeroVersao() + 1;
                for (DiagnosticoVersao v : versoes) {
                    itensVersao.add("Ciclo " + v.getNumeroVersao() + " (" + v.getStatus() + " - " + v.getDataAplicacao() + ")");
                }
            }

            itensVersao.add(0, "+ Novo Ciclo (" + proximoCiclo + ")");
            cbVersaoCiclo.setItems(itensVersao);
            cbVersaoCiclo.getSelectionModel().selectFirst();
        } catch (Exception e) {
            lblStatusMensagem.setStyle("-fx-text-fill: #d32f2f;");
            lblStatusMensagem.setText("Erro ao carregar ciclos de diagnóstico: " + e.getMessage());
        }
    }

    private void carregarDiagnosticoPorCiclo(String cicloStr) {
        respostasEmMemoria.clear();
        if (cicloStr == null || cicloStr.startsWith("+ Novo Ciclo")) {
            versaoAtual = null;
            limparDashboardAnalitico();
            lblStatusMensagem.setStyle("-fx-text-fill: #1e3a2f;");
            lblStatusMensagem.setText("Iniciando novo ciclo de diagnóstico IMO-AF.");
            renderizarQuestoesDimensao(cbDimensaoAtiva.getValue());
            return;
        }

        try {
            int numeroVersao = Integer.parseInt(cicloStr.substring(6, cicloStr.indexOf('(')).trim());
            Object entidade = cbEntidadeAlvo.getValue();

            List<DiagnosticoVersao> versoes;
            if (entidade instanceof Organizacao org) {
                versoes = diagRepo.listarVersoesPorOrganizacao(org.getId());
            } else {
                versoes = diagRepo.listarVersoesPorProdutor(((Produtor) entidade).getId());
            }

            for (DiagnosticoVersao v : versoes) {
                if (v.getNumeroVersao() == numeroVersao) {
                    versaoAtual = v;
                    dpDataAplicacao.setValue(v.getDataAplicacao());
                    txtConsultor.setText(v.getConsultorResponsavel());

                    List<DiagnosticoResposta> respostas = diagRepo.listarRespostasPorVersao(v.getId());
                    for (DiagnosticoResposta r : respostas) {
                        respostasEmMemoria.put(r.getSubdimensao(), r);
                    }
                    break;
                }
            }

            lblStatusMensagem.setStyle("-fx-text-fill: #1976D2;");
            lblStatusMensagem.setText("Ciclo carregado com " + respostasEmMemoria.size() + " respostas registradas.");
            renderizarQuestoesDimensao(cbDimensaoAtiva.getValue());
            atualizarGraficoMaturidade();
        } catch (Exception e) {
            lblStatusMensagem.setStyle("-fx-text-fill: #d32f2f;");
            lblStatusMensagem.setText("Erro ao carregar dados do ciclo: " + e.getMessage());
        }
    }

    private void renderizarQuestoesDimensao(DimensaoDiagnostico dimensao) {
        vbQuestoesContainer.getChildren().clear();
        if (dimensao == null) return;

        int numQuestao = 1;
        for (String pergunta : dimensao.getPerguntas()) {
            VBox cardPergunta = new VBox(8.0);
            cardPergunta.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 6; -fx-border-color: #D9DFDC; -fx-border-radius: 6;");

            Label lblPergunta = new Label(numQuestao + ". " + pergunta);
            lblPergunta.setWrapText(true);
            lblPergunta.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1E3A2F;");

            // Escala Oficial IMO-AF (1 a 5 + NA)
            ToggleGroup grpLikert = new ToggleGroup();
            VBox vbOpcoes = new VBox(6.0);
            vbOpcoes.setStyle("-fx-padding: 4 0;");

            String[] rotulosEscala = {
                "1 - Não existe / não é realizado",
                "2 - Existe de maneira informal ou muito incipiente",
                "3 - Existe parcialmente e é realizado com alguma regularidade",
                "4 - Está estruturado e é realizado regularmente",
                "5 - Está consolidado, é monitorado e continuamente aperfeiçoado",
                "NA - Não se aplica"
            };

            DiagnosticoResposta respExistente = respostasEmMemoria.get(pergunta);
            Integer pontuacaoAtual = (respExistente != null) ? respExistente.getPontuacao() : null;

            for (int i = 1; i <= 5; i++) {
                RadioButton rb = new RadioButton(rotulosEscala[i - 1]);
                rb.setToggleGroup(grpLikert);
                rb.setUserData(i);
                if (pontuacaoAtual != null && pontuacaoAtual == i) {
                    rb.setSelected(true);
                }
                vbOpcoes.getChildren().add(rb);
            }

            // Opção NA representada por pontuacao = 0
            RadioButton rbNA = new RadioButton(rotulosEscala[5]);
            rbNA.setToggleGroup(grpLikert);
            rbNA.setUserData(0);
            if (pontuacaoAtual != null && pontuacaoAtual == 0) {
                rbNA.setSelected(true);
            }
            vbOpcoes.getChildren().add(rbNA);

            // Campos de observação e recomendação
            TextField txtEvidencias = new TextField();
            txtEvidencias.setPromptText("Evidências observadas / Justificativa da situação...");
            if (respExistente != null && respExistente.getObservacoesEvidencias() != null) {
                txtEvidencias.setText(respExistente.getObservacoesEvidencias());
            }

            TextField txtPlanoAcao = new TextField();
            txtPlanoAcao.setPromptText("Recomendação ou plano de ação sugerido...");
            if (respExistente != null && respExistente.getPlanoAcaoRecomendado() != null) {
                txtPlanoAcao.setText(respExistente.getPlanoAcaoRecomendado());
            }

            // Atualização contínua na memória
            Runnable salvarNaMemoria = () -> {
                Toggle selecionado = grpLikert.getSelectedToggle();
                int pontuacao = (selecionado != null) ? (int) selecionado.getUserData() : 0;
                DiagnosticoResposta r = respostasEmMemoria.getOrDefault(pergunta, new DiagnosticoResposta());
                r.setDimensao(dimensao.getTitulo());
                r.setSubdimensao(pergunta);
                r.setPontuacao(pontuacao);
                r.setObservacoesEvidencias(txtEvidencias.getText());
                r.setPlanoAcaoRecomendado(txtPlanoAcao.getText());
                respostasEmMemoria.put(pergunta, r);
            };

            grpLikert.selectedToggleProperty().addListener((obs, o, n) -> salvarNaMemoria.run());
            txtEvidencias.textProperty().addListener((obs, o, n) -> salvarNaMemoria.run());
            txtPlanoAcao.textProperty().addListener((obs, o, n) -> salvarNaMemoria.run());

            cardPergunta.getChildren().addAll(lblPergunta, vbOpcoes, txtEvidencias, txtPlanoAcao);
            vbQuestoesContainer.getChildren().add(cardPergunta);
            numQuestao++;
        }
    }

    @FXML
    public void dimensaoAnterior() {
        int idx = cbDimensaoAtiva.getSelectionModel().getSelectedIndex();
        if (idx > 0) {
            cbDimensaoAtiva.getSelectionModel().select(idx - 1);
        }
    }

    @FXML
    public void proximaDimensao() {
        int idx = cbDimensaoAtiva.getSelectionModel().getSelectedIndex();
        if (idx < cbDimensaoAtiva.getItems().size() - 1) {
            cbDimensaoAtiva.getSelectionModel().select(idx + 1);
        }
    }

    @FXML
    public void salvarRascunho() {
        executarSalvamento("EM_ANDAMENTO");
    }

    @FXML
    public void concluirDiagnostico() {
        executarSalvamento("CONCLUIDO");
    }

    private void executarSalvamento(String status) {
        Object entidade = cbEntidadeAlvo.getValue();
        if (entidade == null) {
            lblStatusMensagem.setStyle("-fx-text-fill: #d32f2f;");
            lblStatusMensagem.setText("Selecione uma Cooperativa ou Produtor!");
            return;
        }

        try {
            if (versaoAtual == null || versaoAtual.getId() == null) {
                versaoAtual = new DiagnosticoVersao();
                if (entidade instanceof Organizacao org) {
                    versaoAtual.setOrganizacaoId(org.getId());
                    versaoAtual.setProdutorId(null);
                    List<DiagnosticoVersao> anteriores = diagRepo.listarVersoesPorOrganizacao(org.getId());
                    versaoAtual.setNumeroVersao(anteriores.isEmpty() ? 1 : anteriores.get(0).getNumeroVersao() + 1);
                } else {
                    Produtor prod = (Produtor) entidade;
                    versaoAtual.setProdutorId(prod.getId());
                    versaoAtual.setOrganizacaoId(null);
                    List<DiagnosticoVersao> anteriores = diagRepo.listarVersoesPorProdutor(prod.getId());
                    versaoAtual.setNumeroVersao(anteriores.isEmpty() ? 1 : anteriores.get(0).getNumeroVersao() + 1);
                }
                versaoAtual.setConsultorResponsavel(SessaoUsuario.getNomeConsultor());
                versaoAtual.setDataAplicacao(dpDataAplicacao.getValue() != null ? dpDataAplicacao.getValue() : LocalDate.now());
                versaoAtual.setStatus(status);

                versaoAtual = diagRepo.salvarVersao(versaoAtual);
            } else {
                versaoAtual.setStatus(status);
                diagRepo.atualizarStatusVersao(versaoAtual.getId(), status);
            }

            if (versaoAtual == null || versaoAtual.getId() == null) {
                throw new IllegalStateException("Falha ao registrar versão no banco de dados.");
            }

            List<DiagnosticoResposta> listaRespostas = new ArrayList<>(respostasEmMemoria.values());
            for (DiagnosticoResposta r : listaRespostas) {
                r.setDiagnosticoVersaoId(versaoAtual.getId());
            }

            diagRepo.salvarRespostas(versaoAtual.getId(), listaRespostas);

            lblStatusMensagem.setStyle("-fx-text-fill: #2e7d32;");
            lblStatusMensagem.setText("Diagnóstico salvo com sucesso! Status: " + status);

            atualizarGraficoMaturidade();
            carregarVersoesEntidade(entidade);
        } catch (Exception e) {
            lblStatusMensagem.setStyle("-fx-text-fill: #d32f2f;");
            lblStatusMensagem.setText("Erro ao salvar diagnóstico: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==========================================
    //       ANÁLISE E DASHBOARD DE MATURIDADE
    // ==========================================

    @FXML
    public void atualizarGraficoMaturidade() {
        if (graficoMaturidade == null) return;

        DiagnosticoVersao analise = (versaoAtual != null) ? versaoAtual : new DiagnosticoVersao();
        analise.getMediasPorDimensao().clear();

        // 1. Tenta carregar do banco se houver versão persistida
        if (analise.getId() != null) {
            try {
                diagRepo.calcularMediasPorDimensao(analise);
            } catch (Exception e) {
                System.err.println("Erro ao calcular médias pelo banco: " + e.getMessage());
            }
        }

        // 2. Calcula as médias por dimensão garantindo estritamente o Enum oficial
        // e considerando apenas notas válidas de 1 a 5 (descarta 0 / NA)
        for (DimensaoDiagnostico dim : DimensaoDiagnostico.values()) {
            // Se já não veio do banco com nota calculada
            if (!analise.getMediasPorDimensao().containsKey(dim.getTitulo())) {
                List<Integer> notasValidas = new ArrayList<>();
                for (String pergunta : dim.getPerguntas()) {
                    DiagnosticoResposta resp = respostasEmMemoria.get(pergunta);
                    if (resp != null && resp.getPontuacao() > 0) {
                        notasValidas.add(resp.getPontuacao());
                    }
                }

                if (!notasValidas.isEmpty()) {
                    double mediaDim = notasValidas.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                    analise.adicionarMediaDimensao(dim.getTitulo(), mediaDim);
                } else {
                    analise.adicionarMediaDimensao(dim.getTitulo(), 0.0);
                }
            }
        }

        // 3. Atualizar Indicadores (Cards)
        double imoAfGeral = analise.getMediaGeral();
        if (lblMediaGeral != null) {
            lblMediaGeral.setText(String.format(Locale.US, "%.2f", imoAfGeral));
        }

        if (lblNivelMaturidade != null) {
            if (imoAfGeral > 0.0) {
                lblNivelMaturidade.setText(analise.getNivelMaturidade() + " — " + analise.getDescricaoMaturidade());
                lblNivelMaturidade.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + analise.getCorMaturidadeHex() + ";");
            } else {
                lblNivelMaturidade.setText("Aguardando Avaliação");
                lblNivelMaturidade.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #757575;");
            }
        }

        // 4. Montar Série do BarChart para as 15 Dimensões
        graficoMaturidade.getData().clear();
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Média da Dimensão (MD_i)");

        for (DimensaoDiagnostico dim : DimensaoDiagnostico.values()) {
            Double nota = analise.getMediasPorDimensao().get(dim.getTitulo());
            double valorGrafico = (nota != null) ? nota : 0.0;

            String rotuloCurto = dim.getTitulo();
            if (rotuloCurto.contains(".")) {
                rotuloCurto = "D" + rotuloCurto.substring(0, rotuloCurto.indexOf('.')).trim();
            }

            serie.getData().add(new XYChart.Data<>(rotuloCurto, valorGrafico));
        }

        graficoMaturidade.getData().add(serie);
    }

    private void limparDashboardAnalitico() {
        if (lblMediaGeral != null) lblMediaGeral.setText("0.0");
        if (lblNivelMaturidade != null) {
            lblNivelMaturidade.setText("Aguardando Avaliação");
            lblNivelMaturidade.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #757575;");
        }
        if (graficoMaturidade != null) {
            graficoMaturidade.getData().clear();
        }
    }
}