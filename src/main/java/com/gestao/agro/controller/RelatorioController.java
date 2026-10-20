package com.gestao.agro.controller;

import com.gestao.agro.model.*;
import com.gestao.agro.repository.*;
import com.gestao.agro.util.ExportacaoService;
import com.gestao.agro.util.SessaoUsuario;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RelatorioController {

    // --- Seleção de Entidade ---
    @FXML private RadioButton rbTipoOrg;
    @FXML private RadioButton rbTipoProd;
    @FXML private ToggleGroup grpTipoAlvo;
    @FXML private Label lblEntidadeAlvo;
    @FXML private ComboBox<Object> cbEntidadeAlvo;
    @FXML private TextField txtConsultor;
    
    // --- CheckBoxes do Dossiê Word ---
    @FXML private CheckBox chkImoAf;
    @FXML private ComboBox<DiagnosticoVersao> cbVersaoCiclo;
    @FXML private CheckBox chkPestel;
    @FXML private CheckBox chkFofa;
    @FXML private CheckBox chkGut;
    @FXML private CheckBox chk5w2h;

    @FXML private Label lblStatusMensagem;

    // --- Repositórios ---
    private final OrganizacaoRepository orgRepo = new OrganizacaoRepository();
    private final ProdutorRepository prodRepo = new ProdutorRepository();
    private final PlanoAcaoRepository planoRepo = new PlanoAcaoRepository();
    private final PestelRepository pestelRepo = new PestelRepository();
    private final SwotRepository swotRepo = new SwotRepository();
    private final GutRepository gutRepo = new GutRepository();
    private final DiagnosticoRepository diagRepo = new DiagnosticoRepository();

    @FXML
    public void initialize() {
        txtConsultor.setText(SessaoUsuario.getNomeConsultor());
        configurarSeletoresEntidade();
        alternarTipoAlvo();
        
        chkImoAf.selectedProperty().addListener((obs, antigo, selecionado) -> {
            cbVersaoCiclo.setDisable(!selecionado);
        });
    }

    private void configurarSeletoresEntidade() {
        grpTipoAlvo.selectedToggleProperty().addListener((obs, antigo, novo) -> alternarTipoAlvo());

        cbEntidadeAlvo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Object obj) {
                if (obj instanceof Organizacao org) return org.getId() + " - " + org.getNome();
                if (obj instanceof Produtor prod) return prod.getId() + " - " + prod.getNome() + " (" + prod.getComunidade() + ")";
                return "";
            }
            @Override
            public Object fromString(String string) { return null; }
        });

        cbEntidadeAlvo.getSelectionModel().selectedItemProperty().addListener((obs, antigo, selecionado) -> {
            carregarCiclosEntidade(selecionado);
        });

        cbVersaoCiclo.setConverter(new StringConverter<>() {
            @Override
            public String toString(DiagnosticoVersao v) {
                if (v == null) return "";
                String dataStr = (v.getDataAplicacao() != null) ? v.getDataAplicacao().toString() : "Sem Data";
                String statusStr = (v.getStatus() != null) ? v.getStatus() : "Pendente";
                return "Ciclo " + v.getNumeroVersao() + " (" + dataStr + " - " + statusStr + ")";
            }
            @Override
            public DiagnosticoVersao fromString(String string) { return null; }
        });
    }

    private void alternarTipoAlvo() {
        cbEntidadeAlvo.getItems().clear();
        cbVersaoCiclo.getItems().clear();
        lblStatusMensagem.setText("");

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

    private void carregarCiclosEntidade(Object entidade) {
        cbVersaoCiclo.getItems().clear();
        if (entidade == null) return;

        try {
            List<DiagnosticoVersao> versoes;
            if (entidade instanceof Organizacao org) {
                versoes = diagRepo.listarVersoesPorOrganizacao(org.getId());
            } else {
                versoes = diagRepo.listarVersoesPorProdutor(((Produtor) entidade).getId());
            }

            if (versoes != null && !versoes.isEmpty()) {
                cbVersaoCiclo.getItems().addAll(versoes);
                cbVersaoCiclo.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            exibirMensagemErro("Erro ao carregar ciclos de diagnóstico: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==============================================================
    //     GERAÇÃO DO GRÁFICO OFF-SCREEN
    // ==============================================================
    private File gerarGraficoImoAfOffscreen(DiagnosticoVersao versao) {
        if (versao == null) return null;
        try {
            diagRepo.calcularMediasPorDimensao(versao);

            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis(0, 5, 1);
            BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
            chart.setAnimated(false); 
            chart.setTitle("Perfil de Maturidade IMO-AF - Ciclo " + versao.getNumeroVersao());

            XYChart.Series<String, Number> serie = new XYChart.Series<>();
            for (DimensaoDiagnostico dim : DimensaoDiagnostico.values()) {
                Double nota = versao.getMediasPorDimensao().get(dim.getTitulo());
                double valor = (nota != null) ? nota : 0.0;
                
                String rotuloCurto = dim.getTitulo();
                if (rotuloCurto.contains(".")) {
                    rotuloCurto = "D" + rotuloCurto.substring(0, rotuloCurto.indexOf('.')).trim();
                }
                serie.getData().add(new XYChart.Data<>(rotuloCurto, valor));
            }
            chart.getData().add(serie);

            Scene scene = new Scene(chart, 800, 400);
            chart.applyCss();
            chart.layout();

            WritableImage image = chart.snapshot(new SnapshotParameters(), null);
            File tempFile = File.createTempFile("grafico_imo_", ".png");
            tempFile.deleteOnExit();
            
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", tempFile);
            return tempFile;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    public void gerarRelatorioWord() {
        Object entidade = cbEntidadeAlvo.getValue();
        if (entidade == null) {
            exibirMensagemErro("Selecione uma Cooperativa ou Produtor Rural primeiro.");
            return;
        }

        boolean incImo = chkImoAf.isSelected();
        DiagnosticoVersao versao = cbVersaoCiclo.getValue();

        if (incImo && versao == null) {
            exibirMensagemErro("Para exportar o Módulo 2, selecione um ciclo de avaliação na lista suspensa.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Dossiê de Diagnóstico (Word)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documento Word (*.docx)", "*.docx"));
        
        String nomePadrao = "Dossie_Diagnostico_" + getNomeEntidade(entidade).replaceAll("[^a-zA-Z0-9]", "_") + ".docx";
        fileChooser.setInitialFileName(nomePadrao);
        
        File file = fileChooser.showSaveDialog(cbEntidadeAlvo.getScene().getWindow());
        
        if (file != null) {
            lblStatusMensagem.setStyle("-fx-text-fill: #1565C0;");
            lblStatusMensagem.setText("A gerar o ficheiro e os gráficos... Por favor, aguarde.");

            File imagemGrafico = null;
            if (incImo) {
                imagemGrafico = gerarGraficoImoAfOffscreen(versao);
            }
            final File imagemFinal = imagemGrafico;

            boolean incPestel = chkPestel.isSelected();
            boolean incFofa = chkFofa.isSelected();
            boolean incGut = chkGut.isSelected();
            boolean inc5w2h = chk5w2h.isSelected();

            CompletableFuture.runAsync(() -> {
                try {
                    List<FatorPestel> pestel = incPestel ? buscarPestelEntidade(entidade) : null;
                    List<FatorSwot> swotFatores = incFofa ? buscarSwotEntidade(entidade) : null;
                    List<CruzamentoSwot> tows = incFofa ? buscarTowsEntidade(entidade) : null;
                    List<ItemGut> gut = incGut ? buscarGutEntidade(entidade) : null;
                    List<PlanoAcao5W2H> planos = inc5w2h ? buscarPlanosEntidade(entidade) : null;

                    ExportacaoService.exportarDossieWord(file, getNomeEntidade(entidade), incImo, versao, pestel, swotFatores, tows, gut, planos, imagemFinal);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).thenRun(() -> {
                Platform.runLater(() -> {
                    lblStatusMensagem.setStyle("-fx-text-fill: #2e7d32;");
                    lblStatusMensagem.setText("Dossiê Word gerado com sucesso em: " + file.getAbsolutePath());
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> exibirMensagemErro("Erro na exportação: " + ex.getCause().getMessage()));
                return null;
            });
        }
    }

    @FXML
    public void gerarPlanilhaExcel() {
        Object entidade = cbEntidadeAlvo.getValue();
        if (entidade == null) {
            exibirMensagemErro("Selecione uma Cooperativa ou Produtor Rural primeiro.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Planilha Excel (5W2H)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Planilha Excel (*.xlsx)", "*.xlsx"));
        
        String nomePadrao = "Plano_Acao_" + getNomeEntidade(entidade).replaceAll("[^a-zA-Z0-9]", "_") + ".xlsx";
        fileChooser.setInitialFileName(nomePadrao);
        
        File file = fileChooser.showSaveDialog(cbEntidadeAlvo.getScene().getWindow());
        
        if (file != null) {
            lblStatusMensagem.setStyle("-fx-text-fill: #1565C0;");
            lblStatusMensagem.setText("A exportar Excel... Por favor, aguarde.");

            CompletableFuture.runAsync(() -> {
                try {
                    List<PlanoAcao5W2H> planos = buscarPlanosEntidade(entidade);
                    ExportacaoService.exportarPlanoAcaoExcel(file, planos);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).thenRun(() -> {
                Platform.runLater(() -> {
                    lblStatusMensagem.setStyle("-fx-text-fill: #2e7d32;");
                    lblStatusMensagem.setText("Planilha Excel gerada com sucesso em: " + file.getAbsolutePath());
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> exibirMensagemErro("Erro na exportação Excel: " + ex.getCause().getMessage()));
                return null;
            });
        }
    }

    private String getNomeEntidade(Object entidade) {
        if (entidade instanceof Organizacao org) return org.getNome();
        if (entidade instanceof Produtor prod) return prod.getNome();
        return "Desconhecida";
    }

    private List<PlanoAcao5W2H> buscarPlanosEntidade(Object entidade) throws Exception {
        return (entidade instanceof Organizacao org) ? planoRepo.listarPorOrganizacao(org.getId()) : planoRepo.listarPorProdutor(((Produtor) entidade).getId());
    }

    private List<FatorPestel> buscarPestelEntidade(Object entidade) throws Exception {
        return (entidade instanceof Organizacao org) ? pestelRepo.listarPorOrganizacao(org.getId()) : pestelRepo.listarPorProdutor(((Produtor) entidade).getId());
    }

    private List<FatorSwot> buscarSwotEntidade(Object entidade) throws Exception {
        return (entidade instanceof Organizacao org) ? swotRepo.listarFatoresPorOrganizacao(org.getId()) : swotRepo.listarFatoresPorProdutor(((Produtor) entidade).getId());
    }

    private List<CruzamentoSwot> buscarTowsEntidade(Object entidade) throws Exception {
        return (entidade instanceof Organizacao org) ? swotRepo.listarCruzamentosPorOrganizacao(org.getId()) : swotRepo.listarCruzamentosPorProdutor(((Produtor) entidade).getId());
    }

    private List<ItemGut> buscarGutEntidade(Object entidade) throws Exception {
        return (entidade instanceof Organizacao org) ? gutRepo.listarPorOrganizacao(org.getId()) : gutRepo.listarPorProdutor(((Produtor) entidade).getId());
    }

    private void exibirMensagemErro(String msg) {
        lblStatusMensagem.setStyle("-fx-text-fill: #d32f2f;");
        lblStatusMensagem.setText(msg);
    }
}