package com.gestao.agro.repository;

import com.gestao.agro.model.ItemGut;
import com.gestao.agro.model.PlanoAcao5W2H;
import com.gestao.agro.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PlanoAcaoRepository {

    public PlanoAcaoRepository() {
        criarTabelaSeNaoExistir();
    }

    private void criarTabelaSeNaoExistir() {
        String sql = """
            CREATE TABLE IF NOT EXISTS plano_acao_5w2h (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                organizacao_id INTEGER,
                produtor_id INTEGER,
                item_gut_id INTEGER,
                what_acao TEXT NOT NULL,
                why_motivo TEXT,
                where_local TEXT,
                when_prazo TEXT,
                who_responsavel TEXT,
                how_metodo TEXT,
                how_much_custo REAL DEFAULT 0.0,
                status TEXT DEFAULT 'NÃO INICIADO',
                data_registro TEXT,
                FOREIGN KEY (organizacao_id) REFERENCES organizacao(id),
                FOREIGN KEY (produtor_id) REFERENCES produtor(id),
                FOREIGN KEY (item_gut_id) REFERENCES matriz_gut(id) ON DELETE SET NULL
            );
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar tabela Plano de Ação 5W2H: " + e.getMessage());
        }
    }

    // ==========================================
    //            OPERAÇÕES CRUD BÁSICAS
    // ==========================================

    public PlanoAcao5W2H salvar(PlanoAcao5W2H plano) throws SQLException {
        String sql = """
            INSERT INTO plano_acao_5w2h (
                organizacao_id, produtor_id, item_gut_id, what_acao, why_motivo, 
                where_local, when_prazo, who_responsavel, how_metodo, how_much_custo, 
                status, data_registro
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (plano.getOrganizacaoId() != null) stmt.setInt(1, plano.getOrganizacaoId());
            else stmt.setNull(1, Types.INTEGER);

            if (plano.getProdutorId() != null) stmt.setInt(2, plano.getProdutorId());
            else stmt.setNull(2, Types.INTEGER);

            if (plano.getItemGutId() != null) stmt.setInt(3, plano.getItemGutId());
            else stmt.setNull(3, Types.INTEGER);

            stmt.setString(4, plano.getWhatAcao());
            stmt.setString(5, plano.getWhyMotivo());
            stmt.setString(6, plano.getWhereLocal());
            stmt.setString(7, plano.getWhenPrazo());
            stmt.setString(8, plano.getWhoResponsavel());
            stmt.setString(9, plano.getHowMetodo());
            stmt.setDouble(10, plano.getHowMuchCusto() != null ? plano.getHowMuchCusto() : 0.0);
            stmt.setString(11, plano.getStatus() != null ? plano.getStatus() : "NÃO INICIADO");
            stmt.setString(12, plano.getDataRegistro() != null ? plano.getDataRegistro().toString() : LocalDate.now().toString());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    plano.setId(rs.getInt(1));
                }
            }
        }
        return plano;
    }

    public void atualizar(PlanoAcao5W2H plano) throws SQLException {
        String sql = """
            UPDATE plano_acao_5w2h SET
                what_acao = ?,
                why_motivo = ?,
                where_local = ?,
                when_prazo = ?,
                who_responsavel = ?,
                how_metodo = ?,
                how_much_custo = ?,
                status = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, plano.getWhatAcao());
            stmt.setString(2, plano.getWhyMotivo());
            stmt.setString(3, plano.getWhereLocal());
            stmt.setString(4, plano.getWhenPrazo());
            stmt.setString(5, plano.getWhoResponsavel());
            stmt.setString(6, plano.getHowMetodo());
            stmt.setDouble(7, plano.getHowMuchCusto() != null ? plano.getHowMuchCusto() : 0.0);
            stmt.setString(8, plano.getStatus());
            stmt.setInt(9, plano.getId());

            stmt.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM plano_acao_5w2h WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<PlanoAcao5W2H> listarPorOrganizacao(int orgId) throws SQLException {
        List<PlanoAcao5W2H> lista = new ArrayList<>();
        String sql = "SELECT * FROM plano_acao_5w2h WHERE organizacao_id = ? ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orgId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(extrairPlanoAcao(rs));
                }
            }
        }
        return lista;
    }

    public List<PlanoAcao5W2H> listarPorProdutor(int prodId) throws SQLException {
        List<PlanoAcao5W2H> lista = new ArrayList<>();
        String sql = "SELECT * FROM plano_acao_5w2h WHERE produtor_id = ? ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, prodId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(extrairPlanoAcao(rs));
                }
            }
        }
        return lista;
    }

    // ==========================================
    //     IMPORTAÇÃO AUTOMATIZADA: MATRIZ GUT
    // ==========================================

    public int importarItensPrioritariosDoGut(Integer orgId, Integer prodId) throws SQLException {
        GutRepository gutRepo = new GutRepository();
        // Carrega todos os itens do GUT para esta entidade
        List<ItemGut> itensGut = (orgId != null) 
            ? gutRepo.listarPorOrganizacao(orgId) 
            : gutRepo.listarPorProdutor(prodId);

        // Carrega os planos já existentes para não duplicar
        List<PlanoAcao5W2H> planosExistentes = (orgId != null) 
            ? listarPorOrganizacao(orgId) 
            : listarPorProdutor(prodId);

        int totalImportados = 0;

        for (ItemGut item : itensGut) {
            // Importa apenas itens classificados como CRÍTICO ou ALTO
            if ("CRÍTICO".equalsIgnoreCase(item.getNivelPrioridade()) || "ALTO".equalsIgnoreCase(item.getNivelPrioridade())) {
                
                // Verifica se já existe um plano de ação ligado a este item do GUT
                boolean jaExiste = planosExistentes.stream().anyMatch(p -> 
                    p.getItemGutId() != null && p.getItemGutId().equals(item.getId())
                );

                if (!jaExiste) {
                    PlanoAcao5W2H novoPlano = new PlanoAcao5W2H();
                    novoPlano.setOrganizacaoId(orgId);
                    novoPlano.setProdutorId(prodId);
                    novoPlano.setItemGutId(item.getId());
                    
                    // Preenche o What (O Quê) com a Ação Proposta do GUT
                    String what = (item.getAcaoProposta() != null && !item.getAcaoProposta().isBlank()) 
                                ? item.getAcaoProposta() 
                                : "Resolver: " + item.getProblemaAcao();
                    novoPlano.setWhatAcao(what);
                    
                    // Preenche o Why (Porquê) com o Problema que deu origem
                    novoPlano.setWhyMotivo("Prioridade " + item.getNivelPrioridade() + " no GUT. Problema: " + item.getProblemaAcao());
                    
                    salvar(novoPlano);
                    totalImportados++;
                }
            }
        }
        return totalImportados;
    }

    // ==========================================
    //            MÉTODO AUXILIAR
    // ==========================================

    private PlanoAcao5W2H extrairPlanoAcao(ResultSet rs) throws SQLException {
        PlanoAcao5W2H p = new PlanoAcao5W2H();
        p.setId(rs.getInt("id"));

        int orgId = rs.getInt("organizacao_id");
        p.setOrganizacaoId(rs.wasNull() ? null : orgId);

        int prodId = rs.getInt("produtor_id");
        p.setProdutorId(rs.wasNull() ? null : prodId);

        int gutId = rs.getInt("item_gut_id");
        p.setItemGutId(rs.wasNull() ? null : gutId);

        p.setWhatAcao(rs.getString("what_acao"));
        p.setWhyMotivo(rs.getString("why_motivo"));
        p.setWhereLocal(rs.getString("where_local"));
        p.setWhenPrazo(rs.getString("when_prazo"));
        p.setWhoResponsavel(rs.getString("who_responsavel"));
        p.setHowMetodo(rs.getString("how_metodo"));
        p.setHowMuchCusto(rs.getDouble("how_much_custo"));
        p.setStatus(rs.getString("status"));

        String dtStr = rs.getString("data_registro");
        if (dtStr != null && !dtStr.isBlank()) {
            try {
                p.setDataRegistro(LocalDate.parse(dtStr));
            } catch (Exception ignored) {}
        }
        return p;
    }
}