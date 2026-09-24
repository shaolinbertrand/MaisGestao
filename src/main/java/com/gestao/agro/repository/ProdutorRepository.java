package com.gestao.agro.repository;

import com.gestao.agro.model.Produtor;
import com.gestao.agro.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProdutorRepository {

    public Produtor salvar(Produtor produtor) throws SQLException {
        String sql = """
            INSERT INTO produtor (
                organizacao_id, nome, cpf, comunidade, data_nascimento, genero,
                possui_caf, principais_culturas, area_propriedade, destino_producao,
                consultor_bloqueio, sync_status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, produtor.getOrganizacaoId());
            stmt.setString(2, produtor.getNome());
            stmt.setString(3, produtor.getCpf());
            stmt.setString(4, produtor.getComunidade());
            stmt.setString(5, produtor.getDataNascimento() != null ? produtor.getDataNascimento().toString() : null);
            stmt.setString(6, produtor.getGenero());
            stmt.setInt(7, produtor.isPossuiCaf() ? 1 : 0);
            stmt.setString(8, produtor.getPrincipaisCulturas());
            stmt.setObject(9, produtor.getAreaPropriedade());
            stmt.setString(10, produtor.getDestinoProducao() != null ? produtor.getDestinoProducao() : "COOPERATIVA");
            stmt.setString(11, produtor.getConsultorBloqueio());
            stmt.setString(12, "PENDENTE");

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    produtor.setId(rs.getInt(1));
                }
            }
        }
        return produtor;
    }

    public void atualizar(Produtor produtor) throws SQLException {
        String sql = """
            UPDATE produtor SET
                organizacao_id = ?,
                nome = ?,
                cpf = ?,
                comunidade = ?,
                data_nascimento = ?,
                genero = ?,
                possui_caf = ?,
                principais_culturas = ?,
                area_propriedade = ?,
                destino_producao = ?,
                consultor_bloqueio = ?,
                sync_status = 'PENDENTE'
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, produtor.getOrganizacaoId());
            stmt.setString(2, produtor.getNome());
            stmt.setString(3, produtor.getCpf());
            stmt.setString(4, produtor.getComunidade());
            stmt.setString(5, produtor.getDataNascimento() != null ? produtor.getDataNascimento().toString() : null);
            stmt.setString(6, produtor.getGenero());
            stmt.setInt(7, produtor.isPossuiCaf() ? 1 : 0);
            stmt.setString(8, produtor.getPrincipaisCulturas());
            stmt.setObject(9, produtor.getAreaPropriedade());
            stmt.setString(10, produtor.getDestinoProducao() != null ? produtor.getDestinoProducao() : "COOPERATIVA");
            stmt.setString(11, produtor.getConsultorBloqueio());
            stmt.setInt(12, produtor.getId());

            stmt.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM produtor WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Produtor buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM produtor WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extrairProdutor(rs);
                }
            }
        }
        return null;
    }

    public void definirBloqueio(int produtorId, String nomeConsultor) throws SQLException {
        String sql = "UPDATE produtor SET consultor_bloqueio = ?, sync_status = 'PENDENTE' WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomeConsultor);
            stmt.setInt(2, produtorId);
            stmt.executeUpdate();
        }
    }

    public List<Produtor> listarBloqueadosPorConsultor(String consultor) throws SQLException {
        List<Produtor> lista = new ArrayList<>();
        String sql = "SELECT * FROM produtor WHERE consultor_bloqueio = ? ORDER BY nome ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, consultor);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(extrairProdutor(rs));
                }
            }
        }
        return lista;
    }

    public List<Produtor> listarPorOrganizacao(int organizacaoId) throws SQLException {
        List<Produtor> lista = new ArrayList<>();
        String sql = "SELECT * FROM produtor WHERE organizacao_id = ? ORDER BY nome ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, organizacaoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(extrairProdutor(rs));
                }
            }
        }
        return lista;
    }

    public List<Produtor> listarTodos() throws SQLException {
        List<Produtor> lista = new ArrayList<>();
        String sql = "SELECT * FROM produtor ORDER BY nome ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(extrairProdutor(rs));
            }
        }
        return lista;
    }

    public boolean existeCpfNaOrganizacao(String cpf, int organizacaoId, Integer produtorIdAtual) throws SQLException {
        if (cpf == null || cpf.isBlank()) {
            return false;
        }

        String apenasDigitos = cpf.replaceAll("\\D", "");
        if (apenasDigitos.isBlank()) {
            return false;
        }

        // Compara tanto o texto exato quanto limpando pontos e traços no banco
        String sql = """
            SELECT COUNT(1) FROM produtor 
            WHERE organizacao_id = ? 
              AND (
                  REPLACE(REPLACE(REPLACE(cpf, '.', ''), '-', ''), ' ', '') = ?
                  OR cpf = ?
              )
              AND (? IS NULL OR id != ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, organizacaoId);
            stmt.setString(2, apenasDigitos);
            stmt.setString(3, cpf.trim());
            stmt.setObject(4, produtorIdAtual);
            stmt.setObject(5, produtorIdAtual);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private Produtor extrairProdutor(ResultSet rs) throws SQLException {
        Produtor p = new Produtor();
        p.setId(rs.getInt("id"));
        p.setOrganizacaoId(rs.getInt("organizacao_id"));
        p.setNome(rs.getString("nome"));
        p.setCpf(rs.getString("cpf"));
        p.setComunidade(rs.getString("comunidade"));

        String dataNascStr = rs.getString("data_nascimento");
        if (dataNascStr != null && !dataNascStr.isBlank()) {
            try {
                p.setDataNascimento(LocalDate.parse(dataNascStr));
            } catch (Exception ignored) {}
        }

        p.setGenero(rs.getString("genero"));
        p.setPossuiCaf(rs.getInt("possui_caf") == 1);
        p.setPrincipaisCulturas(rs.getString("principais_culturas"));
        p.setAreaPropriedade(rs.getDouble("area_propriedade"));
        p.setDestinoProducao(rs.getString("destino_producao"));
        p.setConsultorBloqueio(rs.getString("consultor_bloqueio"));
        p.setSyncStatus(rs.getString("sync_status"));
        return p;
    }
}