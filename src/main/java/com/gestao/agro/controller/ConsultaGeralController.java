package com.gestao.agro.controller;
import javafx.collections.transformation.SortedList;
import com.gestao.agro.model.Organizacao;
import com.gestao.agro.model.Produtor;
import com.gestao.agro.repository.OrganizacaoRepository;
import com.gestao.agro.repository.ProdutorRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ConsultaGeralController {

    // --- Campos de Filtro / Busca ---
    @FXML private TextField txtFiltroOrganizacao;
    @FXML private TextField txtFiltroProdutor;

    // --- Tabela Organizações / Cooperativas ---
    @FXML private TableView<Organizacao> tblOrganizacoes;
    @FXML private TableColumn<Organizacao, Integer> colOrgId;
    @FXML private TableColumn<Organizacao, String> colOrgNome;
    @FXML private TableColumn<Organizacao, String> colOrgCnpj;
    @FXML private TableColumn<Organizacao, String> colOrgMunicipio;
    @FXML private TableColumn<Organizacao, Integer> colOrgSocios;
    @FXML private TableColumn<Organizacao, Double> colOrgFaturamento;
    @FXML private TableColumn<Organizacao, String> colOrgSync;

    // --- Tabela Produtores ---
    @FXML private TableView<Produtor> tblProdutoresGeral;
    @FXML private TableColumn<Produtor, Integer> colPrdId;
    @FXML private TableColumn<Produtor, Integer> colPrdOrgId;
    @FXML private TableColumn<Produtor, String> colPrdNome;
    @FXML private TableColumn<Produtor, Integer> colPrdIdade;
    @FXML private TableColumn<Produtor, String> colPrdGenero;
    @FXML private TableColumn<Produtor, String> colPrdCpf;
    @FXML private TableColumn<Produtor, String> colPrdComunidade;
    @FXML private TableColumn<Produtor, String> colPrdDestino;
    @FXML private TableColumn<Produtor, String> colPrdConsultor;

    private final OrganizacaoRepository orgRepo = new OrganizacaoRepository();
    private final ProdutorRepository prodRepo = new ProdutorRepository();

    private final ObservableList<Organizacao> dadosOrg = FXCollections.observableArrayList();
    private final ObservableList<Produtor> dadosPrd = FXCollections.observableArrayList();

    private FilteredList<Organizacao> dadosOrgFiltrados;
    private FilteredList<Produtor> dadosPrdFiltrados;

    @FXML
    public void initialize() {
        configurarColunas();
        configurarFiltros();
        atualizarTabelas();
    }

    private void configurarColunas() {
        if (colOrgId != null) colOrgId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colOrgNome != null) colOrgNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        if (colOrgCnpj != null) colOrgCnpj.setCellValueFactory(new PropertyValueFactory<>("cnpj"));
        if (colOrgMunicipio != null) colOrgMunicipio.setCellValueFactory(new PropertyValueFactory<>("municipioComunidade"));
        if (colOrgSocios != null) colOrgSocios.setCellValueFactory(new PropertyValueFactory<>("numeroAssociados"));
        if (colOrgFaturamento != null) colOrgFaturamento.setCellValueFactory(new PropertyValueFactory<>("faturamentoEstimado"));
        if (colOrgSync != null) colOrgSync.setCellValueFactory(new PropertyValueFactory<>("syncStatus"));

        if (colPrdId != null) colPrdId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colPrdOrgId != null) colPrdOrgId.setCellValueFactory(new PropertyValueFactory<>("organizacaoId"));
        if (colPrdNome != null) colPrdNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        if (colPrdIdade != null) colPrdIdade.setCellValueFactory(new PropertyValueFactory<>("idade"));
        if (colPrdGenero != null) colPrdGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));
        if (colPrdCpf != null) colPrdCpf.setCellValueFactory(new PropertyValueFactory<>("cpf"));
        if (colPrdComunidade != null) colPrdComunidade.setCellValueFactory(new PropertyValueFactory<>("comunidade"));
        if (colPrdDestino != null) colPrdDestino.setCellValueFactory(new PropertyValueFactory<>("destinoProducao"));
        if (colPrdConsultor != null) colPrdConsultor.setCellValueFactory(new PropertyValueFactory<>("consultorBloqueio"));
    }

    private void configurarFiltros() {
        dadosOrgFiltrados = new FilteredList<>(dadosOrg, p -> true);
        dadosPrdFiltrados = new FilteredList<>(dadosPrd, p -> true);

        // 1. Envolve a FilteredList em SortedList para permitir ordenação por clique na coluna
        SortedList<Organizacao> dadosOrgOrdenados = new SortedList<>(dadosOrgFiltrados);
        SortedList<Produtor> dadosPrdOrdenados = new SortedList<>(dadosPrdFiltrados);

        // 2. Conecta o comparador das tabelas às SortedLists
        if (tblOrganizacoes != null) {
            dadosOrgOrdenados.comparatorProperty().bind(tblOrganizacoes.comparatorProperty());
            tblOrganizacoes.setItems(dadosOrgOrdenados);
        }

        if (tblProdutoresGeral != null) {
            dadosPrdOrdenados.comparatorProperty().bind(tblProdutoresGeral.comparatorProperty());
            tblProdutoresGeral.setItems(dadosPrdOrdenados);
        }

        // Filtro em tempo real para Cooperativas
        if (txtFiltroOrganizacao != null) {
            txtFiltroOrganizacao.textProperty().addListener((obs, antigo, termo) -> {
                dadosOrgFiltrados.setPredicate(org -> {
                    if (termo == null || termo.isBlank()) {
                        return true;
                    }
                    String termoLower = termo.toLowerCase().trim();
                    boolean bateNome = org.getNome() != null && org.getNome().toLowerCase().contains(termoLower);
                    boolean bateCnpj = org.getCnpj() != null && org.getCnpj().contains(termoLower);
                    boolean bateMun = org.getMunicipioComunidade() != null && org.getMunicipioComunidade().toLowerCase().contains(termoLower);

                    return bateNome || bateCnpj || bateMun;
                });
            });
        }

        // Filtro em tempo real para Produtores (busca por nome, comunidade, ID da cooperativa ou CPF)
        if (txtFiltroProdutor != null) {
            txtFiltroProdutor.textProperty().addListener((obs, antigo, termo) -> {
                dadosPrdFiltrados.setPredicate(prod -> {
                    if (termo == null || termo.isBlank()) {
                        return true;
                    }
                    String termoLower = termo.toLowerCase().trim();
                    String termoDigitos = termo.replaceAll("\\D", "");

                    boolean bateNome = prod.getNome() != null && prod.getNome().toLowerCase().contains(termoLower);
                    boolean bateComunidade = prod.getComunidade() != null && prod.getComunidade().toLowerCase().contains(termoLower);
                    boolean bateOrgId = String.valueOf(prod.getOrganizacaoId()).equals(termoLower);

                    boolean bateCpf = false;
                    if (prod.getCpf() != null) {
                        bateCpf = prod.getCpf().contains(termoLower) ||
                                  (!termoDigitos.isEmpty() && prod.getCpf().replaceAll("\\D", "").contains(termoDigitos));
                    }

                    return bateNome || bateComunidade || bateOrgId || bateCpf;
                });
            });
        }
    }

    @FXML
    public void atualizarTabelas() {
        try {
            dadosOrg.clear();
            dadosOrg.addAll(orgRepo.listarTodas());

            dadosPrd.clear();
            dadosPrd.addAll(prodRepo.listarTodos());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}