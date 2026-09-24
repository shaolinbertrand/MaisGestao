package com.gestao.agro.repository;

import com.gestao.agro.model.DiagnosticoResposta;
import com.gestao.agro.model.DiagnosticoVersao;
import com.gestao.agro.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DiagnosticoRepository {

    // ==========================================
    //           CABEÇALHO (VERSÃO)
    // ==========================================

    public DiagnosticoVersao salvarVersao(DiagnosticoVersao versao) throws SQLException {
        String sql = """
            INSERT INTO diagnostico_versao (
                organizacao_id, produtor_id, numero_versao, data_aplicacao,
                consultor_responsavel, status, sync_status
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Tratamento de nulos específico para SQLite/JDBC
            if (versao.getOrganizacaoId() != null) {
                stmt.setInt(1, versao.getOrganizacaoId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }

            if (versao.getProdutorId() != null) {
                stmt.setInt(2, versao.getProdutorId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }

            stmt.setInt(3, versao.getNumeroVersao());
            stmt.setString(4, versao.getDataAplicacao() != null ? versao.getDataAplicacao().toString() : LocalDate.now().toString());
            stmt.setString(5, versao.getConsultorResponsavel());
            stmt.setString(6, versao.getStatus() != null ? versao.getStatus() : "EM_ANDAMENTO");
            stmt.setString(7, "PENDENTE");

            stmt.executeUpdate();

            // 1ª tentativa: captura por getGeneratedKeys
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    versao.setId(rs.getInt(1));
                }
            }

            // 2ª tentativa (Fallback para SQLite): last_insert_rowid()
            if (versao.getId() == null || versao.getId() == 0) {
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        versao.setId(rs.getInt(1));
                    }
                }
            }
        }
        return versao;
    }

    public void atualizarStatusVersao(int versaoId, String status) throws SQLException {
        String sql = "UPDATE diagnostico_versao SET status = ?, sync_status = 'PENDENTE' WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, versaoId);
            stmt.executeUpdate();
        }
    }

    public List<DiagnosticoVersao> listarVersoesPorOrganizacao(int organizacaoId) throws SQLException {
        List<DiagnosticoVersao> lista = new ArrayList<>();
        String sql = "SELECT * FROM diagnostico_versao WHERE organizacao_id = ? ORDER BY numero_versao DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, organizacaoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(extrairVersao(rs));
                }
            }
        }
        return lista;
    }

    public List<DiagnosticoVersao> listarVersoesPorProdutor(int produtorId) throws SQLException {
        List<DiagnosticoVersao> lista = new ArrayList<>();
        String sql = "SELECT * FROM diagnostico_versao WHERE produtor_id = ? ORDER BY numero_versao DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, produtorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(extrairVersao(rs));
                }
            }
        }
        return lista;
    }

    // ==========================================
    //           RESPOSTAS DAS 10 DIMENSÕES
    // ==========================================

    public void salvarRespostas(Integer versaoId, List<DiagnosticoResposta> respostas) throws SQLException {
        if (versaoId == null) {
            throw new IllegalArgumentException("O ID da versão do diagnóstico não pode ser nulo!");
        }

        String deleteSql = "DELETE FROM diagnostico_resposta WHERE diagnostico_versao_id = ?";
        String insertSql = """
            INSERT INTO diagnostico_resposta (
                diagnostico_versao_id, dimensao, subdimensao, pontuacao,
                observacoes_evidencias, plano_acao_recomendado
            ) VALUES (?, ?, ?, ?, ?, ?)
        """;

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Transação única

            // Limpa respostas prévias dessa versão para evitar duplicatas em reedições
            try (PreparedStatement delStmt = conn.prepareStatement(deleteSql)) {
                delStmt.setInt(1, versaoId);
                delStmt.executeUpdate();
            }

            // Insere todas as respostas
            try (PreparedStatement insStmt = conn.prepareStatement(insertSql)) {
                for (DiagnosticoResposta r : respostas) {
                    insStmt.setInt(1, versaoId);
                    insStmt.setString(2, r.getDimensao());
                    insStmt.setString(3, r.getSubdimensao());
                    insStmt.setInt(4, r.getPontuacao());
                    insStmt.setString(5, r.getObservacoesEvidencias());
                    insStmt.setString(6, r.getPlanoAcaoRecomendado());
                    insStmt.addBatch();
                }
                insStmt.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public List<DiagnosticoResposta> listarRespostasPorVersao(int versaoId) throws SQLException {
        List<DiagnosticoResposta> lista = new ArrayList<>();
        String sql = "SELECT * FROM diagnostico_resposta WHERE diagnostico_versao_id = ? ORDER BY id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, versaoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DiagnosticoResposta r = new DiagnosticoResposta();
                    r.setId(rs.getInt("id"));
                    r.setDiagnosticoVersaoId(rs.getInt("diagnostico_versao_id"));
                    r.setDimensao(rs.getString("dimensao"));
                    r.setSubdimensao(rs.getString("subdimensao"));
                    r.setPontuacao(rs.getInt("pontuacao"));
                    r.setObservacoesEvidencias(rs.getString("observacoes_evidencias"));
                    r.setPlanoAcaoRecomendado(rs.getString("plano_acao_recomendado"));
                    lista.add(r);
                }
            }
        }
        return lista;
    }

    // ==========================================
    //           ANÁLISE E CÁLCULO DE MÉDIAS
    // ==========================================

    /**
     * Calcula a média agregada de cada dimensão e preenche o DiagnosticoVersao.
     */
    public void calcularMediasPorDimensao(DiagnosticoVersao versao) throws SQLException {
        if (versao == null || versao.getId() == null) {
            return;
        }

        String sql = """
            SELECT dimensao, AVG(pontuacao) AS media_dimensao
            FROM diagnostico_resposta
            WHERE diagnostico_versao_id = ?
            GROUP BY dimensao
            ORDER BY dimensao ASC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, versao.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String dimensao = rs.getString("dimensao");
                    double media = rs.getDouble("media_dimensao");
                    versao.adicionarMediaDimensao(dimensao, media);
                }
            }
        }
    }

    private DiagnosticoVersao extrairVersao(ResultSet rs) throws SQLException {
        DiagnosticoVersao v = new DiagnosticoVersao();
        v.setId(rs.getInt("id"));

        int orgId = rs.getInt("organizacao_id");
        v.setOrganizacaoId(rs.wasNull() ? null : orgId);

        int prodId = rs.getInt("produtor_id");
        v.setProdutorId(rs.wasNull() ? null : prodId);

        v.setNumeroVersao(rs.getInt("numero_versao"));

        String dtStr = rs.getString("data_aplicacao");
        if (dtStr != null && !dtStr.isBlank()) {
            try {
                v.setDataAplicacao(LocalDate.parse(dtStr));
            } catch (Exception ignored) {}
        }

        v.setConsultorResponsavel(rs.getString("consultor_responsavel"));
        v.setStatus(rs.getString("status"));
        v.setSyncStatus(rs.getString("sync_status"));
        return v;
    }
}