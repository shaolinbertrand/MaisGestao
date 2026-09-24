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

    // --- Dashboard Analítico e Maturidade (Aba 2) ---
    @FXML private Label lblMediaGeral;
    @FXML private Label lblNivelMaturidade;
    @FXML private BarChart<String, Number> graficoMaturidade;

    private final OrganizacaoRepository orgRepo = new OrganizacaoRepository();
    private final ProdutorRepository prodRepo = new ProdutorRepository();
    private final DiagnosticoRepository diagRepo = new DiagnosticoRepository();

    // Respostas mantidas em memória durante a navegação entre dimensões
    // Chave: subdimensao (pergunta)
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
            lblStatusMensagem.setText("Iniciando novo ciclo de diagnóstico.");
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

        for (String pergunta : dimensao.getPerguntas()) {
            VBox cardPergunta = new VBox(8.0);
            cardPergunta.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 6; -fx-border-color: #D9DFDC; -fx-border-radius: 6;");

            Label lblPergunta = new Label(pergunta);
            lblPergunta.setWrapText(true);
            lblPergunta.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1E3A2F;");

            // Escala Likert de 1 a 5
            ToggleGroup grpLikert = new ToggleGroup();
            HBox hbEscala = new HBox(12.0);
            hbEscala.setStyle("-fx-padding: 4 0;");

            String[] rotulosEscala = {
                "1 - Crítico / Inexistente",
                "2 - Inicial / Precário",
                "3 - Regular / Em Desenvolvimento",
                "4 - Bom / Estruturado",
                "5 - Excelente / Consolidado"
            };

            DiagnosticoResposta respExistente = respostasEmMemoria.get(pergunta);
            int pontuacaoAtual = (respExistente != null) ? respExistente.getPontuacao() : 0;

            for (int i = 1; i <= 5; i++) {
                RadioButton rb = new RadioButton(rotulosEscala[i - 1]);
                rb.setToggleGroup(grpLikert);
                rb.setUserData(i);
                if (i == pontuacaoAtual) {
                    rb.setSelected(true);
                }
                hbEscala.getChildren().add(rb);
            }

            // Campos de evidências e plano de ação
            TextField txtEvidencias = new TextField();
            txtEvidencias.setPromptText("Evidências observadas / Justificativa da pontuação...");
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

            cardPergunta.getChildren().addAll(lblPergunta, hbEscala, txtEvidencias, txtPlanoAcao);
            vbQuestoesContainer.getChildren().add(cardPergunta);
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

        // Objeto de cálculo da versão
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

        // 2. Fallback / Cálculo em tempo real com o que estiver na memória
        if (analise.getMediasPorDimensao().isEmpty() && !respostasEmMemoria.isEmpty()) {
            Map<String, List<Integer>> notasPorDimensao = new HashMap<>();
            for (DiagnosticoResposta r : respostasEmMemoria.values()) {
                if (r.getPontuacao() > 0 && r.getDimensao() != null) {
                    notasPorDimensao.computeIfAbsent(r.getDimensao(), k -> new ArrayList<>()).add(r.getPontuacao());
                }
            }

            for (Map.Entry<String, List<Integer>> entry : notasPorDimensao.entrySet()) {
                double media = entry.getValue().stream().mapToInt(Integer::intValue).average().orElse(0.0);
                analise.adicionarMediaDimensao(entry.getKey(), media);
            }
        }

        // 3. Atualizar Indicadores (Cards)
        double mediaGeral = analise.getMediaGeral();
        if (lblMediaGeral != null) {
            lblMediaGeral.setText(String.format(Locale.US, "%.2f", mediaGeral));
        }

        if (lblNivelMaturidade != null) {
            if (mediaGeral > 0.0) {
                lblNivelMaturidade.setText(analise.getNivelMaturidade());
                lblNivelMaturidade.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: " + analise.getCorMaturidadeHex() + ";");
            } else {
                lblNivelMaturidade.setText("Aguardando Avaliação");
                lblNivelMaturidade.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #757575;");
            }
        }

        // 4. Montar Série do BarChart
        graficoMaturidade.getData().clear();
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Média da Dimensão");

        for (DimensaoDiagnostico dim : DimensaoDiagnostico.values()) {
            Double nota = analise.getMediasPorDimensao().get(dim.getTitulo());
            double valorGrafico = (nota != null) ? nota : 0.0;

            // Rótulo amigável curto para não poluir o eixo do gráfico
            String rotuloCurto = dim.getTitulo();
            if (rotuloCurto.contains(".")) {
                rotuloCurto = rotuloCurto.substring(0, rotuloCurto.indexOf('.')).trim();
                rotuloCurto = "Área " + rotuloCurto;
            }

            serie.getData().add(new XYChart.Data<>(rotuloCurto, valorGrafico));
        }

        graficoMaturidade.getData().add(serie);
    }

    private void limparDashboardAnalitico() {
        if (lblMediaGeral != null) lblMediaGeral.setText("0.0");
        if (lblNivelMaturidade != null) {
            lblNivelMaturidade.setText("Aguardando Avaliação");
            lblNivelMaturidade.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #757575;");
        }
        if (graficoMaturidade != null) {
            graficoMaturidade.getData().clear();
        }
    }
}