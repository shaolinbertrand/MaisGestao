package com.gestao.agro.repository;

import com.gestao.agro.model.FatorPestel;
import com.gestao.agro.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PestelRepository {

    public PestelRepository() {
        criarTabelaSeNaoExistir();
    }

    private void criarTabelaSeNaoExistir() {
        String sql = """
            CREATE TABLE IF NOT EXISTS matriz_pestel (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                organizacao_id INTEGER,
                produtor_id INTEGER,
                dimensao TEXT NOT NULL,
                tipo TEXT NOT NULL,
                descricao TEXT NOT NULL,
                impacto INTEGER DEFAULT 3,
                probabilidade INTEGER DEFAULT 3,
                acao_sugerida TEXT,
                data_registro TEXT,
                FOREIGN KEY (organizacao_id) REFERENCES organizacao(id),
                FOREIGN KEY (produtor_id) REFERENCES produtor(id)
            );
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar tabela matriz_pestel: " + e.getMessage());
        }
    }

    public FatorPestel salvar(FatorPestel fator) throws SQLException {
        String sql = """
            INSERT INTO matriz_pestel (
                organizacao_id, produtor_id, dimensao, tipo, descricao,
                impacto, probabilidade, acao_sugerida, data_registro
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (fator.getOrganizacaoId() != null) {
                stmt.setInt(1, fator.getOrganizacaoId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }

            if (fator.getProdutorId() != null) {
                stmt.setInt(2, fator.getProdutorId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }

            stmt.setString(3, fator.getDimensao());
            stmt.setString(4, fator.getTipo());
            stmt.setString(5, fator.getDescricao());
            stmt.setInt(6, fator.getImpacto());
            stmt.setInt(7, fator.getProbabilidade());
            stmt.setString(8, fator.getAcaoSugerida());
            stmt.setString(9, fator.getDataRegistro() != null ? fator.getDataRegistro().toString() : LocalDate.now().toString());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    fator.setId(rs.getInt(1));
                }
            }

            if (fator.getId() == null || fator.getId() == 0) {
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        fator.setId(rs.getInt(1));
                    }
                }
            }
        }
        return fator;
    }

    public void atualizar(FatorPestel fator) throws SQLException {
        String sql = """
            UPDATE matriz_pestel SET
                dimensao = ?,
                tipo = ?,
                descricao = ?,
                impacto = ?,
                probabilidade = ?,
                acao_sugerida = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fator.getDimensao());
            stmt.setString(2, fator.getTipo());
            stmt.setString(3, fator.getDescricao());
            stmt.setInt(4, fator.getImpacto());
            stmt.setInt(5, fator.getProbabilidade());
            stmt.setString(6, fator.getAcaoSugerida());
            stmt.setInt(7, fator.getId());

            stmt.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM matriz_pestel WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<FatorPestel> listarPorOrganizacao(int organizacaoId) throws SQLException {
        List<FatorPestel> lista = new ArrayList<>();
        String sql = "SELECT * FROM matriz_pestel WHERE organizacao_id = ? ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, organizacaoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(extrairFator(rs));
                }
            }
        }
        return lista;
    }

    public List<FatorPestel> listarPorProdutor(int produtorId) throws SQLException {
        List<FatorPestel> lista = new ArrayList<>();
        String sql = "SELECT * FROM matriz_pestel WHERE produtor_id = ? ORDER BY id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, produtorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(extrairFator(rs));
                }
            }
        }
        return lista;
    }

    private FatorPestel extrairFator(ResultSet rs) throws SQLException {
        FatorPestel f = new FatorPestel();
        f.setId(rs.getInt("id"));

        int orgId = rs.getInt("organizacao_id");
        f.setOrganizacaoId(rs.wasNull() ? null : orgId);

        int prodId = rs.getInt("produtor_id");
        f.setProdutorId(rs.wasNull() ? null : prodId);

        f.setDimensao(rs.getString("dimensao"));
        f.setTipo(rs.getString("tipo"));
        f.setDescricao(rs.getString("descricao"));
        f.setImpacto(rs.getInt("impacto"));
        f.setProbabilidade(rs.getInt("probabilidade"));
        f.setAcaoSugerida(rs.getString("acao_sugerida"));

        String dtStr = rs.getString("data_registro");
        if (dtStr != null && !dtStr.isBlank()) {
            try {
                f.setDataRegistro(LocalDate.parse(dtStr));
            } catch (Exception ignored) {}
        }
        return f;
    }
}